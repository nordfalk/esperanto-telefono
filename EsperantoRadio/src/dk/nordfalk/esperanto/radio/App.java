/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.esperanto.radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import com.bugsense.trace.BugSenseHandler;
import dk.dr.radio.util.Kontakt;
import dk.dr.radio.util.Log;
import dk.dr.radio.util.MedieafspillerInfo;

/**
 *
 * @author j
 */
public class App extends Application {

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
        brødtekst += "\n\n" + new MedieafspillerInfo().lavTelefoninfo(akt);
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
				PackageManager pm = getPackageManager();
				Datumoj.appInfo = pm.getPackageInfo(getPackageName(), 0);
			} catch (Exception e) {
				eraro(e);
			}
      Datumoj.prefs = PreferenceManager.getDefaultSharedPreferences(this);

    
  }

}
