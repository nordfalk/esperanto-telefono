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
import dk.dr.radio.diverse.Rapportering;

/**
 * Det centrale objekt som alt andet bruger til
 */
public class DRData {

  public static DRData instans;

  // scp /home/j/android/dr-radio-android/DRRadiov3/res/raw/grunddata_udvikling.json j:../lundogbendsen/hjemmeside/drradiov3_grunddata.json

  public static final String GRUNDDATA_URL = App.PRODUKTION
      ? "http://www.dr.dk/tjenester/iphone/radio/settings/iphone200d.drxml"
      : "http://android.lundogbendsen.dk/drradiov3_grunddata.json";
  //public static final String GRUNDDATA_URL = "http://www.dr.dk/tjenester/iphone/radio/settings/iphone200d.json";

  //private static final String BASISURL = "http://www.dr.dk/tjenester/mu-apps";
  //private static final String BASISURL = "http://dr-mu-apps.azurewebsites.net";
  private static final String BASISURL = App.PRODUKTION
     ? "http://www.dr.dk/tjenester/mu-apps"
     : "http://dr-mu-apps.azurewebsites.net/tjenester/mu-apps-test";

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

  public Rapportering rapportering = new Rapportering();
  public SenestLyttede senestLyttede = new SenestLyttede();
  public Favoritter favoritter = new Favoritter();
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

  public static String getUdsendelseStreamsUrlFraUrn(String urn) {
    // http://www.dr.dk/tjenester/mu-apps/program?urn=urn:dr:mu:programcard:52e6fa58a11f9d1588de9c49&includeStreams=true
    return BASISURL + "/program?includeStreams=true&urn=" + urn;
  }


  public static String getProgramserieUrl(String programserieSlug) {
    // svarer til v3_programserie.json
    // http://www.dr.dk/tjenester/mu-apps/series/monte-carlo?type=radio&includePrograms=true
    // http://www.dr.dk/tjenester/mu-apps/series/monte-carlo?type=radio&includePrograms=true&includeStreams=true

    return BASISURL + "/series/" + programserieSlug + "?type=radio&includePrograms=true";
  }

  public static String getKanalStreamsUrlFraSlug(String slug) {
    //return BASISURL + "/channel?includeStreams=true&urn=" + urn;
    return BASISURL + "/channel/" + slug + "?includeStreams=true";
  }

  public static String getKanalUdsendelserUrlFraKode(String kode, String datoStr) {
    return BASISURL + "/schedule/" + URLEncoder.encode(kode) + "/date/" + datoStr;  // svarer til v3_kanalside__p3.json;
  }

  public static String getAtilÅUrl() {
    return BASISURL + "/series-list?type=radio";
  }

  public static String getUdsendelseStreamsUrlFraSlug(String udsendelseSlug) {
    return BASISURL + "/program/" + udsendelseSlug + "?type=radio&includeStreams=true";
  }

  public static String getSøgIUdsendelserUrl(String søgStr) {
    return BASISURL + "/search/programs?q=" + URLEncoder.encode(søgStr) + "&type=radio";
  }

  public static String getSøgISerierUrl(String søgStr) {
    return BASISURL + "/search/series?q=" + URLEncoder.encode(søgStr) + "&type=radio";
  }

  public static String getBogOgDramaUrl() {
    return BASISURL + "/radio-drama-adv";
  }

  public static String getNyeProgrammerSiden(String programserieSlug, String dato) {
    return BASISURL + "/new-programs-since/" + programserieSlug + "/" + dato;
  }

  public static String getPlaylisteUrl(String slug) {
    // Tidligere (marts 2014) skulle kanalens slug med, såsom
    // http://www.dr.dk/tjenester/mu-apps/playlist/monte-carlo-352/p3
    // Det er tilsyneladende ikke nødvendigt mere, per april 2014
    return BASISURL + "/playlist/" + slug + "/0";
  }
}
