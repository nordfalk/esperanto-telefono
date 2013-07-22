/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.esperanto.radio;

import android.app.Application;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import com.bugsense.trace.BugSenseHandler;
import dk.dr.radio.util.Log;

/**
 *
 * @author j
 */
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
      BugSenseHandler.initAndStartSession(this, "b8d4f0e8");

			try {
				PackageManager pm = getPackageManager();
				Datumoj.appInfo = pm.getPackageInfo(getPackageName(), 0);
			} catch (Exception e) {
				Log.kritiskFejlStille(e);
			}
      Datumoj.prefs = PreferenceManager.getDefaultSharedPreferences(this);

    
  }

}
