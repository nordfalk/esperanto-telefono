/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dr.exoplayer;

import android.media.MediaCodec;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsMasterPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.upstream.BufferPool;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import java.io.IOException;

/**
 * A {@link dk.dr.exoplayer.DemoPlayer.RendererBuilder} for HLS.
 */
public class HlsRendererBuilder implements DemoPlayer.RendererBuilder, ManifestCallback<HlsMasterPlaylist> {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int VIDEO_BUFFER_SEGMENTS = 200;

  private final String userAgent;
  private final String url;
  private final String contentId;

  private DemoPlayer player;
  private DemoPlayer.RendererBuilderCallback callback;

  public HlsRendererBuilder(String userAgent, String url, String contentId) {
    this.userAgent = userAgent;
    this.url = url;
    this.contentId = contentId;
  }

  @Override
  public void buildRenderers(DemoPlayer player, DemoPlayer.RendererBuilderCallback callback) {
    this.player = player;
    this.callback = callback;
    HlsMasterPlaylistParser parser = new HlsMasterPlaylistParser();
    ManifestFetcher<HlsMasterPlaylist> mediaPlaylistFetcher =
        new ManifestFetcher<HlsMasterPlaylist>(parser, contentId, url);
    mediaPlaylistFetcher.singleLoad(player.getMainHandler().getLooper(), this);
  }

  @Override
  public void onManifestError(String contentId, IOException e) {
    callback.onRenderersError(e);
  }

  @Override
  public void onManifest(String contentId, HlsMasterPlaylist manifest) {
    LoadControl loadControl = new DefaultLoadControl(new BufferPool(BUFFER_SEGMENT_SIZE));

    DataSource dataSource = new HttpDataSource(userAgent, null, null);
    HlsChunkSource chunkSource = new HlsChunkSource(dataSource, manifest);
    HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
        VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, 2);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
        MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, player.getMainHandler(), player, 50);
    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

    TrackRenderer[] renderers = new TrackRenderer[DemoPlayer.RENDERER_COUNT];
    renderers[DemoPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[DemoPlayer.TYPE_AUDIO] = audioRenderer;
    callback.onRenderers(null, null, renderers);
  }
}
