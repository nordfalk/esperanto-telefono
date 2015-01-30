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

package dk.dr.radio.data.afproevning;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dk.dr.radio.data.DRBackendTidsformater;
import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Diverse;
import dk.dr.radio.data.Grunddata;
import dk.dr.radio.data.Kanal;
import dk.dr.radio.data.Programserie;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

// For at køre denne klasse, skal noget a la det følgende ind i VM Options i værktøjet
// -classpath $PROJECT_DIR$/../../dr-netradio/trunk/JSONParsning/lib/json-1.0.jar:$PROJECT_DIR$/out/production/DRRadiov3:$APPLICATION_HOME_DIR$/lib/idea_rt.jar:$PROJECT_DIR$/../../android-sdk-linux_86/platforms/android-18/android.jar:$PROJECT_DIR$/libs/android-support-v7-appcompat.jar:$PROJECT_DIR$/libs/android-support-v4.jar:$PROJECT_DIR$/libs/bugsense-3.6.jar:$PROJECT_DIR$/libs/volley.jar

/**
 * Afprøvning af diverse ting
 */
public class Afproevning {

  public static void main(String[] a) throws Exception {
    FilCache.init(new File("/tmp/drradio-cache"));
    DRBackendTidsformater.servertidsformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // +01:00 springes over da kolon i +01:00 er ikke-standard Java
    System.out.println("App.instans="+ App.instans);
    tjekUdelukFraHLS();
    tjekHentAlleUdsendelser();
    tjek_hent_a_til_å_og_radiodrama();
  }


  static String hentStreng(String url) throws IOException {
    //String data = Diverse.læsStreng(new FileInputStream(FilCache.hentFil(url, false, true, 1000 * 60 * 60 * 24 * 7)));
    Log.d(url);
    url = url.replaceAll("Ø", "%C3%98");
    url = url.replaceAll("Å", "%C3%85");
    String data = Diverse.læsStreng(new FileInputStream(FilCache.hentFil(url, false, true, 12 * 1000 * 60 * 60)));
    Log.d(data);
    return data;

  }

  static void hentSupplerendeData(Grunddata ths) {
    for (Kanal k : ths.kanaler)
      try {
        String url = k.getStreamsUrl();
        String data = hentStreng(url);
        JSONObject o = new JSONObject(data);
        //k.slug = o.getString(DRJson.Slug.name());
        //kanalFraSlug.put(k.slug, k);
        k.streams = DRJson.parsStreams(o.getJSONArray(DRJson.Streams.name()));
        //Log.d(k.kode + " k.lydUrl=" + k.streams);
      } catch (Exception e) {
        Log.e(e);
      }
    Log.d("DRData.instans.grunddata.kanalFraSlug=" + DRData.instans.grunddata.kanalFraSlug);
  }


  public static void tjekUdelukFraHLS() throws Exception {
    DRData i = DRData.instans = new DRData();
    i.grunddata = new Grunddata();
    i.grunddata.da_parseFællesGrunddata(Diverse.læsStreng(new FileInputStream("../EsperantoRadiov3/res/raw/grunddata.json")));
    i.grunddata.android_json.put("udeluk_HLS", "C6603 .*/18, IdeaPadA10 A10/17, LIFETAB_E7312 LIFETAB_E7310/17, LIFETAB_E10310/.*");
    i.grunddata.udelukHLS=false;
    i.grunddata.tjekUdelukFraHLS("C6603 C6603/18"); if (i.grunddata.udelukHLS!=true) throw new Exception();
    i.grunddata.tjekUdelukFraHLS("C6603 C6603/17"); if (i.grunddata.udelukHLS==true) throw new Exception();
    i.grunddata.tjekUdelukFraHLS("IdeaPadA10 A10/17"); if (i.grunddata.udelukHLS!=true) throw new Exception();
    i.grunddata.tjekUdelukFraHLS("IdeaPadA10 A10/23"); if (i.grunddata.udelukHLS==true) throw new Exception();
    i.grunddata.tjekUdelukFraHLS("IdeaPadA10 A11/17"); if (i.grunddata.udelukHLS==true) throw new Exception();
    i.grunddata.tjekUdelukFraHLS("LIFETAB_E10310/16"); if (i.grunddata.udelukHLS!=true) throw new Exception();
  }

