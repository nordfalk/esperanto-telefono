/**
 Esperanto-radio por Androjd, farita de Jacob Nordfalk.
 Kelkaj partoj de la kodo originas de DR Radio 2 por Android, vidu
 http://code.google.com/p/dr-radio-android/

 Esperanto-radio por Androjd estas libera softvaro: vi povas redistribui
 ĝin kaj/aŭ modifi ĝin kiel oni anoncas en la licenco GNU Ĝenerala Publika
 Licenco (GPL) versio 2.

 Esperanto-radio por Androjd estas distribuita en la espero ke ĝi estos utila,
 sed SEN AJNA GARANTIO; sen eĉ la implica garantio de surmerkatigindeco aŭ
 taŭgeco por iu aparta celo.
 Vidu la GNU Ĝenerala Publika Licenco por pli da detaloj.

 Vi devus ricevi kopion de la GNU Ĝenerala Publika Licenco kune kun la
 programo. Se ne, vidu <http://www.gnu.org/licenses/>.
 */
package dk.dr.radio.afspilning;

import android.app.Activity;
import android.os.PowerManager.WakeLock;
import dk.dr.radio.diverse.Opkaldshaandtering;
import java.io.IOException;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.deskclock.AlarmAlertWakeLock;
import dk.dr.radio.diverse.AfspillerWidget;
import dk.nordfalk.esperanto.radio.R;
import dk.nordfalk.esperanto.radio.datumoj.Log;
import dk.nordfalk.esperanto.radio.App;
import dk.nordfalk.esperanto.radio.Datumoj;
import dk.nordfalk.esperanto.radio.Ludado_akt;
import java.util.ArrayList;

/**
 * Tidligere AfspillerService - afspilerdel
 * @author j
 */
