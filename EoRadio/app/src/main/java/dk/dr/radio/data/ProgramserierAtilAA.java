package dk.dr.radio.data;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dk.dr.radio.diverse.App;
import dk.dr.radio.net.volley.DrVolleyResonseListener;
import dk.dr.radio.net.volley.DrVolleyStringRequest;

/**
 * Created by j on 05-10-14.
 */
public class ProgramserierAtilAA {
  public ArrayList<Programserie> liste;
  public List<Runnable> observatører = new ArrayList<Runnable>();



  public void startHentData() {
    Request<?> req = new DrVolleyStringRequest(DRData.getAtilÅUrl(), new DrVolleyResonseListener() {
      @Override
      public void fikSvar(String json, boolean fraCache, boolean uændret) throws Exception {
        //Log.d("programserierAtilÅ fikSvar " + fraCache+uændret+json);
        if (uændret) return;
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<Programserie> res = new ArrayList<Programserie>();
        for (int n = 0; n < jsonArray.length(); n++) {
          JSONObject programserieJson = jsonArray.getJSONObject(n);
          String programserieSlug = programserieJson.getString(DRJson.Slug.name());
          //Log.d("\n=========================================== programserieSlug = " + programserieSlug);
          Programserie programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
          if (programserie == null) {
            // Hvis der allerede er et programserie-element fra anden side indeholder den mere information end denne her
            programserie = new Programserie();
            DRJson.parsProgramserie(programserieJson, programserie);
            DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
          }
          res.add(programserie);
        }
        //Log.d("programserierAtilÅ res=" + res);
        //Log.d("programserierAtilÅ jsonArray.length()=" + jsonArray.length());
        //Log.d("programserierAtilÅ res.size()=" + res.size());
        DRData.instans.programserierAtilÅ.liste = res;
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
