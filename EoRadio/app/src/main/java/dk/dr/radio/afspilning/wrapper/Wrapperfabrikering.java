package dk.dr.radio.afspilning.wrapper;

import android.os.Build;

import dk.dr.radio.data.DRData;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

/**
 * Created by j on 14-11-15.
 */
public class Wrapperfabrikering {


  private static Class<? extends MediaPlayerWrapper> mediaPlayerWrapperKlasse = null;
  enum Hvilken { GammelMediaPlayer, NyExoPlayer, NyEmaPlayer };
  private static Hvilken hvilkenSidst;

  public static MediaPlayerWrapper opret() {
    if (mediaPlayerWrapperKlasse == null) {
      boolean rapporter = App.prefs.getBoolean("Rapportér statistik", true);
      if (!rapporter) {
        App.langToast("DR Radio indsamler ikke brugsstatisik. Rapportér venligst om det gør en forskel for dig MHT batteriforbrug.");
        App.langToast("Hvis du er sikker på at det medfører væsentligt længere batterilevetid, så kontakt os, så vi kan kigge på problemet.");
      }
      if (App.EMULATOR || !App.ÆGTE_DR) rapporter = false;

      // A/B/C test - Vælg en tilfældig
      Hvilken hvilken = Hvilken.values()[(int) (Math.random()*Hvilken.values().length)];
      if (hvilkenSidst==hvilken) { // Det er lidt kedeligt med den samme, prøv igen
        hvilken = Hvilken.values()[(int) (Math.random()*Hvilken.values().length)];
      }

      //boolean exoplayer = App.PRODUKTION||!App.ÆGTE_DR ? false : Math.random()>0.5;
      hvilken = Hvilken.NyEmaPlayer;
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) hvilken = Hvilken.GammelMediaPlayer;

      hvilkenSidst = hvilken;
      if (App.prefs.getBoolean("tving_exoplayer", DRData.instans.grunddata.tving_exoplayer)) hvilken = Hvilken.NyExoPlayer;
      if (App.prefs.getBoolean("tving_mediaplayer", DRData.instans.grunddata.tving_mediaplayer)) hvilken = Hvilken.GammelMediaPlayer;
      if (App.prefs.getBoolean("tving_emaplayer", DRData.instans.grunddata.tving_emaplayer)) hvilken = Hvilken.NyEmaPlayer;

      if (hvilken==Hvilken.NyExoPlayer) {
        try {
          if (!rapporter)
            mediaPlayerWrapperKlasse = ExoPlayerWrapper.class;
          else
            mediaPlayerWrapperKlasse = (Class<? extends MediaPlayerWrapper>) Class.forName("dk.dr.radio.afspilning.wrapper.AkamaiExoPlayerWrapper");
        } catch (Exception e) {
          e.printStackTrace();
          mediaPlayerWrapperKlasse = ExoPlayerWrapper.class;
          if (App.ÆGTE_DR) Log.e("Mangler Akamai-wrapper til statistik", e);
        }
      } else if (hvilken==Hvilken.NyEmaPlayer) {
        try {
          if (!rapporter)
            mediaPlayerWrapperKlasse = EmaPlayerWrapper.class;
          else
            mediaPlayerWrapperKlasse = (Class<? extends MediaPlayerWrapper>) Class.forName("dk.dr.radio.afspilning.wrapper.AkamaiEmaPlayerWrapper");
        } catch (Exception e) {
          e.printStackTrace();
          mediaPlayerWrapperKlasse = ExoPlayerWrapper.class;
          if (App.ÆGTE_DR) Log.e("Mangler Akamai-wrapper til statistik", e);
        }
      } else {
        try {
          if (!rapporter)
            mediaPlayerWrapperKlasse = AndroidMediaPlayerWrapper.class;
          else
            mediaPlayerWrapperKlasse = (Class<? extends MediaPlayerWrapper>) Class.forName("dk.dr.radio.afspilning.wrapper.AkamaiMediaPlayerWrapper");
        } catch (ClassNotFoundException e) {
          mediaPlayerWrapperKlasse = AndroidMediaPlayerWrapper.class;
          if (App.ÆGTE_DR) Log.e("Mangler Akamai-wrapper til statistik", e);
        }
      }
      if (App.fejlsøgning) App.kortToast(mediaPlayerWrapperKlasse.getSimpleName());
    }
    try {
      Log.d("MediaPlayerWrapper opret() " + mediaPlayerWrapperKlasse);
      return mediaPlayerWrapperKlasse.newInstance();
    } catch (Exception e) {
      Log.rapporterFejl(e);
    }
    return new AndroidMediaPlayerWrapper();
  }

  public static void nulstilWrapper() {
    if (App.fejlsøgning) App.kortToast(("Fjerner wrapper\n"+mediaPlayerWrapperKlasse).replaceAll("dk.dr.radio.afspilning.wrapper.",""));
    mediaPlayerWrapperKlasse = null;
  }

}
