/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.nordfalk.esperanta_telefono;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

/**
 *
 * @author Jacob Nordfalk
 */
public class Instrukcioj_akt extends Activity {
  private WebView wv;

  public class ReVokoDeJavaskripto
  {
    public void lingvajAgordoj() {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_MAIN);
      ComponentName com = new ComponentName("com.android.settings", "com.android.settings.LanguageSettings");
      intent.setComponent(com);

      Toast.makeText(Instrukcioj_akt.this, "Iru malsupren kaj enŝaltu AnySoftKeyboard'on.", Toast.LENGTH_LONG).show();
      Toast.makeText(Instrukcioj_akt.this, "Post tio vi devas agordi AnySoftKeyboard'on kaj enŝalti Esperanton.", Toast.LENGTH_LONG).show();
      startActivity(intent);
    }

    public void elektiEniganMetodon() {
      try {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
      } catch (Exception e) {
        //Toast.makeText(Instrukcioj_akt.this, "Eraro okazis. Permane premu: ", Toast.LENGTH_LONG).show();
        //Toast.makeText(Instrukcioj_akt.this, "Hejmo-butonon, Menu-butonon,", Toast.LENGTH_LONG).show();
        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        Toast.makeText(Instrukcioj_akt.this, "Elektu 'Lingvoj' kaj 'Eniga metodo' kaj", Toast.LENGTH_LONG).show();
      }
      Toast.makeText(Instrukcioj_akt.this, "elektu AnySoftKeyboard'on.", Toast.LENGTH_LONG).show();
    }


    public void agordoj() {
      startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
    }


    public String androjdVersio() {
      return Build.VERSION.SDK;
    }



    public void kontakti() {
      String mesaĝo = "Skribu vian mesaĝon ĉi tie:\n\n\n---\n"
              +"\nModelo: "+Build.MODEL
              +"\n"+Build.PRODUCT
              +"\nAndrojda versio v"+Build.VERSION.RELEASE
              +"\nsdk: r"+Build.VERSION.SDK_INT;

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"jacob.nordfalk@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Komento pri 'Esperanta telefono'");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mesaĝo);
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle) {
      super.onCreate(icicle);

      wv = new WebView(this);
      wv.loadUrl("file:///android_asset/bonvenon.html");

      wv.getSettings().setJavaScriptEnabled(true);
      wv.addJavascriptInterface(new ReVokoDeJavaskripto(), "javo");

      final boolean novaKlavarElekto = Build.VERSION.SDK_INT >= 14;
      wv.setWebViewClient(null);

      wv.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
          wv.loadUrl("javascript:document.getElementById('malnovaKlavarElekto').style.display="
            +(novaKlavarElekto?"'none';":"'block';"));
          wv.loadUrl("javascript:document.getElementById('novaKlavarElekto').style.display="
            +(!novaKlavarElekto?"'none';":"'block';"));
        }
      });

      /*
      wv.loadUrl("javascript:document.getElementById('malnovaKlavarElekto').style.visibility="
          +(novaKlavarElekto?"'none';":"'block';"));
      wv.loadUrl("javascript:document.getElementById('novaKlavarElekto').style.visibility="
          +(!novaKlavarElekto?"'none';":"'block';"));
*/
      setContentView(wv);
  }

}
