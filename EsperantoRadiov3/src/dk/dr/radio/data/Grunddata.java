/**
 DR Radio 2 is developed by Jacob Nordfalk, Hanafi Mughrabi and Frederik Aagaard.
 Some parts of the code are loosely based on Sveriges Radio Play for Android.

 DR Radio 2 for Android is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2 as published by
 the Free Software Foundation.

 DR Radio 2 for Android is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 DR Radio 2 for Android.  If not, see <http://www.gnu.org/licenses/>.

 */

package dk.dr.radio.data;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import dk.dr.radio.data.afproevning.FilCache;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

public class Grunddata {
  /**
   * Grunddata
   */
  public JSONObject android_json;
  public JSONObject json;

  public List<String> p4koder = new ArrayList<String>();
  public List<Kanal> kanaler = new ArrayList<Kanal>();
  public Kanal forvalgtKanal;
  public ArrayList<Runnable> observatører = new ArrayList<Runnable>(); // Om grunddata/stamdata ændrer sig

  /** find en kanal ud fra kode, f.eks. P1D, P2D, P3, P4F, RØ4, ES4, OD4, KH4, HO4, ÅL4, NV4, ÅB4, TR4, ÅR4, P5D, P6B, P7M, P8J, RAM, DRN */
  public HashMap<String, Kanal> kanalFraKode = new HashMap<String, Kanal>();
  public HashMap<String, Kanal> kanalFraSlug = new LinkedHashMap<String, Kanal>();
  public static final Kanal ukendtKanal = new Kanal();
  public long opdaterPlaylisteEfterMs = 30 * 1000;
  public long opdaterGrunddataEfterMs = 30 * 60 * 1000;
  /** Om Http Live Streaming skal udelukkes fra mulige lydformater. Gælder på Android 2 og visse Android 4-enheder */
  public boolean udelukHLS;

  public String radioTxtUrl = "http://esperanto-radio.com/radio.txt";

  public Grunddata() {
    ukendtKanal.navn = "";
    ukendtKanal.slug = "";
    ukendtKanal.kode = "";
    ukendtKanal.urn = "";
    kanalFraKode.put(null, ukendtKanal);
    kanalFraKode.put("", ukendtKanal);
    kanalFraSlug.put(null, ukendtKanal);
    kanalFraSlug.put("", ukendtKanal);
  }


  public void eo_parseFællesGrunddata(String ĉefdatumojJson) throws JSONException {
    json = new JSONObject(ĉefdatumojJson);

    // Erstat med evt ny værdi
    radioTxtUrl = json.optString("elsendojUrl", radioTxtUrl);

    JSONArray kanalojJs = json.getJSONArray("kanaloj");
    int antal = kanalojJs.length();
    for (int i = 0; i < antal; i++) {
      JSONObject kJs = kanalojJs.getJSONObject(i);
      Kanal k = new Kanal();
      k.kode = kJs.optString("kodo", null);
      if (k.kode ==null) continue;
      k.navn = kJs.getString("nomo");
      String rektaElsendaSonoUrl = kJs.optString("rektaElsendaSonoUrl", null);
      String rektaElsendaPriskriboUrl = kJs.optString("rektaElsendaPriskriboUrl", null);
      k.eo_hejmpaĝoEkrane = kJs.optString("hejmpaĝoEkrane", null);
      k.eo_hejmpaĝoButono = kJs.optString("hejmpaĝoButono", null);
      k.eo_retpoŝto = kJs.optString("retpoŝto", null);
      k.eo_emblemoUrl = kJs.optString("emblemoUrl", null);
      k.eo_json = kJs;
      kanaler.add(k);

      if (rektaElsendaSonoUrl != null) {
        Udsendelse el = new Udsendelse();
        el.startTid = new Date();
        el.kanalSlug = k.navn;
        el.startTidKl = "REKTA";
        el.titel = "";
        el.sonoUrl = rektaElsendaSonoUrl;
        el.rektaElsendaPriskriboUrl = rektaElsendaPriskriboUrl;
        k.eo_rektaElsendo = el;
        //k.udsendelser.add(el);
      }
    }


    for (Kanal k : kanaler) {
      kanalFraKode.put(k.kode, k);
      kanalFraSlug.put(k.navn, k);
    }
  }
  public static final DateFormat datoformato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

