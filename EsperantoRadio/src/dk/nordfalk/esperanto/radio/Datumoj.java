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
package dk.nordfalk.esperanto.radio;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.bugsense.trace.BugSense;
import com.bugsense.trace.BugSenseHandler;
import com.google.ads.a;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import dk.dr.radio.afspilning.Ludado;
import eo.radio.datumoj.Log;
import eo.radio.datumoj.Elsendo;
import eo.radio.datumoj.Kanalo;
import eo.radio.datumoj.Kasxejo;
import eo.radio.datumoj.Cxefdatumoj;
import eo.radio.datumoj.RssParsado;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONException;

/**
 * Det centrale objekt som alt andet bruger til
 */
public class Datumoj {
  public static final int stamdataID = 8;
  private static final String ŜLOSILO_KANALOJ = "esperantoradio_kanaloj_v" + stamdataID;
  private static final String kanalojUrl = "http://javabog.dk/privat/" + ŜLOSILO_KANALOJ + ".json";
  //private static String elsendojUrl = "http://esperanto-radio.com/radio.txt";
  private static final String ŜLOSILO_ELSENDOJ = "elsendoj";
  /** Globalt flag */
  public static final boolean evoluiganto = false;

  private void sætKanalOgUdsendelseSikkert(String kodo) {
    aktualaKanalkodo = kodo;
    aktualaKanalo = stamdata.kanalkodoAlKanalo.get(aktualaKanalkodo);

    if (aktualaKanalo == null || aktualaKanalo.elsendoj.size() == 0) { // Ne devus okazi, sed tamen okazas se oni neniam ajn elektis kanalon
      aktualaKanalo = stamdata.kanaloj.get(0);
      aktualaKanalkodo = aktualaKanalo.kodo;
    }
    // Ĉiam elektu la plej lastan elsendon
    aktualaElsendo = aktualaKanalo.elsendoj.get(aktualaKanalo.elsendoj.size() - 1);
  }
  public Ludado ludado;
  public Handler handler = new Handler();
  public boolean udsendelser_ikkeTilgængeligt;
  public static Datumoj instanco;
  public String rektaElsendaPriskribo;
  public Cxefdatumoj stamdata;
  public String aktualaKanalkodo;
  public Kanalo aktualaKanalo;
  public Elsendo aktualaElsendo;
  public static final String ŜLOSILO_kanalo = "kanalo";
  /** Bruges til at sende broadcasts om nye stamdata */
  public static final String INTENT_novaj_ĉefdatumoj = "dk.dr.radio.afspiller.OPDATERING_Stamdata";
  /** Bruges til at sende broadcasts om ny info om udsendelsen (programinfo) */
  public static final String INTENT_novaj_elsendoj = "dk.dr.radio.afspiller.OPDATERING_Udsendelse";
  /** Bruges til at sende broadcasts om ny info om hvad der spiller nu  */
  public static final String OPDATERINGSINTENT_SpillerNuListe = "dk.dr.radio.afspiller.OPDATERING_SpillerNuListe";
  /** Hvis true er indlæsning i gang og der skal vises en venteskærm.
   * Man kan vente på et broadcast eller kalde wait() for at blive vækket når indlæsning er færdig
   */
  public boolean indlæserVentVenligst = false;
  //
  // Opdateringer i baggrunden.
  //
  private boolean baggrundsopdateringAktiv = false;
  private boolean baggrundstrådSkalVente = true;

