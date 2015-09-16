package dk.dr.radio.data;

import java.text.ParseException;
import java.util.Date;

import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

/**
 * Created by j on 13-09-15.
 */
public class EoFavoritter extends Favoritter {

  void startOpdaterAntalNyeUdsendelserForProgramserie(final String programserieSlug, String dato) {
    Programserie ps = DRData.instans.programserieFraSlug.get(programserieSlug);
    if (ps==null) return; // Kial / kiel okazas?
    int antal = 0;
    try {
      Date ekde = DRJson.apiDatoFormat.parse(dato);
      for (Udsendelse u : ps.getUdsendelser()) {
        if (u.startTid.after(ekde)) antal++;
      }
    } catch (ParseException e) {
      Log.rapporterFejl(e);
    }
    favoritTilAntalDagsdato.put(programserieSlug, antal);
    App.forgrundstråd.postDelayed(beregnAntalNyeUdsendelser, 50); // Vent 1/2 sekund på eventuelt andre svar
  }
}