  public void leguRadioTxt(String radioTxt) {
    String kapo = null;
    for (String unuo : radioTxt.split("\n\r?\n")) {
      unuo = unuo.trim();
      //Log.d("Unuo: "+unuo);
      if (unuo.length() == 0) {
        continue;
      }
      if (kapo == null) {
        kapo = unuo;
      } else {
        try {
          Udsendelse e = new Udsendelse();
          String[] x = unuo.split("\n");
          /*
           3ZZZ en Esperanto
           2011-09-29
           http://www.melburno.org.au/3ZZZradio/mp3/2011-09-26.3ZZZ.radio.mp3
           Anonco : el retmesaĝo de Floréal Martorell « Katastrofo ĉe Vinilkosmo/ Eurokka Kanto : informo pri la kompaktdisko Hiphopa Kompilo 2 « Miela obsedo » Legado: el la verko de Ken Linton Kanako el Kananam ĉapitro 12 « Stranga ĝardeno » Lez el Monato de aŭgusto /septembro « Tantamas ŝtopiĝoj » de Ivo durwael Karlo el Monato » Eksplofas la popola kolero » [...]
           */
          e.kanalSlug = x[0];
          e.startTidKl = x[1];
          e.startTid = datoformato.parse(x[1]);
          e.sonoUrl = x[2];
          e.beskrivelse = x[3];

          Kanal k = kanalFraSlug.get(e.kanalSlug);
          // Jen problemo. "Esperanta Retradio" nomiĝas "Peranto" en
          // http://esperanto-radio.com/radio.txt . Ni solvas tion serĉante ankaŭ por la kodo
          // "kodo": "peranto",
          // "nomo": "Esperanta Retradio",

          if (k == null) {
            k = kanalFraKode.get(e.kanalSlug.toLowerCase());
            if (k != null) e.kanalSlug = k.navn;
          }

          if (k == null) {
            Log.d("Nekonata kanalnomo - ALDONAS GXIN: " + e.kanalSlug);
            k = new Kanal();
            k.eo_json = new JSONObject();
            k.kode = k.navn = e.kanalSlug;
            k.eo_datumFonto = "aldonita de radio.txt";
            kanalFraKode.put(k.kode, k);
            kanalFraSlug.put(k.navn, k);
            kanaler.add(k);
          } else if (k.eo_datumFonto ==null) {
            k.eo_datumFonto = "radio.txt";
          }
          //Log.d("Aldonas elsendon "+e.toString());
          k.udsendelser.add(e);
        } catch (Exception e) {
          Log.e("Ne povis legi unuon: " + unuo, e);
        }
      }
    }

    for (Kanal k : kanaler) Collections.reverse(k.udsendelser);
  }

  /**
   * @return true se io estis ŝarĝita
   */
  public void ŝarĝiElsendojnDeRss(boolean nurLokajn) {
    for (Kanal k : kanaler) {
      ŝarĝiElsendojnDeRssUrl(k.eo_json.optString("elsendojRssUrl", null), k, nurLokajn);
      ŝarĝiElsendojnDeRssUrl(k.eo_json.optString("elsendojRssUrl1", null), k, nurLokajn);
      //ŝarĝiElsendojnDeRssUrl(k.json.optString("elsendojRssUrl2", null), k, nurLokajn);
    }
  }

  public void ŝarĝiElsendojnDeRssUrl(String elsendojRssUrl, Kanal k, boolean nurLokajn) {
    if (elsendojRssUrl== null) return;
    try {
      Log.d("============ parsas RSS de "+k.kode +" =============");
      String dosiero = FilCache.hentFil(elsendojRssUrl, nurLokajn);
      Log.d(" akiris " + elsendojRssUrl);
      if (dosiero == null) return;
      ArrayList<Udsendelse> elsendoj;
      if ("vinilkosmo".equals(k.kode)) {
        elsendoj = EoRssParsado.parsiElsendojnDeRssVinilkosmo(new FileInputStream(dosiero));
      } else {
        elsendoj = EoRssParsado.parsiElsendojnDeRss(new FileInputStream(dosiero));
      }
      if (k.eo_json.optBoolean("elsendojRssIgnoruTitolon", false)) for (Udsendelse e : elsendoj) e.titel = null;
      if (elsendoj.size() > 0) {
        if (k.eo_rektaElsendo != null) elsendoj.add(k.eo_rektaElsendo);
        k.udsendelser = elsendoj;
        k.eo_datumFonto = "rss";
      }
      Log.d(" parsis " + elsendojRssUrl + " kaj ricevis " + elsendoj.size() + " elsendojn");
    } catch (Exception ex) {
      Log.e("Eraro parsante " + elsendojRssUrl, ex);
    }
  }

  public void forprenuMalplenajnKanalojn() {
    for (Iterator<Kanal> ki =this.kanaler.iterator(); ki.hasNext(); ) {
      Kanal k = ki.next();
      if (k.udsendelser.isEmpty()) {
        Log.d("============ FORPRENAS "+k.kode +", ĉar ĝi ne havas elsendojn! "+k.eo_datumFonto);
      }
    }
  }

