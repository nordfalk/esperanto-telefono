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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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


  private void fjernKanalMedFejl(Kanal k) {
    kanaler.remove(k);
    p4koder.remove(k.kode);
    kanalFraKode.remove(k.kode);
    kanalFraSlug.remove(k.slug);
  }


  private void parseKanaler(JSONArray jsonArray, boolean parserP4underkanaler) throws JSONException {

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
        parseKanaler(underkanaler, true);
      }
    }
  }

  /**
   * Henter grunddata (faste data)
   * @throws java.io.IOException hvis der er et problem med netværk
   *                             eller parsning (dvs interne fejl af forskellig art som bør rapporteres til udvikler)
   */
  public void parseFællesGrunddata(String str) throws JSONException {
    json = new JSONObject(str);

    try {
      opdaterGrunddataEfterMs = json.getJSONObject("intervals").getInt("settings") * 1000;
      opdaterPlaylisteEfterMs = json.getJSONObject("intervals").getInt("playlist") * 1000;
    } catch (Exception e) {
      Log.e(e);
    } // Ikke kritisk

    kanaler.clear();
    p4koder.clear();
    parseKanaler(json.getJSONArray("channels"), false);
    Log.d("parseKanaler " + kanaler + " - P4:" + p4koder);
    android_json = json.getJSONObject("android");
    tjekUdelukFraHLS(Build.MODEL + " " + Build.PRODUCT + "/" + Build.VERSION.SDK_INT);
    DRBackendTidsformater.servertidsformatAndre = parseDRBackendTidsformater(android_json.optJSONArray("servertidsformatAndre"), DRBackendTidsformater.servertidsformatAndre);
    DRBackendTidsformater.servertidsformatPlaylisteAndre = parseDRBackendTidsformater(android_json.optJSONArray("servertidsformatPlaylisteAndre"), DRBackendTidsformater.servertidsformatPlaylisteAndre);
    if (forvalgtKanal == null) forvalgtKanal = kanaler.get(2); // Det er nok P3 :-)
    for (Runnable r : new ArrayList<Runnable>(observatører)) r.run();
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

  private DateFormat[] parseDRBackendTidsformater(JSONArray servertidsformatAndreJson, DateFormat[] servertidsformatAndre) throws JSONException {
    if (servertidsformatAndreJson==null) return  servertidsformatAndre;
    DateFormat[] res = new DateFormat[servertidsformatAndreJson.length()];
    for (int i=0; i<res.length; i++) {
      res[i] = new SimpleDateFormat(servertidsformatAndreJson.getString(i));
    }
    return res;
  }
}
