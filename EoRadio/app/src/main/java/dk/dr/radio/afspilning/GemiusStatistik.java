package dk.dr.radio.afspilning;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import dk.dr.radio.data.Grunddata;
import dk.dr.radio.data.Lydkilde;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.net.Diverse;

/**
 * Created by json on 09-07-14.
 */
class GemiusStatistik {

  static final String RAPPORTERINGSURL = "http://www.dr.dk/mu-online/api/1.0/reporting/gemius";
  private static final String NØGLE = "Gemius sporingsnøgle";


  private static SimpleDateFormat servertidsformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSZ", Locale.US); // "2014-07-09T09:54:32.086603Z" +01:00 springes over da kolon i +01:00 er ikke-standard Java
  private SharedPreferences prefs;
  private JSONObject json;
  private String sporingsnøgle;

  private ArrayList hændelser = new ArrayList();
  private Lydkilde lydkilde;

  GemiusStatistik() {
    String påkrævedeFelter =
        "{\"ScreenResolution\":{\"X\":1024,\"Y\":768}"
            + ",\"VideoResolution\":{\"X\":1024,\"Y\":768}"
            + ",\"ScreenColorDepth\":24"
            + ",\"PlayerEvents\":[{\"MaterialOffsetSeconds\":0,\"Started\":\"FirstPlay\",\"Created\":\"2014-07-09T09:54:32.086603Z\"}]"
            + ",\"ChannelType\":\"RADIO\""
            + ",\"Testmode\":\"true\""
//            +",\"Testmode\":"+!App.PRODUKTION
            + "}";
    try {
      json = new JSONObject(påkrævedeFelter);
      json.put("AutoStarted", false);
      //json.put("Platform", "dr.android." + App.versionsnavn);
//      json.put("Platform", "dr.android.KAN_MAN_SE_DETTE_ekstrafelter_vaek");
      json.put("Platform", "dr.android.test_fra_main");
//      json.put("Telefonmodel", Build.MODEL + " " + Build.PRODUCT);
//      json.put("Android_v", Build.VERSION.RELEASE);
/* Behøves, men har ingen meningsfulde værdier
    json.put("ScreenResolution", );
    json.put("ScreenResolution = new Resolution(1024, 768),
    json.put("VideoResolution = new Resolution(1024, 768),
    json.put("ScreenColorDepth = 24
*/
      if (App.instans != null) {
        prefs = App.instans.getSharedPreferences(NØGLE, 0);
        sporingsnøgle = prefs.getString(NØGLE, null);
        if (sporingsnøgle==null) {
          sporingsnøgle = App.prefs.getString(NØGLE, null);
          if (sporingsnøgle!=null) { // 28 nov 2014 - flyt data fra fælles prefs til separat fil - kan fjernes ultimo 2015
            App.prefs.edit().remove(NØGLE).commit();
            prefs.edit().putString(NØGLE, sporingsnøgle).commit();
          }
        }
        json.put("CorrelationId", sporingsnøgle);
        Display display = ((WindowManager) App.instans.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        json.put("ScreenResolution", new JSONObject().put("X", width).put("Y", height));
      }
    } catch (JSONException e) {
      Log.rapporterFejl(e);
      json = new JSONObject();
    }
  }


  void testSetlydkilde() throws JSONException {
    hændelser.clear();
    setLydkilde(null);

    registérHændelse(PlayerAction.FirstPlay, 0);
  }

  void setLydkilde(Lydkilde nyLydkilde) {
    Log.d("Gemius setLydkilde " + lydkilde);
    if (hændelser.size() > 0) {
      Log.d("Gemius setLydkilde, hov, havde ikke sendt disse hændelser: " + hændelser);
      startSendData();
    }
    if (nyLydkilde != null && lydkilde != nyLydkilde) {
      lydkilde = nyLydkilde;
      registérHændelse(PlayerAction.FirstPlay, 0);
    }
  }


  void startSendData() {
    if (hændelser.isEmpty()) return;
    if (App.ÆGTE_DR) try {
      // json.put("Url", "http://test.com");  // behøves ikke?
      // json.put("InitialLoadTime", 0);  // behøves ikke?
      // json.put("TimezoneOffsetInMinutes", -120);  // behøves ikke?
      json.put("PlayerEvents", new JSONArray(hændelser));
      if (lydkilde != null) {
        json.put("IsLiveStream", lydkilde.erDirekte());
        json.put("Id", findSlug(lydkilde.getUdsendelse(), lydkilde));
        json.put("Channel", findSlug(lydkilde.getKanal(), lydkilde));
      } else {
        if (App.instans != null) { // Hvis dette er en kørende app, så rapporter en fejl med dette
          Log.rapporterFejl(new IllegalStateException("Gemius lydkilde er null"));
        }
        json.put("IsLiveStream", false);
        json.put("Id", "matador-24-24");// kan ikke være "ukendt"
        json.put("Channel", "ukendt");
      }
    } catch (Exception e) {
      Log.rapporterFejl(e, "for " + lydkilde);
    }
    hændelser.clear();
    final String data = json.toString();
    if (App.fejlsøgning) Log.d("Gemius startSendData json=" + data);
    //new Exception().printStackTrace();

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject res = Diverse.postJson(RAPPORTERINGSURL, data);
          String nySporingsnøgle = res.optString("CorrelationId");
          if (nySporingsnøgle.length() > 0 && !nySporingsnøgle.equals(sporingsnøgle)) {
            json.put("CorrelationId", nySporingsnøgle);
            sporingsnøgle = nySporingsnøgle;
            if (prefs != null) prefs.edit().putString(NØGLE, sporingsnøgle).commit();
          }
          if (App.fejlsøgning) Log.d("Gemius res=" + res);
        } catch (IOException ioe) {
          Log.d("data json=" + data);
          Log.e(ioe);
        } catch (Exception e) {
          Log.rapporterFejl(e, "data json=" + data);
        }
      }
    }).start();
  }

  /**
   * Find slug ud fra en lydkilde
   * @param lk lydkilden
   * @param lk0 hvis den ikke kan findes ud fra lk, så prøv den her lydkilde
   * @return slug, eller "ukendt"
   */
  private String findSlug(Lydkilde lk, Lydkilde lk0) {
    if (lk==null || lk==Grunddata.ukendtKanal) return findSlug(lk0, null);
    if (lk.slug!=null && lk.slug.length()>0) return lk.slug;
    if (lk0 != null) return findSlug(lk0, null);
    return "ukendt";
  }


  static enum PlayerAction {
    FirstPlay,
    Play,
    Pause,
    Seeking,
    Completed,
    Quit,
    Stopped
  }

  void registérHændelse(PlayerAction hvad, long mediaOffsetISekunder) {
    try {
      JSONObject hændelse = new JSONObject();
      hændelse.put("Started", hvad.toString());
      hændelse.put("Created", servertidsformat.format(new Date())); // "2014-07-09T09:54:32.086603Z"
      hændelse.put("MaterialOffsetSeconds", mediaOffsetISekunder);
      Log.d("Gemius registérHændelse " + hændelse);
      hændelser.add(hændelse);
    } catch (Exception e) {
      Log.rapporterFejl(e);
    }
  }


  /**
   * Til afprøvning
   */
  public static void main(String[] a) throws Exception {
    GemiusStatistik g = new GemiusStatistik();
    for (int n = 0; n < 100; n++) {
      g.testSetlydkilde();
      g.registérHændelse(PlayerAction.Play, 0);
      Thread.sleep(1000);
      g.registérHændelse(PlayerAction.Completed, 2);
      g.startSendData();
      Thread.sleep(1000);
    }

  }

}
