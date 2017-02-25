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

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;

import dk.dr.radio.afspilning.Afspiller;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.v3.R;

/**
 * Det centrale objekt som alt andet bruger til
 */
public class DRData {

  public static DRData instans;

  // scp /home/j/android/dr-radio-android/DRRadiov3/res/raw/grunddata_udvikling.json j:../lundogbendsen/hjemmeside/drradiov3_grunddata.json

  public static final String GRUNDDATA_URL = App.instans==null? "http://javabog.dk/privat/esperantoradio_kanaloj_v8.json" :
          App.PRODUKTION
      ? App.instans.getString(R.string.GRUNDDATA_URL_PRODUKTION)
      : App.instans.getString(R.string.GRUNDDATA_URL_UDVIKLING);
  //public static final String GRUNDDATA_URL = "http://www.dr.dk/tjenester/iphone/radio/settings/iphone200d.json";

  private static final String BASISURL = "http://www.dr.dk/tjenester/mu-apps";
//  private static final String BASISURL = "http://dr-mu-apps.azurewebsites.net/tjenester/mu-apps";
  //private static final String BASISURL = App.PRODUKTION
  //   ? "http://www.dr.dk/tjenester/mu-apps"
  //   : "http://dr-mu-apps.azurewebsites.net";

  public Grunddata grunddata;
  public Afspiller afspiller;

  public HashMap<String, Udsendelse> udsendelseFraSlug = new HashMap<String, Udsendelse>();
  public HashMap<String, Programserie> programserieFraSlug = new HashMap<String, Programserie>();

  /**
   * Manglende 'SeriesSlug' (i andre kald end det for dagsprogrammet for en kanal!)
   * betyder at der ikke er en programserie, og videre navigering derfor skal slås fra.
   * 9.okt 2014
   */
  public HashSet<String> programserieSlugFindesIkke = new HashSet<String>();

  public SenestLyttede senestLyttede = new SenestLyttede();
  public Favoritter favoritter = App.ÆGTE_DR? new Favoritter() : new EoFavoritter();
  public HentedeUdsendelser hentedeUdsendelser = new HentedeUdsendelser();  // Understøttes ikke på Android 2.2
  public ProgramserierAtilAA programserierAtilÅ = new ProgramserierAtilAA();
  public DramaOgBog dramaOgBog = new DramaOgBog();
    /*
     * Kald
		 * http://www.dr.dk/tjenester/mu-apps/search/programs?q=monte&type=radio
		 * vil kun returnere radio programmer
		 * http://www.dr.dk/tjenester/mu-apps/search/series?q=monte&type=radio
		 * vil kun returnere radio serier
		 */

  public static String getUdsendelseStreamsUrl(Udsendelse u) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR - URN="+u);
    // http://www.dr.dk/tjenester/mu-apps/program?urn=urn:dr:mu:programcard:52e6fa58a11f9d1588de9c49&includeStreams=true
    return BASISURL + "/program?includeStreams=true&urn=" + u.urn;
  }

  private static final boolean BRUG_URN = true;

  public static String getProgramserieUrl(Programserie ps, String programserieSlug) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    if (App.TJEK_ANTAGELSER && ps!=null && !programserieSlug.equals(ps.slug)) Log.fejlantagelse(programserieSlug + " !=" + ps.slug);
    // http://www.dr.dk/tjenester/mu-apps/series/monte-carlo?type=radio&includePrograms=true
    // http://www.dr.dk/tjenester/mu-apps/series/monte-carlo?type=radio&includePrograms=true&includeStreams=true
    if (BRUG_URN && ps != null)
      return BASISURL + "/series?urn=" + ps.urn + "&type=radio&includePrograms=true";
    return BASISURL + "/series/" + programserieSlug + "?type=radio&includePrograms=true";
  }

  public static String getKanalStreamsUrlFraSlug(String slug) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    //return BASISURL + "/channel?includeStreams=true&urn=" + urn;
    return BASISURL + "/channel/" + slug + "?includeStreams=true";
  }

  public static String getKanalUdsendelserUrlFraKode(String kode, String datoStr) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    return BASISURL + "/schedule/" + URLEncoder.encode(kode) + "/date/" + datoStr;
  }

  public static String getAtilÅUrl() {
    return BASISURL + "/series-list?type=radio";
  }

  /** Bruges kun fra FangBrowseIntent */
  public static String getUdsendelseStreamsUrlFraSlug(String udsendelseSlug) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    return BASISURL + "/program/" + udsendelseSlug + "?type=radio&includeStreams=true";
  }

  public static String getSøgIUdsendelserUrl(String søgStr) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    return BASISURL + "/search/programs?q=" + URLEncoder.encode(søgStr) + "&type=radio";
  }

  public static String getSøgISerierUrl(String søgStr) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    return BASISURL + "/search/series?q=" + URLEncoder.encode(søgStr) + "&type=radio";
  }

  public static String getBogOgDramaUrl() {
    return BASISURL + "/radio-drama-adv";
  }

  /*
    http://www.dr.dk/tjenester/mu-apps/new-programs-since/2014-02-13?urn=urn:dr:mu:bundle:4f3b8b29860d9a33ccfdb775
    … den kan også bruges med slug:
    http://www.dr.dk/tjenester/mu-apps/new-programs-since/monte-carlo/2014-02-13
   */
  public static String getNyeProgrammerSiden(String programserieSlug, String dato) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    return BASISURL + "/new-programs-since/" + programserieSlug + "/" + dato;
  }

  public static String getPlaylisteUrl(Udsendelse u) {
    if (!App.ÆGTE_DR) throw new IllegalStateException("!App.ÆGTE_DR");
    //if (BRUG_URN)
    //  return BASISURL + "/playlist?urn=" + u.urn + "/0"; // virker ikke
    return BASISURL + "/playlist/" + u.slug + "/0";
  }
}