  static void ŝarĝiInstancon() throws IOException, JSONException, PackageManager.NameNotFoundException {
    long komenco = System.currentTimeMillis();
    if (evoluiganto) Toast.makeText(App.app, "Programo freŝe startita", Toast.LENGTH_LONG).show();

    //if (evoluiganto) Debug.startMethodTracing("/data/data/dk.nordfalk.esperanto.radio/files/trace.data");


    int stamdataResId = App.app.getResources().getIdentifier(ŜLOSILO_KANALOJ, "raw", App.app.getPackageName());
    if (stamdataResId == 0) throw new InternalError("Ne trovita: " + ŜLOSILO_KANALOJ);

    String kanalojStr = App.prefs.getString(ŜLOSILO_KANALOJ, null);
    String elsendojStr = App.prefs.getString(ŜLOSILO_ELSENDOJ, null);

    if (kanalojStr == null) {
      // Indlæs fra raw this vi ikke har nogle cachede stamdata i prefs
      //InputStream is = akt.getResources().openRawResource(R.raw.stamdata_android22);
      kanalojStr = Utilajxoj.læsInputStreamSomStreng(App.app.getResources().openRawResource(stamdataResId));
    }

    if (elsendojStr == null) {
      // Indlæs fra raw this vi ikke har nogle cachede stamdata i prefs
      //InputStream is = akt.getResources().openRawResource(R.raw.stamdata_android22);
      elsendojStr = Utilajxoj.læsInputStreamSomStreng(App.app.getResources().openRawResource(R.raw.radio));
    }
    Log.d((System.currentTimeMillis() - komenco) + " akiris datumojn ");


    instanco = new Datumoj();
    instanco.stamdata = new Cxefdatumoj(kanalojStr);
    instanco.stamdata.leguElsendojn(elsendojStr);
    Log.d((System.currentTimeMillis() - komenco) + " parsis datumojn ");
    sxargxiKanalbildojn(instanco.stamdata, true);
    Log.d((System.currentTimeMillis() - komenco) + " ŝarĝis kanalbildojn ");
    // Daŭras tro da tempo! Ne faru en la ĉefa fadeno!
    Log.d(instanco.stamdata.kanaloj);

    // Kanalvalg. Tjek først Preferences, brug derefter JSON-filens forvalgte kanal
    // Por nun 'Muzaiko' estu cxiam la antauxelektita kanalo
    //if (instans.aktualaKanalkodo == null) instans.aktualaKanalkodo = prefs.getString(ŜLOSILO_kanalo, null);
    if (instanco.aktualaKanalkodo == null) instanco.aktualaKanalkodo = instanco.stamdata.json.optString("komenca_kanalo");
    instanco.sætKanalOgUdsendelseSikkert(instanco.aktualaKanalkodo);


    instanco.ludado = new Ludado();
    instanco.ludado.setKanalon(instanco.aktualaKanalo.nomo, instanco.aktualaElsendo.sonoUrl);
    //if (evoluiganto) Debug.stopMethodTracing();
    Log.d((System.currentTimeMillis() - komenco) + " finis ŝargadon");


    // 31. okt: Fjernet af Jacob - da baggrundstråden ikke skal startes af f.eks. widgetter
    // se kontroluFonaFadenoStartis()
    //if (!instans.fonaFadeno.isAlive()) instans.fonaFadeno.start();
  }

  /**
   * Først efter indlæstning starter vi baggrundstråden - fra splash og fra afspiller_akt.
   * Dette er et separat skridt da det ikke skal ske ved opstart af levende ikon
   */
  public void kontroluFonaFadenoStartis() {
    if (!fonaFadeno.isAlive()) fonaFadeno.start();
  }

  public void ŝanĝiKanalon(String novaKanalkodo) {
    Log.d("DRData.skiftKanal(" + novaKanalkodo);

    sætKanalOgUdsendelseSikkert(novaKanalkodo);

    App.prefs.edit().putString(ŜLOSILO_kanalo, aktualaKanalkodo).commit();
    rektaElsendaPriskribo = null;
    // Væk baggrundstråden så den indlæser den nye kanals elsendo etc og laver broadcasts med nyt info
    baggrundstrådSkalOpdatereNu();
  }

  public void setBaggrundsopdateringAktiv(boolean aktiv) {
    if (baggrundsopdateringAktiv == aktiv) return;

    baggrundsopdateringAktiv = aktiv;

    Log.d("setBaggrundsopdateringAktiv( " + aktiv);

    if (baggrundsopdateringAktiv) baggrundstrådSkalOpdatereNu(); // væk baggrundtråd
  }

  private void baggrundstrådSkalOpdatereNu() {
    baggrundstrådSkalVente = false;
    synchronized (fonaFadeno) {
      fonaFadeno.notify();
    }
  }
  final Thread fonaFadeno = new Thread() {
    @Override
    public void run() {

      boolean ioEstisSxargxita = sxargxiKanalbildojn(stamdata, false);
      ioEstisSxargxita |= sxargxiElsendojnDeRss(stamdata, false);

      if (ioEstisSxargxita) {
        App.app.sendBroadcast(new Intent(INTENT_novaj_ĉefdatumoj));
      }

      // Hovedløkke
      while (true) {
        try {
          if (baggrundstrådSkalVente) synchronized (fonaFadeno) {
              if (baggrundsopdateringAktiv)
                fonaFadeno.wait(15000); // Vent 15 sekunder. Men vågn op hvis nogen kalder fonaFadeno.notify()!

              // baggrundsopdateringAktiv kan være sat til false inden for de sidste 15 sekunder og så skal vi vente videre
              if (!baggrundsopdateringAktiv)
                fonaFadeno.wait(); // Vent indtil tråden vækkes

              fonaFadeno.wait(50); // Vent kort så den aktiverende tråd kan gøre sit arbejde færdigt
            }
          baggrundstrådSkalVente = true;

          hentUdsendelserOgSpillerNuListe();

          App.tracker.dispatch();

        } catch (Exception ex) {
          Log.e(ex);
        }
      }
    }
  };

