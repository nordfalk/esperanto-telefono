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
package dk.nordfalk.esperanto.radio.datumoj;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilajxoj {
  private static HttpClient httpClient;

  public static String hentUrlSomStreng(String url) throws IOException {
    // AndroidHttpClient er først defineret fra Android 2.2
    //if (httpClient == null) httpClient = android.net.http.AndroidHttpClient.newInstance("Android DRRadio");
    if (httpClient == null) {
      HttpParams params = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(params, 15 * 1000);
      HttpConnectionParams.setSoTimeout(params, 15 * 1000);
      HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
      HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
      //HttpProtocolParams.setUseExpectContinue(params, true);
      HttpProtocolParams.setUserAgent(params, "Android DRRadio/1.x");
      httpClient = new DefaultHttpClient(params);
    }
    //dt("");
    Log.d("Elŝutas " + url);
    //Log.e(new Exception("Henter "+url));
    //InputStream is = new URL(url).openStream();

    HttpGet c = new HttpGet(url);
    HttpResponse response = httpClient.execute(c);
    InputStream is = response.getEntity().getContent();
    Header contentEncoding = response.getFirstHeader("Content-Encoding");
    if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
      is = new GZIPInputStream(is);
    }

    String jsondata = læsInputStreamSomStreng(is);
    //Log.d("Hentede "+url+" på "+dt("hente "+url));
    return jsondata;
  }

  public static String læsInputStreamSomStreng(InputStream is) throws IOException, UnsupportedEncodingException {

    // Det kan være nødvendigt at hoppe over BOM mark - se http://android.forums.wordpress.org/topic/xml-pull-error?replies=2
    //is.read(); is.read(); is.read(); // - dette virker kun hvis der ALTID er en BOM
    // Hop over BOM - hvis den er der!
    is = new BufferedInputStream(is);  // bl.a. FileInputStream understøtter ikke mark, så brug BufferedInputStream
    is.mark(1); // vi har faktisk kun brug for at søge én byte tilbage
    if (is.read() == 0xef) {
      is.read();
      is.read();
    } // Der var en BOM! Læs de sidste 2 byte
    else is.reset(); // Der var ingen BOM - hop tilbage til start


    final char[] buffer = new char[0x3000];
    StringBuilder out = new StringBuilder();
    Reader in = new InputStreamReader(is, "UTF-8");
    int read;
    do {
      read = in.read(buffer, 0, buffer.length);
      if (read > 0) {
        out.append(buffer, 0, read);
      }
    } while (read >= 0);
    in.close();
    String jsondata = out.toString();
    return jsondata;
  }

  public static ArrayList<String> jsonArrayTilArrayListString(JSONArray j) throws JSONException {
    int n = j.length();
    ArrayList<String> res = new ArrayList<String>(n);
    for (int i = 0; i < n; i++) {
      res.add(j.getString(i));
    }
    return res;
  }

  public static String læsNullSomTom(JSONObject j, String string) {
    try {
      return j.getString(string);
    } catch (JSONException ex) {
      return "";
    }
  }
}