  public void rezumo() {
    for (Kanal k : this.kanaler) {
      Log.d("============ "+k.kode +" ============= "+k.udsendelser.size()+" "+k.eo_datumFonto);
      int n = 0;
      for (Udsendelse e : k.udsendelser) {
        Log.d(n++ +" "+ e.startTidKl +" "+e.titel +" "+e.sonoUrl+" "+e.beskrivelse);
        if (n>300) {
          Log.d("...");
          break;
        }
      }
    }
  }


  /**
   * Henter grunddata (faste data)
   * @throws java.io.IOException hvis der er et problem med netværk
   *                             eller parsning (dvs interne fejl af forskellig art som bør rapporteres til udvikler)
   */
  public void da_parseFællesGrunddata(String str) throws JSONException {
    json = new JSONObject(str);

    try {
      opdaterGrunddataEfterMs = json.getJSONObject("intervals").getInt("settings") * 1000;
      opdaterPlaylisteEfterMs = json.getJSONObject("intervals").getInt("playlist") * 1000;
    } catch (Exception e) {
      Log.e(e);
    } // Ikke kritisk

    kanaler.clear();
    p4koder.clear();
    da_parseKanaler(json.getJSONArray("channels"), false);
    Log.d("parseKanaler " + kanaler + " - P4:" + p4koder);
    android_json = json.getJSONObject("android");
    tjekUdelukFraHLS(Build.MODEL + " " + Build.PRODUCT + "/" + Build.VERSION.SDK_INT);
    DRBackendTidsformater.servertidsformatAndre = da_parseDRBackendTidsformater(android_json.optJSONArray("servertidsformatAndre"), DRBackendTidsformater.servertidsformatAndre);
    DRBackendTidsformater.servertidsformatPlaylisteAndre = da_parseDRBackendTidsformater(android_json.optJSONArray("servertidsformatPlaylisteAndre"), DRBackendTidsformater.servertidsformatPlaylisteAndre);
    if (forvalgtKanal == null) forvalgtKanal = kanaler.get(2); // Det er nok P3 :-)
    for (Runnable r : new ArrayList<Runnable>(observatører)) r.run();
  }

  private void fjernKanalMedFejl(Kanal k) {
    kanaler.remove(k);
    p4koder.remove(k.kode);
    kanalFraKode.remove(k.kode);
    kanalFraSlug.remove(k.slug);
  }


  private void da_parseKanaler(JSONArray jsonArray, boolean parserP4underkanaler) throws JSONException {

    int antal = jsonArray.length();
    for (int i = 0; i < antal; i++) {
      JSONObject j = jsonArray.getJSONObject(i);
      String kanalkode = j.optString("scheduleIdent", "P4F");
      Kanal k = kanalFraKode.get(kanalkode);
      if (k == null) {
        k = new Kanal();
        k.kode = j.optString("scheduleIdent", "P4F");
        kanalFraKode.put(k.kode, k);
      }
      k.navn = j.getString("title");
      k.urn = j.getString("urn");
      k.slug = j.optString("slug", "p4");
      k.p4underkanal = parserP4underkanaler;
      kanaler.add(k);
      if (parserP4underkanaler) p4koder.add(k.kode);
      kanalFraSlug.put(k.slug, k);
      if (j.optBoolean("isDefault")) forvalgtKanal = k;

      JSONArray underkanaler = j.optJSONArray("channels");
      if (underkanaler != null) {
        if (!Kanal.P4kode.equals(k.kode)) Log.rapporterFejl(new IllegalStateException("Forkert P4-kode: "), k.kode);
        da_parseKanaler(underkanaler, true);
      }
    }
  }

  private DateFormat[] da_parseDRBackendTidsformater(JSONArray servertidsformatAndreJson, DateFormat[] servertidsformatAndre) throws JSONException {
    if (servertidsformatAndreJson==null) return  servertidsformatAndre;
    DateFormat[] res = new DateFormat[servertidsformatAndreJson.length()];
    for (int i=0; i<res.length; i++) {
      res[i] = new SimpleDateFormat(servertidsformatAndreJson.getString(i));
    }
    return res;
  }

  /**
   * Sætter flaget udelukHLS, som slår HLS fra på Android-enheder, der ikke understøtter det
   * @param model_og_version
   */
  public void tjekUdelukFraHLS(String model_og_version) {
    Log.d("tjekUdelukFraHLS(" + model_og_version);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH && !App.testFraMain()) {
      Log.d("tjekUdelukFraHLS() - Android 2 (og 3) understøtter ikke HLS");
      udelukHLS = true;
      return;
    }

    udelukHLS = false;
    try {
      for (String lin : android_json.getString("udeluk_HLS").split(",")) {
        if (model_og_version.matches(lin.trim())) {
          Log.d("tjekUdelukFraHLS linjen " + lin + " matcher " + model_og_version + ", så HLS slås fra");
          udelukHLS = true;
          break;
        }
      }
    } catch (Exception e) {
      Log.e(e);
    } // Ikke kritisk
  }
}