  {
    fonaFadeno.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
  } // malalta prioritato

  private void hentUdsendelserOgSpillerNuListe() {
    Log.d("hentUdsendelserOgSpillerNuListe(" + aktualaKanalkodo);


    if (aktualaElsendo.rektaElsendaPriskriboUrl != null) try {
        // Muzaiko
        rektaElsendaPriskribo = Utilajxoj.hentUrlSomStreng(aktualaElsendo.rektaElsendaPriskriboUrl);
        if (rektaElsendaPriskribo.toLowerCase().contains("<html>")) {
          // La rektaElsendaPriskriboUrl ne devus enhavi <html>-kodojn. Se gxi havas, tiam
          // versxajne estas iu 'hotspot' kiu kaptis la adreson kaj kiu
          // sendas ensalutan pagxon
          rektaElsendaPriskribo = "Ne povis elŝuti";
        }
        App.app.sendBroadcast(new Intent(INTENT_novaj_elsendoj));
      } catch (Exception ex) {
        Log.e(ex);
        rektaElsendaPriskribo = "Ne povis elŝuti";
      }
    else {
      rektaElsendaPriskribo = null;
    }

    // Tjek om en evt ny udgave af stamdata skal indlæses
    final String STAMDATA_SIDST_INDLÆST = "stamdata_sidst_indlæst";
    long sidst = App.prefs.getLong(STAMDATA_SIDST_INDLÆST, 0);
    long nu = System.currentTimeMillis();
    long alder = (nu - sidst) / 1000 / 60;
    if (alder >= 30) try { // stamdata er ældre end en halv time
        Log.d("Stamdata er " + alder + " minutter gamle, opdaterer dem...");
        // Opdater tid (hvad enten indlæsning lykkes eller ej)
        App.prefs.edit().putLong(STAMDATA_SIDST_INDLÆST, nu).commit();

        String kanalojStr = Utilajxoj.hentUrlSomStreng(kanalojUrl);
        final Cxefdatumoj stamdata2 = new Cxefdatumoj(kanalojStr);
        // Hentning og parsning gik godt - vi gemmer den nye udgave i prefs
        App.prefs.edit().putString(ŜLOSILO_KANALOJ, kanalojStr).commit();

        try {
          String elsendojStr = Utilajxoj.hentUrlSomStreng(stamdata2.elsendojUrl);
          stamdata2.leguElsendojn(elsendojStr);
          // Hentning og parsning gik godt - vi gemmer den nye udgave i prefs
          App.prefs.edit().putString(ŜLOSILO_ELSENDOJ, elsendojStr).commit();
        } catch (Exception e) {
          Log.e("Fejl parsning af " + stamdata2.elsendojUrl, e);
        }
        sxargxiKanalbildojn(stamdata2, false);
        sxargxiElsendojnDeRss(stamdata2, false);
        Log.d(instanco.stamdata.kanaloj);

        handler.post(new Runnable() {
          public void run() {
            stamdata = stamdata2;
            App.app.sendBroadcast(new Intent(INTENT_novaj_ĉefdatumoj));
          }
        });
      } catch (Exception e) {
        Log.e("Fejl parsning af stamdata. Url=" + kanalojUrl, e);
      }

  }

  private static boolean sxargxiKanalbildojn(final Cxefdatumoj dat, boolean nurLokajn) {
    boolean ioEstisSxargxita = false;
    for (Kanalo k : dat.kanaloj) {
      String emblemoUrl = k.json.optString("emblemoUrl", "");
      if (emblemoUrl.length() > 0 && k.emblemo == null) try {
          String dosiero = Kasxejo.akiriDosieron(emblemoUrl, true, nurLokajn);
          if (dosiero == null) continue;
          /*
           int kiomDaDpAlta = 50; // 50 dp
           // Convert the dps to pixels
           final float scale = appCtx.getResources().getDisplayMetrics().density;
           int alteco = (int) (kiomDaDpAlta * scale + 0.5f);
           Bitmap res = kreuBitmapTiomAlta(dosiero, alteco);
           */
          Bitmap res = BitmapFactory.decodeFile(dosiero);

          if (res != null) ioEstisSxargxita = true;
          k.emblemo = res;
        } catch (Exception ex) {
          Log.e(ex);
        }
    }
    return ioEstisSxargxita;
  }