public class Ludado implements OnPreparedListener, OnSeekCompleteListener,
    OnCompletionListener, OnInfoListener, OnErrorListener, OnBufferingUpdateListener {
  /** Bruges fra widget til at kommunikere med servicen */
  public static final int WIDGET_START_ELLER_STOP = 11;
  /** Afspillerens status - bruges også i broadcasts */
  public static final int STATUSO_NEKONATA = 0;
  public static final int STATUSO_HALTIS = 1;
  public static final int STATUSO_KONEKTAS = 2;
  public static final int STATUSO_LUDAS = 3;
  public int ludadstatuso = STATUSO_HALTIS;
  private MediaPlayer mediaPlayer;
  private List<AfspillerListener> observantoj = new ArrayList<AfspillerListener>();
  public String kanalNavn;
  public String kanalUrl;
  //private Udsendelse aktuelUdsendelse;

  private static void sætMediaPlayerLytter(MediaPlayer mediaPlayer, Ludado lytter) {
    mediaPlayer.setOnCompletionListener(lytter);
    mediaPlayer.setOnErrorListener(lytter);
    mediaPlayer.setOnInfoListener(lytter);
    mediaPlayer.setOnPreparedListener(lytter);
    mediaPlayer.setOnBufferingUpdateListener(lytter);
    mediaPlayer.setOnSeekCompleteListener(lytter);
    if (holdSkærmTændt && lytter != null) mediaPlayer.setWakeMode(Datumoj.appCtx, PowerManager.SCREEN_DIM_WAKE_LOCK);
  }
  private final Opkaldshaandtering opkaldshåndtering;
  private final TelephonyManager tm;
  private static boolean holdSkærmTændt;
  public boolean eraroSignifasBrui = false;
  public WakeLock wakeLock;
  private WifiManager.WifiLock wifilock = null;

  /** Forudsætter Datumoj er initialiseret */
  public Ludado() {
    mediaPlayer = new MediaPlayer();

    sætMediaPlayerLytter(mediaPlayer, this);
    ŝarĝuPreferojn();


    opkaldshåndtering = new Opkaldshaandtering(this);
    try {
      wifilock = ((WifiManager) Datumoj.appCtx.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "DR Radio");
      wifilock.setReferenceCounted(false);
    } catch (Exception e) {
      App.eraro(e);
    } // TODO fjern try/catch
    tm = (TelephonyManager) Datumoj.appCtx.getSystemService(Context.TELEPHONY_SERVICE);
    tm.listen(opkaldshåndtering, PhoneStateListener.LISTEN_CALL_STATE);
  }

  public static void ŝarĝuPreferojn() {
    // Indlæs gamle værdier så vi har nogle...
    // Fjernet. Skulle ikke være nødvendigt. Jacob 22/10-2011
    // kanalNavn = p.getString("kanalNavn", "P1");
    // kanalUrl = p.getString("kanalUrl", "rtsp://live-rtsp.dr.dk/rtplive/_definst_/Channel5_LQ.stream");

    // Xperia Play har brug for at holde skærmen tændt. Muligvis også andre....
    holdSkærmTændt = "R800i".equals(Build.MODEL);
    String ŜLOSILOholdSkærmTændt = "holdSkærmTændt";
    holdSkærmTændt = Datumoj.prefs.getBoolean(ŜLOSILOholdSkærmTændt, holdSkærmTændt);
    // Gem værdi hvis den ikke findes, sådan at indstillingsskærm viser det rigtige
    if (!Datumoj.prefs.contains(ŜLOSILOholdSkærmTændt)) Datumoj.prefs.edit().putBoolean(ŜLOSILOholdSkærmTændt, holdSkærmTændt).commit();
  }
  private int onErrorTæller;
  private long onErrorTællerNultid;

  public void startiLudadon() throws IOException {
    Log.d("startiLudadon(" + kanalUrl);

    eraroSignifasBrui = false;
    onErrorTæller = 0;
    onErrorTællerNultid = System.currentTimeMillis();

    if (ludadstatuso == STATUSO_HALTIS) {
      // Start afspillerservicen så programmet ikke bliver lukket
      // når det kører i baggrunden under afspilning
      Datumoj.appCtx.startService(new Intent(Datumoj.appCtx, AService.class).putExtra("kanalNavn", kanalNavn));
      if (Datumoj.prefs.getBoolean("wifilås", true) && wifilock != null) try {
          wifilock.acquire();
        } catch (Exception e) {
          App.eraro(e);
        } // TODO fjern try/catch
      startAfspilningIntern();

      // Skru op til 1/5 styrke hvis volumen er lavere end det
      minimumaLaŭteco(1);

    } else Log.d(" forkert status=" + ludadstatuso);
  }

  public static void minimumaLaŭteco(int minimumKvinono) {
    AudioManager audioManager = (AudioManager) Datumoj.appCtx.getSystemService(Context.AUDIO_SERVICE);
    int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    int nu = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    if (nu < minimumKvinono * max / 5) {
      audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, minimumKvinono * max / 5, AudioManager.FLAG_SHOW_UI);
    }
  }

  private void startAfspilningIntern() {
    if (kanalUrl == null) return;
    Log.d("Starter streaming fra " + kanalNavn);
    Log.d("mediaPlayer.setDataSource( " + kanalUrl);
    // mediaPlayer.setDataSource() bør kaldes fra en baggrundstråd da det kan ske
    // at den hænger under visse netværksforhold
    new Thread() {
      public void run() {
        Log.d("mediaPlayer.setDataSource() start");
        try {
          mediaPlayer.setDataSource(kanalUrl);
          Log.d("mediaPlayer.setDataSource() slut");
          mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
          mediaPlayer.prepareAsync();
        } catch (Exception ex) {
          App.eraro(ex);
          handler.post(new Runnable() {
            public void run() {
              stopAfspilning();
            }
          });
        }
      }
    }.start();

    ludadstatuso = STATUSO_KONEKTAS;
    sendOnAfspilningForbinder(-1);
    opdaterWidgets();
    handler.removeCallbacks(startAfspilningIntern);

    if (Datumoj.uziAnalytics()) {
      Datumoj.tracker.trackPageView(kanalNavn);
      Datumoj.tracker.trackPageView(kanalUrl);
      Datumoj.tracker.trackEvent(
          "Ludado", // Category
          "Komenco", // Action
          kanalNavn, // Label
          77);       // Value
    }
  }

  synchronized public void stopAfspilning() {
    Log.d("AfspillerService stopAfspilning");
    handler.removeCallbacks(startAfspilningIntern);
    // Da mediaPlayer.reset() erfaringsmæssigt kan hænge i dette tilfælde afregistrerer vi
    // alle lyttere og bruger en ny
    final MediaPlayer gammelMediaPlayer = mediaPlayer;
    sætMediaPlayerLytter(gammelMediaPlayer, null); // afregistrér alle lyttere
    new Thread() {
      @Override
      public void run() {
        try {
          gammelMediaPlayer.stop();
          Log.d("gammelMediaPlayer.release() start");
          gammelMediaPlayer.release();
          Log.d("gammelMediaPlayer.release() færdig");
        } catch (Exception e) {
          App.eraro(e);
        }
      }
    }.start();

    mediaPlayer = new MediaPlayer();
    sætMediaPlayerLytter(mediaPlayer, this); // registrér lyttere på den nye instans

    ludadstatuso = STATUSO_HALTIS;
    opdaterWidgets();

    // Stop afspillerservicen
    Datumoj.appCtx.stopService(new Intent(Datumoj.appCtx, AService.class));
    if (wifilock != null) try {
        wifilock.release();
      } catch (Exception e) {
        App.eraro(e);
      } // TODO fjern try/catch

    // Informer evt aktivitet der lytter
    for (AfspillerListener observatør : observantoj) {
      observatør.onAfspilningStoppet();
    }

    if (Datumoj.uziAnalytics()) {
      Datumoj.tracker.trackEvent(
          "Ludado", // Category
          "Fino", // Action
          kanalNavn, // Label
          77);       // Value
    }

    if (wakeLock != null) {
      wakeLock.release();
      wakeLock = null;
    }
  }

  public void addAfspillerListener(AfspillerListener lytter) {
    if (!observantoj.contains(lytter)) {
      observantoj.add(lytter);
      // Informer lytteren om aktuel status
      if (ludadstatuso == STATUSO_KONEKTAS) {
        lytter.onAfspilningForbinder(-1);
      } else if (ludadstatuso == STATUSO_HALTIS) {
        lytter.onAfspilningStoppet();
      } else {
        lytter.onAfspilningStartet();
      }
    }
  }

  public void removeAfspillerListener(AfspillerListener lytter) {
    observantoj.remove(lytter);
  }

  public void setKanalon(String navn, String url) {
    if (kanalNavn != null && kanalNavn.equals(navn) && kanalUrl != null && kanalUrl.equals(url)) return;
    kanalNavn = navn;
    kanalUrl = url;

    if ((ludadstatuso == STATUSO_LUDAS) || (ludadstatuso == STATUSO_KONEKTAS)) {
      stopAfspilning();
      try {
        startiLudadon();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    opdaterWidgets();
  }

  private void opdaterWidgets() {

    AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(Datumoj.appCtx);
    int[] appWidgetId = mAppWidgetManager.getAppWidgetIds(new ComponentName(Datumoj.appCtx, AfspillerWidget.class));

    for (int id : appWidgetId) {
      AfspillerWidget.opdaterUdseende(Datumoj.appCtx, mAppWidgetManager, id);
    }
  }

  public int getAfspillerstatus() {
    return ludadstatuso;
  }

  //
  //    TILBAGEKALD FRA MEDIAPLAYER
  //
  public void onPrepared(MediaPlayer mp) {
    Log.d("Ludado onPrepared");
    ludadstatuso = STATUSO_LUDAS; //No longer buffering
    if (observantoj != null) {
      opdaterWidgets();
      for (AfspillerListener observer : observantoj) {
        observer.onAfspilningStartet();
      }
    }
    if (wakeLock != null) {
      wakeLock.release();
      wakeLock = null;
    }
    eraroSignifasBrui = false;
    // Det ser ud til kaldet til start() kan tage lang tid på Android 4.1 Jelly Bean
    // (i hvert fald på Samsung Galaxy S III), så vi kalder det i baggrunden
    new Thread() {
      public void run() {
        Log.d("mediaPlayer.start()");
        mediaPlayer.start();
        Log.d("mediaPlayer.start() slut");
      }
    }.start();
  }

  public void onCompletion(MediaPlayer mp) {
    Log.d("Ludado onCompletion!");
    // Hvis forbindelsen mistes kommer der en onCompletion() og vi er derfor
    // nødt til at genstarte, medmindre brugeren trykkede stop
    if (ludadstatuso == STATUSO_LUDAS) {
      Log.d("Genstarter afspilning!");
      mediaPlayer.stop();
      // mediaPlayer.reset();
      // Da mediaPlayer.reset() erfaringsmæssigt kan hænge i dette tilfælde afregistrerer vi
      // alle lyttere og bruger en ny
      final MediaPlayer gammelMediaPlayer = mediaPlayer;
      sætMediaPlayerLytter(gammelMediaPlayer, null); // afregistrér alle lyttere
      new Thread() {
        public void run() {
          Log.d("gammelMediaPlayer.release() start");
          gammelMediaPlayer.release();
          Log.d("gammelMediaPlayer.release() færdig");
        }
      }.start();

      mediaPlayer = new MediaPlayer();
      sætMediaPlayerLytter(mediaPlayer, this); // registrér lyttere på den nye instans
      // Tenu telefonon veka por 3 sekundoj
      AlarmAlertWakeLock.createPartialWakeLock(Datumoj.appCtx).acquire(3000);

      startAfspilningIntern();
    } else {
    }
    if (wakeLock != null) {
      wakeLock.release();
      wakeLock = null;
    }
  }

  public boolean onInfo(MediaPlayer mp, int hvad, int extra) {
    //Log.d("onInfo(" + MedieafspillerInfo.infokodeTilStreng(hvad) + "(" + hvad + ") " + extra);
    Log.d("Ludado onInfo(" + hvad + ") " + extra);
    return true;
  }
  Handler handler = new Handler();
  Runnable startAfspilningIntern = new Runnable() {
    public void run() {
      try {
        startAfspilningIntern();
      } catch (Exception e) {
        App.eraro(e);
      }
    }
  };

  public boolean onError(MediaPlayer mp, int hvad, int extra) {
    //Log.d("onError(" + MedieafspillerInfo.fejlkodeTilStreng(hvad) + "(" + hvad + ") " + extra+ " onErrorTæller="+onErrorTæller);
    Log.d("Ludado onError(" + hvad + ") " + extra + " onErrorTæller=" + onErrorTæller);

    if (Build.VERSION.SDK_INT >= 16 && hvad == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
      // Ignorer, da Samsung Galaxy SIII sender denne fejl (onError(1) -110) men i øvrigt spiller fint videre!
      return true;
    }

    // Iflg http://developer.android.com/guide/topics/media/index.html :
    // "It's important to remember that when an error occurs, the MediaPlayer moves to the Error
    //  state and you must reset it before you can use it again."
    if (ludadstatuso == STATUSO_LUDAS || ludadstatuso == STATUSO_KONEKTAS) {


      // Hvis der har været
      // 1) færre end 10 fejl eller
      // 2) der højest er 1 fejl pr 20 sekunder så prøv igen
      long dt = System.currentTimeMillis() - onErrorTællerNultid;

      if (onErrorTæller++ < 10 || (dt / onErrorTæller > 20000)) {
        mediaPlayer.stop();
        mediaPlayer.reset();

        // Vi venter længere og længere tid her
        int n = onErrorTæller;
        if (n > 11) n = 11;
        int ventetid = 10 + 5 * (1 << n); // fra n=0:10 msek til n=10:5 sek   til max n=11:10 sek
        Log.d("Ventetid før vi prøver igen: " + ventetid + "  n=" + n + " " + onErrorTæller);
        handler.postDelayed(startAfspilningIntern, ventetid);


        if (eraroSignifasBrui) {
          vibru(1000);
        }

      } else {
        stopAfspilning(); // Vi giver op efter 10. forsøg
        Toast.makeText(Datumoj.appCtx, "Bedaŭrinde ne eblas ludi tiun ĉi elsendon", Toast.LENGTH_LONG).show();
        Toast.makeText(Datumoj.appCtx, "Provu elekti alian kanalon", Toast.LENGTH_LONG).show();

        if (eraroSignifasBrui) {
          Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
          if (alert == null) {
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {  // I can't see this ever being null (as always have a default notification) but just incase
              // alert backup is null, using 2nd backup
              alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
          }
          kanalUrl = alert.toString();
          handler.postDelayed(startAfspilningIntern, 100);
          vibru(4000);
        }
      }
    } else {
      mediaPlayer.reset();
    }
    return true;
  }

  private void vibru(int ms) {
    Log.d("vibru " + ms);
    try {
      Vibrator vibrator = (Vibrator) Datumoj.appCtx.getSystemService(Activity.VIBRATOR_SERVICE);
      vibrator.vibrate(ms);
      // Tenu telefonon veka por 1/2a sekundo
      AlarmAlertWakeLock.createPartialWakeLock(Datumoj.appCtx).acquire(500);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendOnAfspilningForbinder(int procent) {
    for (AfspillerListener observer : observantoj) {
      observer.onAfspilningForbinder(procent);
    }
  }

  public void onBufferingUpdate(MediaPlayer mp, int procent) {
    Log.d("buf " + procent);
    if (procent < -100) procent = -1; // Ignorér vilde tal

    sendOnAfspilningForbinder(procent);
  }

  public void onSeekComplete(MediaPlayer mp) {
    Log.d("Ludado onSeekComplete");
  }

  public void lukNed() {
    stopAfspilning();
    tm.listen(opkaldshåndtering, PhoneStateListener.LISTEN_NONE);
  }
}
