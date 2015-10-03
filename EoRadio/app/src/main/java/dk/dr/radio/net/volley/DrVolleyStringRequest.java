package dk.dr.radio.net.volley;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

/**
 * Oprettet af Jacob Nordfalk d 13-03-14.
 */
public class DrVolleyStringRequest extends StringRequest {
  private final DrVolleyResonseListener lytter;

  public DrVolleyStringRequest(String url, final DrVolleyResonseListener listener) {
    super(url, listener, listener);
    lytter = listener;
    lytter.url = url;
    if (!App.ÆGTE_DR && url.startsWith("http://www.dr.dk/tjenester")) {
      Log.rapporterFejl(new IllegalAccessException("Dette er ikke en DR app"));
    }
    if (!App.PRODUKTION && url.equals("http://dr-mu-apps.azurewebsites.net/tjenester/mu-apps-test/channel/p4?includeStreams=true")) {
      throw new Error("P4 streamURL kaldt, uden underkanal");
    }
    App.sætErIGang(true, url);
    /*
     * DRs serverinfrastruktur caches med Varnish, men det kan tage lang tid for den bagvedliggende
     * serverinfrastruktur at svare.
     */
    setRetryPolicy(new DefaultRetryPolicy(4000, 3, 1.5f)); // Ny instans hver gang, da der ændres i den

    Cache.Entry response = App.volleyRequestQueue.getCache().get(url);
    if (response == null) return; // Vi har ikke en cachet udgave
    try {
      //String contentType = response.responseHeaders.get(HTTP.CONTENT_TYPE);
      // Fix: Det er set at Volley ikke husker contentType, og dermed går tegnsættet tabt. Gæt på UTF-8 hvis det sker
      //String charset = contentType==null?HTTP.UTF_8:HttpHeaderParser.parseCharset(response.responseHeaders);
      //lytter.cachetVærdi = new String(response.data, charset);
      lytter.cachetVærdi = new String(response.data, HttpHeaderParser.parseCharset(response.responseHeaders));

      // Vi kalder fikSvar i forgrundstråden - og dermed må forespørgsler ikke foretages direkte
      // fra en listeopdatering eller fra getView
      lytter.fikSvar(listener.cachetVærdi, true, false);
    } catch (Exception e) {
      e.printStackTrace();
      // En fejl i den cachede værdi - smid indholdet af cachen væk, det kan alligevel ikke bruges
      App.volleyRequestQueue.getCache().remove(url);
      return;
    }
    //Log.d("XXXXXXXXXXXXXX Cache.Entry  e=" + response);
    // Kald først fikSvar når forgrundstråden er færdig med hvad den er i gang med
    // - i tilfælde af at en forespørgsel er startet midt under en listeopdatering giver det problemer
    // at opdatere listen omgående, da elementer så kan skifte position (og måske type) midt i det hele
    /*
    App.forgrundstråd.post(new Runnable() {
      @Override
      public void run() {
    try {
      listener.fikSvar(listener.cachetVærdi, true, false);
    } catch (Exception e) {
      listener.onErrorResponse(new VolleyError(e));
    }
      }
    });
    */
  }

  @Override
  public Map<String, String> getHeaders(){
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("User-agent", App.versionsnavn);
    return headers;
  }

  @Override
  public void cancel() {
    super.cancel();
    lytter.annulleret();
  }


  @Override
  protected Response<String> parseNetworkResponse(NetworkResponse response) {
    response.headers.remove("Cache-Control");
    return super.parseNetworkResponse(response);
  }

}
