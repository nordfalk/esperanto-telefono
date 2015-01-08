package dk.dr.radio.data;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.volley.DrVolleyResonseListener;
import dk.dr.radio.diverse.volley.DrVolleyStringRequest;

/**
 * Created by j on 05-10-14.
 */
public class DramaOgBog {
  public ArrayList<Programserie>[] lister;
  public List<Runnable> observatører = new ArrayList<Runnable>();

  static String[] jsonNavne = { "Drama", "Books"};
  public static String[] overskrifter = { "RADIO DRAMA", "LYDBØGER"};

  public void startHentData() {
    Request<?> req = new DrVolleyStringRequest(DRData.getBogOgDramaUrl(), new DrVolleyResonseListener() {
      @Override
      public void fikSvar(String json, boolean fraCache, boolean uændret) throws Exception {
        if (uændret) return;
        JSONObject jsonObject = new JSONObject(json);
        ArrayList<Programserie>[] resa = new ArrayList[overskrifter.length];
        for (int sektionsnummer=0; sektionsnummer<overskrifter.length; sektionsnummer++) {
          ArrayList<Programserie> res = resa[sektionsnummer] = new ArrayList<Programserie>();
          JSONArray jsonArray = jsonObject.optJSONArray(jsonNavne[sektionsnummer]);
          if (jsonArray==null) continue;
          for (int n = 0; n < jsonArray.length(); n++) {
            JSONObject programserieJson = jsonArray.getJSONObject(n);
            String programserieSlug = programserieJson.getString(DRJson.Slug.name());
            //Log.d("\n DramaOgBog =========================================== programserieSlug = " + programserieSlug);
            Programserie programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
            if (programserie == null) {
              programserie = new Programserie();
              DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
            }
            res.add(DRJson.parsProgramserie(programserieJson, programserie));
            Log.d("DramaOgBogD "+sektionsnummer+" "+n+programserie+" "+programserie.antalUdsendelser+" "+programserie.billedeUrl);
          }
          Log.d("parseDramaOgBog "+overskrifter[sektionsnummer]+ " res=" + res);
        }
        lister = resa;
        for (Runnable r : observatører) r.run(); // Informér observatører
      }
    }) {
      public Priority getPriority() {
        return Priority.LOW;
      }
    };
    App.volleyRequestQueue.add(req);
  }
}
