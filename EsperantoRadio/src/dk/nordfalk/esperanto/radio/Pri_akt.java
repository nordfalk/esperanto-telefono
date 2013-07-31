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

import dk.nordfalk.esperanto.radio.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.Button;
import android.widget.ImageButton;
import dk.dr.radio.util.Kontakt;
import dk.dr.radio.util.Log;
import dk.dr.radio.util.MedieafspillerInfo;

public class Pri_akt extends Activity implements OnClickListener {
  WebView webview;
  private static final String EMAILSUBJECT = "Sugesto por Esperanto-radio por Androjd";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pri_akt);

    Datumoj drData;
    try {
      drData = Datumoj.kontroluInstanconSxargxita(this);
    } catch (Exception ex) {
      Log.e(ex);
      finish(); // Hop ud!
      return;
    }
    String aboutUrl = drData.stamdata.s("hejmpaĝo");

    webview = (WebView) findViewById(R.id.about_webview);

    // Jacob: Fix for 'syg' webview-cache - se http://code.google.com/p/android/issues/detail?id=10789
    WebViewDatabase webViewDB = WebViewDatabase.getInstance(this);
    if (webViewDB != null) {
      // OK, webviewet kan bruge sin cache
      webview.getSettings().setJavaScriptEnabled(true);
      webview.loadUrl(aboutUrl);
      // hjælper det her??? webview.getSettings().setDatabasePath(...);
    } else {
      // Øv, vi viser URLen i en ekstern browser.
      // Når brugeren derefter trykker 'tilbage' ser han et tomt webview.
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(aboutUrl)));
    }

    webview.setBackgroundColor(Color.parseColor("#333333"));

    Button sendFeedbackButton = (Button) findViewById(R.id.about_footer_button);

    sendFeedbackButton.setOnClickListener(this);
  }

  public void onClick(View v) {
    String brødtekst = "";
    brødtekst += Datumoj.instans.stamdata.s("feedback_brugerspørgsmål");
    brødtekst += "\n" + Datumoj.instans.ludado.kanalUrl;
    brødtekst += "\n\n" + new MedieafspillerInfo().lavTelefoninfo(Pri_akt.this);

    Kontakt.kontakt(this, EMAILSUBJECT, brødtekst, Log.log.toString());
  }
}