  public static void tjekHentAlleUdsendelser() throws Exception {
    DRData i = DRData.instans = new DRData();
    i.grunddata = new Grunddata();
    i.grunddata.da_parseFællesGrunddata(Diverse.læsStreng(new FileInputStream("../EsperantoRadiov3/res/raw/grunddata.json")));

    hentSupplerendeData(i.grunddata);
    //System.exit(0);

    for (Kanal kanal : i.grunddata.kanaler) {
      Log.d("\n\n===========================================\n\nkanal = " + kanal);
      if (Kanal.P4kode.equals(kanal.kode)) continue;
      if ("DRN".equals(kanal.kode)) continue; // ikke DR Nyheder

      String datoStr = DRJson.apiDatoFormat.format(new Date());
      kanal.setUdsendelserForDag(DRJson.parseUdsendelserForKanal(new JSONArray(
          hentStreng(DRData.getKanalUdsendelserUrlFraKode(kanal.kode, datoStr))), kanal, new Date(), DRData.instans), "0");
      for (Udsendelse u : kanal.udsendelser) {
        Log.d("\nudsendelse = " + u);
        JSONObject obj = new JSONObject(hentStreng(u.getStreamsUrl()));
        //Log.d(obj.toString(2));
        boolean MANGLER_SeriesSlug = !obj.has(DRJson.SeriesSlug.name());

        u.streams = DRJson.parsStreams(obj.getJSONArray(DRJson.Streams.name()));
        if (u.streams.size() == 0) Log.d("Ingen lydstreams");

        try {
          u.playliste = DRJson.parsePlayliste(new JSONArray(hentStreng(DRData.getPlaylisteUrl(u.slug))));
          Log.d("u.playliste= " + u.playliste);
        } catch (IOException e) {
          e.printStackTrace();
        }

        boolean gavNull = false;
        Programserie ps = i.programserieFraSlug.get(u.programserieSlug);
        if (ps == null) {
          String str = hentStreng(DRData.getProgramserieUrl(u.programserieSlug));
          if ("null".equals(str)) gavNull = true;
          else {
            JSONObject data = new JSONObject(str);
            ps = DRJson.parsProgramserie(data, null);
            JSONArray prg = data.getJSONArray(DRJson.Programs.name());
            ArrayList<Udsendelse> udsendelser = DRJson.parseUdsendelserForProgramserie(prg, kanal, DRData.instans);
            ps.tilføjUdsendelser(0, udsendelser);
            i.programserieFraSlug.put(u.programserieSlug, ps);
          }
        }
        if (MANGLER_SeriesSlug) Log.d("MANGLER_SeriesSlug "+u+ " gavNull="+gavNull +"  fra dagsprogram ="+u.programserieSlug);
      }
    }
  }

  public static void tjek_hent_a_til_å_og_radiodrama() throws Exception {
    DRData i = DRData.instans = new DRData();
    FilCache.init(new File("/tmp/drradio-cache"));
    DRBackendTidsformater.servertidsformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // +01:00 springes over da kolon i +01:00 er ikke-standard Java
    i.grunddata = new Grunddata();
    i.grunddata.da_parseFællesGrunddata(Diverse.læsStreng(new FileInputStream("../EsperantoRadiov3/res/raw/grunddata.json")));
    hentSupplerendeData(i.grunddata);


    // A-Å-liste
    {
      JSONArray jsonArray = new JSONArray(hentStreng(DRData.getAtilÅUrl()));


      // http://www.dr.dk/tjenester/mu-apps/series?type=radio&includePrograms=true&urn=urn:dr:mu:bundle:50d2ab93860d9a09809ca4f2
      ArrayList<Programserie> res = new ArrayList<Programserie>();
      for (int n = 0; n < jsonArray.length(); n++) {
        JSONObject programserieJson = jsonArray.getJSONObject(n);
        String programserieSlug = programserieJson.getString(DRJson.Slug.name());
        Log.d("\n=========================================== programserieSlug = " + programserieSlug);

        Programserie programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
        if (programserie == null) {
          programserie = new Programserie();
          DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
        }
        res.add(DRJson.parsProgramserie(programserieJson, programserie));
/*
        int offset = 0;
        // Virker ikke, giver ALLE udsendelser i RadioDrama:
        // final String url = "http://www.dr.dk/tjenester/mu-apps/series/" + programserieSlug + "?type=radio&includePrograms=true&offset=" + offset;
        final String url = "http://www.dr.dk/tjenester/mu-apps/series/" + programserieSlug + "?type=radio&includePrograms=true&offset=" + offset;
        JSONObject data = new JSONObject(hentStreng(url));
        if (offset == 0) {
          programserie = DRJson.parsProgramserie(data, programserie);
          DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
        }
        programserie.tilføjUdsendelser(offset, DRJson.parseUdsendelserForProgramserie(data.getJSONArray(DRJson.Programs.name()), null, DRData.instans));
        Log.d(programserie.slug + " = " + programserie.getUdsendelser());
*/
      }
      Log.d("res=" + res);

    }

//    System.exit(0);
/* JSON-format er ændret
    // RadioDrama
    // Virker ikke:
    //JSONArray jsonArray = new JSONArray(hentStreng("http://www.dr.dk/tjenester/mu-apps/radio-drama?type=radio&includePrograms=true"));
    JSONArray jsonArray = new JSONArray(hentStreng(DRData.getBogOgDramaUrl()));
    ArrayList<Programserie> res = new ArrayList<Programserie>();
    for (int n = 0; n < jsonArray.length(); n++) {
      JSONObject programserieJson = jsonArray.getJSONObject(n);
      String programserieSlug = programserieJson.getString(DRJson.Slug.name());
      Log.d("\n=========================================== programserieSlug = " + programserieSlug);

      Programserie programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
      if (programserie == null) {
        programserie = new Programserie();
        DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
      }
      res.add(DRJson.parsProgramserie(programserieJson, programserie));

      int offset = 0;
      // Virker ikke, giver ALLE udsendelser i RadioDrama:
      // final String url = "http://www.dr.dk/tjenester/mu-apps/series/" + programserieSlug + "?type=radio&includePrograms=true&offset=" + offset;
      final String url = "http://www.dr.dk/tjenester/mu-apps/series/" + programserieSlug + "?type=radio&includePrograms=true&offset=" + offset;
      JSONObject data = new JSONObject(hentStreng(url));
      if (offset == 0) {
        programserie = DRJson.parsProgramserie(data, programserie);
        DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
      }
      programserie.tilføjUdsendelser(offset, DRJson.parseUdsendelserForProgramserie(data.getJSONArray(DRJson.Programs.name()), null, DRData.instans));
      Log.d(programserie.slug + " = " + programserie.getUdsendelser());
    }
    Log.d("res=" + res);
*/

  //  System.exit(0);
  }


}