  private static boolean sxargxiElsendojnDeRss(final Cxefdatumoj dat, boolean nurLokajn) {
    boolean ioEstisSxargxita = false;
    long komenco = System.currentTimeMillis();
    for (Kanalo k : dat.kanaloj) {
      String elsendojRssUrl = k.json.optString("elsendojRssUrl", null);
      if (elsendojRssUrl != null) try {
          String dosiero = Kasxejo.akiriDosieron(elsendojRssUrl, false, nurLokajn);
          Log.d((System.currentTimeMillis() - komenco) + " akiris " + elsendojRssUrl);
          if (dosiero == null) continue;
          ArrayList<Elsendo> elsendoj = RssParsado.parsuElsendojnDeRss(new FileInputStream(dosiero));
          // Kelkaj RSS-fluoj havas nur la daton en la titolo. Tio estas jam videbla kaj tial ni ne montru tion
          if (k.json.optBoolean("elsendojRssIgnoruTitolon", false)) for (Elsendo e : elsendoj) e.titolo = null;
          if (elsendoj.size() > 0) {
            if (k.rektaElsendo != null) elsendoj.add(k.rektaElsendo);
            k.elsendoj = elsendoj;
            ioEstisSxargxita = true;
          }
          Log.d((System.currentTimeMillis() - komenco) + " parsis " + elsendojRssUrl + " kaj ricevis " + elsendoj.size() + " elsendojn");
        } catch (Exception ex) {
          Log.e("Eraro parsante " + elsendojRssUrl, ex);
        }
    }
    return ioEstisSxargxita;
  }


  /*
   private static Bitmap kreuBitmapTiomAlta(String dosiero, int alteco) {
   Options options = new BitmapFactory.Options();
   options.inScaled = false;
   options.inDither = false;
   options.inJustDecodeBounds = true;
   BitmapFactory.decodeFile(dosiero, options);
   int desiredH = 50;
   int srcWidth = options.outWidth;
   int srcHeight = options.outHeight;
   // Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
   if (desiredH > srcHeight) {
   desiredH = srcHeight;
   }
   // Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
   // from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
   int inSampleSize = 1;
   while (srcHeight / 2 > desiredH) {
   srcHeight /= 2;
   srcHeight /= 2;
   inSampleSize *= 2;
   }
   //float desiredScale = (float) desiredH / srcHeight;
   options.inJustDecodeBounds = false;
   options.inDither = false;
   options.inSampleSize = inSampleSize;
   options.inScaled = false;
   options.inPreferredConfig = Bitmap.Config.ARGB_8888;
   Bitmap res = BitmapFactory.decodeFile(dosiero, options);
   return res;
   }
   */

  /* malnova - forigu
   public String findKanalUrlFraKode(Kanalo kanal) {

   String url = kanal.rektaElsendaSonoUrl;
   if (aktualaElsendo != null && aktualaElsendo.sonoUrl!=null) {
   url = aktualaElsendo.sonoUrl;
   }
   String info = "Kanal: "+kanal.nomo+"\n"+url;
   if (Datumoj.evoluiganto) Toast.makeText(appCtx, info, Toast.LENGTH_LONG).show();
   Log.d(info);
   return url;
   }
   */
  public void kundividi(Activity akt) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Esperanto-radio por Androjd");
    sendIntent.putExtra(Intent.EXTRA_TEXT,
        "Saluton!\n\n"
        + "Mi rekomendas ke vi elprovas tiun ĉi programon per via Androjda telefono:\n"
        + "La Esperanto-radio de Muzaiko\n"
        + "https://market.android.com/details?id=dk.nordfalk.esperanto.radio\n"
        + "\n"
        + "Muzaiko estas Esperanto-radio kiu konstante elsendas.\n"
        + "Eblas ankaŭ aŭskulti la lastatempajn elsendojn de deko da aliaj radistacioj." //			+"\n\n(kaj... ne forgesu meti 5 stelojn :-)"
        );
    sendIntent.setType("text/plain");
    akt.startActivity(Intent.createChooser(sendIntent, "Sendi al"));
  }
}
