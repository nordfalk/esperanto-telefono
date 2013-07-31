package dk.nordfalk.esperanto.radio;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.dr.radio.util.Log;
import java.util.Date;

public class AppRater {
  private final static int DAYS_UNTIL_PROMPT = 3;
  private final static int LAUNCHES_UNTIL_PROMPT = 7;
  private final static String dontshowagain = "rate_dontshowagain";

  public static void app_launched(Context mContext, boolean novaInstalo, boolean launch) {
    SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
    if (prefs.getBoolean(dontshowagain, false)) {
      return;
    }

    SharedPreferences.Editor editor = prefs.edit();

    // Increment launch counter
    //long launch_count = prefs.getLong("launch_count", 0) + 1;
    long launch_count = prefs.getLong("launch_count", novaInstalo ? 0 : 7) + (launch ? 1 : 0);
    editor.putLong("launch_count", launch_count);

    // Get date of first launch
    Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
    if (date_firstLaunch == 0) {
      date_firstLaunch = System.currentTimeMillis();
      if (!novaInstalo) date_firstLaunch -= DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000 - 10000;
      editor.putLong("date_firstlaunch", date_firstLaunch);
    }
    Log.d("AppRater: novaInstalo=" + novaInstalo + " launch_count=" + launch_count + " date_firstLaunch=" + new Date(date_firstLaunch));



    // Wait at least n days before opening
    if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
      if (System.currentTimeMillis() >= date_firstLaunch
          + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
        showRateDialog(mContext, editor);
      }
    }

    editor.commit();
  }

  public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
    final Dialog dialog = new Dialog(mContext);
    final String APP_PNAME = mContext.getPackageName();

    dialog.setTitle(R.string.AppRater_taksu);

    LinearLayout ll = new LinearLayout(mContext);
    ll.setOrientation(LinearLayout.VERTICAL);

    TextView tv = new TextView(mContext);
    tv.setText(R.string.AppRater_teksto);
    tv.setWidth(240);
    tv.setPadding(4, 0, 4, 10);
    ll.addView(tv);

    Button b1 = new Button(mContext);
    b1.setText(R.string.AppRater_taksi);
    b1.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        editor.putBoolean(dontshowagain, true);
        editor.commit();
        dialog.dismiss();
        try {
          mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
        } catch (Exception e) {
          App.videblaEraro(null, e);
        }
      }
    });
    ll.addView(b1);
    /*
     b1 = new Button(mContext);
     b1.setText(R.string.AppRater_subteni);
     b1.setOnClickListener(new OnClickListener() {
     public void onClick(View v) {
     mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
     dialog.dismiss();
     }
     });
     ll.addView(b1);
     */
    Button b2 = new Button(mContext);
    b2.setText(R.string.AppRater_poste);
    b2.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        editor.putLong("date_firstlaunch", System.currentTimeMillis());
        editor.commit();
        dialog.dismiss();
      }
    });
    ll.addView(b2);

    Button b3 = new Button(mContext);
    b3.setText(R.string.AppRater_ne_dankon);
    b3.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        editor.putBoolean(dontshowagain, true);
        editor.commit();
        dialog.dismiss();
      }
    });
    ll.addView(b3);

    dialog.setContentView(ll);
    dialog.show();
  }
}
// see http://www.androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater