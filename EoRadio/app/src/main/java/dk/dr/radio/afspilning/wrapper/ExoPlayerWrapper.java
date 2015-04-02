package dk.dr.radio.afspilning.wrapper;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.ExoPlayerLibraryInfo;

import java.io.IOException;

import dk.dr.radio.afspilning.exoplayer.DefaultRendererBuilder;
import dk.dr.radio.afspilning.exoplayer.DemoPlayer;
import dk.dr.radio.afspilning.exoplayer.EventLogger;
import dk.dr.radio.afspilning.exoplayer.HlsRendererBuilder;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

/**
 * Wrapper til ExoPlayer.
 * @author Jacob Nordfalk 28-11-14.
 */
public class ExoPlayerWrapper implements MediaPlayerWrapper, DemoPlayer.Listener {

  DemoPlayer player;

  private EventLogger eventLogger;
  private MediaPlayerLytter lytter;
  private PowerManager.WakeLock mWakeLock = null;
  private boolean mStayAwake;

  @Override
  public void setDataSource(final String url) throws IOException {
    App.forgrundstråd.post(new Runnable() {
      @Override
      public void run() {
        String versionName;
        versionName = "ExoPlayerDemo/" + App.versionsnavn + " (Linux;Android " + Build.VERSION.RELEASE + ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
        if (url.endsWith("m3u8")) {
          player = new DemoPlayer(new HlsRendererBuilder(versionName, url, url));
          App.kortToast("HlsRendererBuilder\n" + url);
        } else {
          player = new DemoPlayer(new DefaultRendererBuilder(App.instans, Uri.parse(url), null));
          App.kortToast("DefaultRendererBuilder\n" + url);
        }
        player.addListener(ExoPlayerWrapper.this);
        player.seekTo(0);
        eventLogger = new EventLogger();
        eventLogger.startSession();
        player.addListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.setInternalErrorListener(eventLogger);
      }
    });
  }

  @Override
  public void setAudioStreamType(int streamMusic) {
    //TODO player.setAudioStreamType(streamMusic);
  }

  @Override
  public void prepare() throws IOException {
    App.forgrundstråd.post(new Runnable() {
      @Override
      public void run() {
        player.prepare();
        player.setPlayWhenReady(true);
      }
    });
  }

  @Override
  public void seekTo(int offsetMs) {
    if (player!=null) player.seekTo(offsetMs);
  }

  @Override
  public int getDuration() {
    if (player==null) return 0; // fix for https://mint.splunk.com/dashboard/project/cd78aa05/errors/3038148734
    return (int) player.getDuration();
  }

  @Override
  public int getCurrentPosition() {
    return (int) player.getCurrentPosition();
  }

  @Override
  public void start() {
    player.getPlayerControl().start();
    stayAwake(true);
  }


  @Override
  public void stop() {
    if (player!=null) player.getPlayerControl().pause();
    stayAwake(false);
  }

  @Override
  public void release() {
    player.release();
  }

  @Override
  public void reset() {
    //player.reset();
  }

  @Override
  public boolean isPlaying() {
    return player!=null && player.getPlaybackState()== ExoPlayer.STATE_READY;
  }

  @Override
  public void setVolume(float leftVolume, float rightVolume) {
    Log.e(new IllegalArgumentException("ikke implementeret"));
  }

  /**
   * Set the low-level power management behavior for this MediaPlayer.
   *
   * <p>This function has the MediaPlayer access the low-level power manager
   * service to control the device's power usage while playing is occurring.
   * The parameter is a combination of {@link android.os.PowerManager} wake flags.
   * Use of this method requires {@link android.Manifest.permission#WAKE_LOCK}
   * permission.
   * By default, no attempt is made to keep the device awake during playback.
   *
   * @param context the Context to use
   * @param mode    the power/wake mode to set
   * @see android.os.PowerManager
   */
  public void setWakeMode(Context context, int mode) {
    boolean washeld = false;
    if (mWakeLock != null) {
      if (mWakeLock.isHeld()) {
        washeld = true;
        mWakeLock.release();
      }
      mWakeLock = null;
    }

    PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(mode|PowerManager.ON_AFTER_RELEASE, "ExoPlayer");
    mWakeLock.setReferenceCounted(false);
    if (washeld) {
      mWakeLock.acquire();
    }
  }

  private void stayAwake(boolean awake) {
    if (mWakeLock != null) {
      if (awake && !mWakeLock.isHeld()) {
        mWakeLock.acquire();
      } else if (!awake && mWakeLock.isHeld()) {
        mWakeLock.release();
      }
    }
    mStayAwake = awake;
  }

  @Override
  public void setMediaPlayerLytter(MediaPlayerLytter lytter) {
    this.lytter = lytter;
  }

  @Override
  public void onStateChanged(boolean playWhenReady, int playbackState) {
    if (lytter==null) return;
    switch (playbackState) {
      case ExoPlayer.STATE_BUFFERING:
        lytter.onBufferingUpdate(null, player.getBufferedPercentage());
        break;
      case ExoPlayer.STATE_ENDED:
        lytter.onCompletion(null);
        break;
      case ExoPlayer.STATE_IDLE:
        lytter.onCompletion(null);
        //text += "idle";
        break;
      case ExoPlayer.STATE_PREPARING:
        lytter.onBufferingUpdate(null, 100);
        break;
      case ExoPlayer.STATE_READY:
        lytter.onPrepared(null);
        break;
      default:
        break;
    }
  }

  @Override
  public void onError(Exception e) {
    lytter.onError(null, 42, 42);
  }

  @Override
  public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {

  }
}
