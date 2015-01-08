package dk.dr.radio.afspilning.wrapper;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.IOException;

import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

/**
 * Wrapper til MediaPlayer.
 * Akamai kræver at MediaPlayer'en bliver 'wrappet' sådan at deres statistikmodul
 * kommer ind imellem den og resten af programmet.
 * Dette muliggør også fjernafspilning (a la AirPlay), da f.eks.
 * ChromeCast-understøttelse kræver præcist den samme wrapping.
 * Oprettet af  Jacob Nordfalk 25-03-14.
 */
public class AndroidMediaPlayerWrapper implements MediaPlayerWrapper {
  MediaPlayer mediaPlayer;

  public AndroidMediaPlayerWrapper(boolean opretMediaPlayer) {
    if (opretMediaPlayer) mediaPlayer = new MediaPlayer();
  }

  public AndroidMediaPlayerWrapper() {
    this(true);
  }


  private static int tæller;

  @Override
  public void setDataSource(String lydUrl) throws IOException {
    if (lydUrl.startsWith("file:")) {
      /* FIX: Det ser ud til at nogle telefonmodellers MediaPlayer har problemer
         med at åbne filer på SD-kortet hvis vi bare kalder setDataSource("file:///...
         Det er bl.a. set på:
         Telefonmodel: LT26i LT26i_1257-7813   - Android v4.1.2 (sdk: 16)
         Derfor bruger vi for en FileDescriptor i dette tilfælde
       */
      FileInputStream fis = new FileInputStream(Uri.parse(lydUrl).getPath());
      mediaPlayer.setDataSource(fis.getFD());
      fis.close();
      return;
    }

    mediaPlayer.setDataSource(lydUrl);
  }

  @Override
  public void setAudioStreamType(int streamMusic) {
    mediaPlayer.setAudioStreamType(streamMusic);
  }

  @Override
  public void prepare() throws IOException {
    mediaPlayer.prepare();
  }

  @Override
  public void stop() {
    mediaPlayer.stop();
  }

  @Override
  public void release() {
    mediaPlayer.release();
  }

  @Override
  public void seekTo(int offsetMs) {
    mediaPlayer.seekTo(offsetMs);
  }

  @Override
  public int getDuration() {
    return mediaPlayer.getDuration();
  }

  @Override
  public int getCurrentPosition() {
    return mediaPlayer.getCurrentPosition();
  }

  @Override
  public void start() {
    mediaPlayer.start();
  }

  @Override
  public void reset() {
    mediaPlayer.reset();
  }

  @Override
  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  @Override
  public void setVolume(float leftVolume, float rightVolume) {
    mediaPlayer.setVolume(leftVolume, rightVolume);
  }

  @Override
  public void setWakeMode(Context ctx, int screenDimWakeLock) {
    mediaPlayer.setWakeMode(ctx, screenDimWakeLock);
  }

  @Override
  public void setMediaPlayerLytter(MediaPlayerLytter lytter) {
    mediaPlayer.setOnCompletionListener(lytter);
    mediaPlayer.setOnCompletionListener(lytter);
    mediaPlayer.setOnErrorListener(lytter);
    mediaPlayer.setOnPreparedListener(lytter);
    mediaPlayer.setOnBufferingUpdateListener(lytter);
    mediaPlayer.setOnSeekCompleteListener(lytter);
  }

  private static Class<? extends MediaPlayerWrapper> mediaPlayerWrapperKlasse = null;

  public static MediaPlayerWrapper opret() {
    if (!App.PRODUKTION) mediaPlayerWrapperKlasse = null; // TODO skal nok fjernes igen
    if (mediaPlayerWrapperKlasse == null) {
      if (App.prefs.getBoolean("exoplayer", false)) {
        mediaPlayerWrapperKlasse = ExoPlayerWrapper.class;
      } else if (!App.prefs.getBoolean("Rapportér statistik", true)) {
        App.langToast("DR Radio indsamler ikke brugsstatisik. Rapportér venligst om det gør en forskel for dig MHT batteriforbrug.");
        App.langToast("Hvis du er sikker på at det medfører væsentligt længere batterilevetid, så kontakt os, så vi kan kigge på problemet.");
        mediaPlayerWrapperKlasse = AndroidMediaPlayerWrapper.class;
      } else {
        try {
          mediaPlayerWrapperKlasse = (Class<? extends MediaPlayerWrapper>) Class.forName("dk.dr.radio.afspilning.wrapper.AkamaiMediaPlayerWrapper");
        } catch (ClassNotFoundException e) {
          mediaPlayerWrapperKlasse = AndroidMediaPlayerWrapper.class;
          Log.e("Mangler Akamai-wrapper til statistik", e);
        }
      }
    }

    try {
      Log.d("MediaPlayerWrapper opret() " + mediaPlayerWrapperKlasse);
      return mediaPlayerWrapperKlasse.newInstance();
    } catch (Exception e) {
      Log.rapporterFejl(e);
    }
    return new AndroidMediaPlayerWrapper();
  }
}
