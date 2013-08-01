/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.esperanto.radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import dk.dr.radio.util.Kontakt;
import eo.radio.datumoj.Log;
import eo.radio.datumoj.Kasxejo;

/**
 *
 * @author j
 */
public class App extends Application {
  public static App app;
  public static SharedPreferences prefs;
  public static GoogleAnalyticsTracker tracker;
  public static PackageInfo appInfo;

  public static boolean uziAnalytics() {
    return App.prefs.getBoolean("analytics", true);
  }

  public static void eraro(final Exception e) {
    BugSenseHandler.sendException(e);
    Log.e(e);
  }

  public static void videblaEraro(final Activity akt, final Exception e) {
    BugSenseHandler.sendException(e);
    Log.e(e);
    AlertDialog.Builder ab = new AlertDialog.Builder(akt);
    ab.setTitle("Beda\u016drinde okazis eraro");
    ab.setMessage(e.toString());
    ab.setNegativeButton("Ignori", null);
    ab.setPositiveButton("Raporti", new Dialog.OnClickListener() {
      public void onClick(DialogInterface arg0, int arg1) {
        String brødtekst = "Skribu kio okazis:\n\n\n---\n";
        brødtekst += "\nErarsxpuro;\n" + android.util.Log.getStackTraceString(e);
        brødtekst += "\n\n" + App.app.lavTelefoninfo();
        Kontakt.kontakt(akt, "Eraro en EsperantoRadio", brødtekst, Log.log.toString());
      }
    });
    ab.create().show();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    BugSenseHandler.initAndStartSession(this, "b8d4f0e8");

    try {
      app = this;
      PackageManager pm = getPackageManager();
      appInfo = pm.getPackageInfo(getPackageName(), 0);
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
      /*
       // XXX TODO traktu tion ĉi
       Locale locale = new Locale("en");
       Locale.setDefault(locale);
       Configuration config = new Configuration();
       config.locale = locale;
       //appCtx.getResources().updateConfiguration(config, getResources().getDisplayMetrics());
       appCtx.getResources().getConfiguration().updateFrom(config);
       akt.getResources().getConfiguration().updateFrom(config);
       */


      //evoluiganto = prefs.getBoolean("udvikler", false);
      Kasxejo.init(getCacheDir().getPath());
      Datumoj.ŝarĝiInstancon();

      App.tracker = GoogleAnalyticsTracker.getInstance();
      App.tracker.startNewSession("UA-29361423-1", App.app);
      App.tracker.setProductVersion(App.appInfo.versionName, "" + Datumoj.instanco.stamdataID);

      if (App.uziAnalytics()) {
        App.tracker.trackPageView("starto:" + App.appInfo.versionName);

        boolean montriReklamojn = App.prefs.getBoolean(MontriReklamojn.ŜLOSILO_montri_reklamojn, false);
        App.tracker.trackPageView("montriReklamojn:" + montriReklamojn);
      }

    } catch (Exception e) {
      eraro(e);
    }
  }

  public static boolean isUriAvailable(Context context, Intent intent) {
    return context.getPackageManager().resolveActivity(intent, 0) != null;
  }

  public String lavTelefoninfo() {

    String ret = "Program: " + getPackageName() + " version " + appInfo.versionName
        + "\nTelefonmodel: " + Build.MODEL + " " + Build.PRODUCT
        + "\nAndroid v" + Build.VERSION.RELEASE
        + "\nsdk: " + Build.VERSION.SDK
        + "\nAndroid_ID: " + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    return ret;
  }
}
