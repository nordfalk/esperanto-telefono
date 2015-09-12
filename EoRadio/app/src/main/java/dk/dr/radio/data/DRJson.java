package dk.dr.radio.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.v3.R;

/**
 * Navne for felter der er i DRs JSON-feeds og støttefunktioner til at parse dem
 * Created by j on 19-01-14.
 */
public enum DRJson {
  Slug,       // unik ID for en udsendelse eller getKanal
  SeriesSlug, // unik ID for en programserie
  Urn,        // en anden slags unik ID
  Title, Description, ImageUrl,
  StartTime, EndTime,
  Streams,
  Uri, Played, Artist, Image,
  Type, Kind, Quality, Kbps, ChannelSlug, TotalPrograms, Programs,
  FirstBroadcast, DurationInSeconds, Format, OffsetMs,
  ProductionNumber, ShareLink, Episode, Chapters, Subtitle,

  /**
   * "Watchable indikerer om der er nogle resourcer (og deraf streams) tilgængelige,
   * så det er egentlig en Streamable-property:
   * Watchable = (pc.PrimaryAssetKind == "AudioResource" || pc.PrimaryAssetKind == "VideoResource")"
   */
  Watchable,
  /**
   * Om en udsendelse kan streames.
   * "Attribut der angiver om du formentlig kan streame et program.
   * Når jeg skriver formentlig, så er det fordi den ikke tjekker på platform men bare generelt — iOS kan fx ikke streame f4m, men den vil stadig vise Playable da den type findes. Dog er det yderst sjældent at f4m vil være der og ikke m3u8"
   */
  Playable,
  /* Om en udsendelse kan hentes. Som #Watchable */
  Downloadable,
  /* Berigtigelser */
  RectificationTitle, RectificationText,

  /* Drama og Bog */
  Spots, Series
  ;

  /*
    public enum StreamType {
      Shoutcast, // 0 tidligere 'Streaming'
      HLS_fra_DRs_servere,  // 1 tidligere 'IOS' - virker p.t. ikke på Android
      RTSP, // 2 tidligere 'Android' - udfases
      HDS, // 3 Adobe  HTTP Dynamic Streaming
      HLS_fra_Akamai, // 4 oprindeligt 'HLS'
      HLS_med_probe_og_fast_understream,
      HLS_byg_selv_m3u8_fil,
      Ukendt;  // = -1 i JSON-svar
      static StreamType[] v = values();
    }
    */
  public enum StreamType {
    Streaming_RTMP, // 0
    HLS_fra_DRs_servere,  // 1 tidligere 'IOS' - virker p.t. ikke på Android
    RTSP, // 2 tidligere 'Android' - udfases
    HDS, // 3 Adobe  HTTP Dynamic Streaming
    HLS_fra_Akamai, // 4 oprindeligt 'HLS' - virker på Android 4
    HTTP, // 5 Til on demand/hentning af lyd
    Shoutcast, // 6 Til Android 2
    Ukendt;  // = -1 i JSON-svar
    static StreamType[] v = values();

  }


  public enum StreamKind {
    Audio,
    Video;
    static StreamKind[] v = values();
  }

  public enum StreamQuality {
    High,     // 0
    Medium,   // 1
    Low,      // 2
    Variable; // 3
    static StreamQuality[] v = values();
  }

  public enum StreamConnection {
    Wifi,
    Mobile;
    static StreamConnection[] v = values();
  }


  /**
   * Datoformat som serveren forventer det forskellige steder
   */
  public static DateFormat apiDatoFormat = new SimpleDateFormat("yyyy-MM-dd");
/*
  public static void main(String[] a) throws ParseException {
    System.out.println(servertidsformat.format(new Date()));
    System.out.println(servertidsformat.parse("2014-01-16T09:04:00+01:00"));
  }
*/

  public static final Locale dansk = new Locale("da", "DA");
  public static final DateFormat klokkenformat = new SimpleDateFormat("HH:mm", dansk);
  public static final DateFormat datoformat = new SimpleDateFormat("d. MMM yyyy", dansk);
  private static final DateFormat ugedagformat = new SimpleDateFormat("EEEE d. MMM", dansk);
  private static final DateFormat årformat = new SimpleDateFormat("yyyy", dansk);
  public static String iDagDatoStr, iMorgenDatoStr, iGårDatoStr, iOvermorgenDatoStr, iForgårsDatoStr, iÅrDatoStr;
  public static final String I_DAG = "I DAG";
  private static HashMap<String, String> datoTilBeskrivelse = new HashMap<String, String>();

  public static void opdateriDagIMorgenIGårDatoStr(long nu) {
    String nyIDagDatoStr = datoformat.format(new Date(nu));
    if (nyIDagDatoStr.equals(iDagDatoStr)) return;

    iDagDatoStr = datoformat.format(new Date(nu));
    iMorgenDatoStr = datoformat.format(new Date(nu + 24 * 60 * 60 * 1000));
    iOvermorgenDatoStr = datoformat.format(new Date(nu + 2 * 24 * 60 * 60 * 1000));
    iGårDatoStr = datoformat.format(new Date(nu - 24 * 60 * 60 * 1000));
    iForgårsDatoStr = datoformat.format(new Date(nu - 2 * 24 * 60 * 60 * 1000));
    iÅrDatoStr = årformat.format(new Date(nu));
    datoTilBeskrivelse.clear();
  }

  static {
    opdateriDagIMorgenIGårDatoStr(System.currentTimeMillis());
  }

  private static final String HTTP_WWW_DR_DK = "http://www.dr.dk";
  private static final int HTTP_WWW_DR_DK_lgd = HTTP_WWW_DR_DK.length();

  /**
   * Fjerner http://www.dr.dk i URL'er
   */
  private static String fjernHttpWwwDrDk(String url) {
    if (url != null && url.startsWith(HTTP_WWW_DR_DK)) {
      return url.substring(HTTP_WWW_DR_DK_lgd);
    }
    return url;
  }


  private static Udsendelse opretUdsendelse(DRData drData, JSONObject o) throws JSONException {
    String slug = o.optString(DRJson.Slug.name());  // Bemærk - kan være tom!
    Udsendelse u = new Udsendelse();
    u.slug = slug;
    drData.udsendelseFraSlug.put(u.slug, u);
    u.titel = o.getString(DRJson.Title.name());
    u.beskrivelse = o.getString(DRJson.Description.name());
    u.billedeUrl = fjernHttpWwwDrDk(o.optString(DRJson.ImageUrl.name(), null));
    u.programserieSlug = o.optString(DRJson.SeriesSlug.name());  // Bemærk - kan være tom!
    u.episodeIProgramserie = o.optInt(DRJson.Episode.name());
    u.urn = o.optString(DRJson.Urn.name());  // Bemærk - kan være tom!
    return u;
  }

  /**
   * Parser udsendelser for getKanal. A la http://www.dr.dk/tjenester/mu-apps/schedule/P3/0
   */
  public static ArrayList<Udsendelse> parseUdsendelserForKanal(JSONArray jsonArray, Kanal kanal, Date dato, DRData drData) throws JSONException {
    String dagsbeskrivelse = getDagsbeskrivelse(dato);

    ArrayList<Udsendelse> uliste = new ArrayList<Udsendelse>();
    for (int n = 0; n < jsonArray.length(); n++) {
      JSONObject o = jsonArray.getJSONObject(n);
      Udsendelse u = opretUdsendelse(drData, o);
      u.kanalSlug = kanal.slug;// o.optString(DRJson.ChannelSlug.name(), kanal.slug);  // Bemærk - kan være tom.
      u.kanHøres = o.getBoolean(DRJson.Watchable.name());
      u.startTid = DRBackendTidsformater.parseUpålideigtServertidsformat(o.getString(DRJson.StartTime.name()));
      u.startTidKl = klokkenformat.format(u.startTid);
      u.slutTid = DRBackendTidsformater.parseUpålideigtServertidsformat(o.getString(DRJson.EndTime.name()));
      u.slutTidKl = klokkenformat.format(u.slutTid);
      u.dagsbeskrivelse = dagsbeskrivelse;
/*
      if (datoStr.equals(iDagDatoStr)) ; // ingen ting
      else if (datoStr.equals(iMorgenDatoStr)) u.startTidKl += " - i morgen";
      else if (datoStr.equals(iGårDatoStr)) u.startTidKl += " - i går";
      else u.startTidKl += " - " + datoStr;
*/
      uliste.add(u);
    }
    return uliste;
  }

  public static String getDagsbeskrivelse(Date tid) {
    String datoStr0 = datoformat.format(tid);
    // Vi har brug for at tjekke for ens datoer hurtigt, så vi laver datoen med objekt-lighed ==
    // Se også String.intern()
    String dagsbeskrivelse = datoTilBeskrivelse.get(datoStr0);
    if (dagsbeskrivelse == null) {
      dagsbeskrivelse = ugedagformat.format(tid);
      String år = årformat.format(tid);
      if (datoStr0.equals(iDagDatoStr)) dagsbeskrivelse = App.instans.getString(R.string.i_dag); // DA ŝanĝo
      else if (datoStr0.equals(iMorgenDatoStr)) dagsbeskrivelse = App.instans.getString(R.string.i_morgen)+" - " + dagsbeskrivelse;
      else if (datoStr0.equals(iOvermorgenDatoStr)) dagsbeskrivelse = App.instans.getString(R.string.i_overmorgen) + " - " + dagsbeskrivelse;
      else if (datoStr0.equals(iGårDatoStr)) dagsbeskrivelse = App.instans.getString(R.string.i_går); // "I GÅR - "+dagsbeskrivelse;
      else if (datoStr0.equals(iForgårsDatoStr)) dagsbeskrivelse = App.instans.getString(R.string.i_forgårs)+" - " + dagsbeskrivelse;
      else if (år.equals(iÅrDatoStr)) dagsbeskrivelse = dagsbeskrivelse;
      else dagsbeskrivelse = dagsbeskrivelse + " " + år;
      dagsbeskrivelse = dagsbeskrivelse.toUpperCase();
      datoTilBeskrivelse.put(datoStr0, dagsbeskrivelse);
    }
    return dagsbeskrivelse;
  }

  /**
   * Parser udsendelser for programserie.
   * A la http://www.dr.dk/tjenester/mu-apps/series/sprogminuttet?type=radio&includePrograms=true
   */
  public static ArrayList<Udsendelse> parseUdsendelserForProgramserie(JSONArray jsonArray, Kanal kanal, DRData drData) throws JSONException {
    ArrayList<Udsendelse> uliste = new ArrayList<Udsendelse>();
    for (int n = 0; n < jsonArray.length(); n++) {
      uliste.add(parseUdsendelse(kanal, drData, jsonArray.getJSONObject(n)));
    }
    return uliste;
  }

  public static Udsendelse parseUdsendelse(Kanal kanal, DRData drData, JSONObject o) throws JSONException {
    Udsendelse u = opretUdsendelse(drData, o);
    if (kanal != null && kanal.slug.length() > 0) u.kanalSlug = kanal.slug;
    else u.kanalSlug = o.optString(DRJson.ChannelSlug.name());  // Bemærk - kan være tom.
    u.startTid = DRBackendTidsformater.parseUpålideigtServertidsformat(o.getString(DRJson.FirstBroadcast.name()));
    u.startTidKl = klokkenformat.format(u.startTid);
    u.slutTid = new Date(u.startTid.getTime() + o.getInt(DRJson.DurationInSeconds.name()) * 1000);

    if (!App.PRODUKTION && (!o.has(DRJson.Playable.name()) || !o.has(DRJson.Playable.name())))
      Log.rapporterFejl(new IllegalStateException("Mangler Playable eller Downloadable"), o.toString());
    u.kanHøres = o.optBoolean(DRJson.Playable.name());
    u.kanHentes = o.optBoolean(DRJson.Downloadable.name());
    // Hvis HLS ikke understøttes må vi bruge vi hentningsURL (mp3) til streaming
    if (DRData.instans.grunddata.udelukHLS) {
      u.kanHøres = u.kanHentes;
    }
    u.berigtigelseTitel = o.optString(DRJson.RectificationTitle.name(), null);
    u.berigtigelseTekst = o.optString(DRJson.RectificationText.name(), null);
    if (!App.PRODUKTION && false) {
      u.berigtigelseTitel = "BEKLAGER";
      u.berigtigelseTekst = "Denne udsendelse er desværre ikke tilgængelig. For yderligere oplysninger se dr.dk/programetik";
    }

    return u;
  }

  /*
  Title: "Back to life",
  Artist: "Soul II Soul",
  DetailId: "2213875-1-1",
  Image: "http://api.discogs.com/image/A-4970-1339439274-8053.jpeg",
  ScaledImage: "http://asset.dr.dk/discoImages/?discoserver=api.discogs.com&file=%2fimage%2fA-4970-1339439274-8053.jpeg&h=400&w=400&scaleafter=crop&quality=85",
  Played: "2014-02-06T15:58:33",
  OffsetMs: 6873000
   */
  public static ArrayList<Playlisteelement> parsePlayliste(JSONArray jsonArray) throws JSONException {
    ArrayList<Playlisteelement> liste = new ArrayList<Playlisteelement>();
    for (int n = 0; n < jsonArray.length(); n++) {
      JSONObject o = jsonArray.getJSONObject(n);
      Playlisteelement u = new Playlisteelement();
      u.titel = o.getString(DRJson.Title.name());
      u.kunstner = o.getString(DRJson.Artist.name());
      u.billedeUrl = o.optString(DRJson.Image.name(), null);
      u.startTid = DRBackendTidsformater.parseUpålideigtServertidsformatPlayliste(o.getString(DRJson.Played.name()));
      u.startTidKl = klokkenformat.format(u.startTid);
      u.offsetMs = o.optInt(DRJson.OffsetMs.name(), -1);
      liste.add(u);
    }
    return liste;
  }

  /**
   * Parse en stream.
   * F.eks. Streams-objekt fra
   * http://www.dr.dk/tjenester/mu-apps/channel?urn=urn:dr:mu:bundle:4f3b8926860d9a33ccfdafb9&includeStreams=true
   * http://www.dr.dk/tjenester/mu-apps/program?includeStreams=true&urn=urn:dr:mu:programcard:531520836187a20f086b5bf9
   * @param jsonArray
   * @return
   * @throws JSONException
   */

  public static ArrayList<Lydstream> parsStreams(JSONArray jsonArray) throws JSONException {
    ArrayList<Lydstream> lydData = new ArrayList<Lydstream>();
    for (int n = 0; n < jsonArray.length(); n++)
      try {
        JSONObject o = jsonArray.getJSONObject(n);
        //Log.d("streamjson=" + o.toString());
        Lydstream l = new Lydstream();
        //if (o.getInt("FileSize")!=0) { Log.d("streamjson=" + o.toString(2)); System.exit(0); }
        l.url = o.getString(DRJson.Uri.name());
        if (l.url.startsWith("rtmp:")) continue; // Skip Adobe Real-Time Messaging Protocol til Flash
        int type = o.getInt(Type.name());
        l.type = type < 0 ? StreamType.Ukendt : StreamType.values()[type];
        if (l.type == StreamType.HDS) continue; // Skip Adobe HDS - HTTP Dynamic Streaming
        //if (l.type == StreamType.IOS) continue; // Gamle HLS streams der ikke virker på Android
        if (o.getInt(Kind.name()) != StreamKind.Audio.ordinal()) continue;
        l.kvalitet = StreamQuality.values()[o.getInt(Quality.name())];
        l.format = o.optString(Format.name()); // null for direkte udsendelser
        l.kbps = o.getInt(Kbps.name());
        lydData.add(l);
        if (App.fejlsøgning) Log.d("lydstream=" + l);
      } catch (Exception e) {
        Log.rapporterFejl(e);
      }
    return lydData;
  }


  /*
  http://www.dr.dk/tjenester/mu-apps/program/p2-koncerten-616 eller
  http://www.dr.dk/tjenester/mu-apps/program?includeStreams=true&urn=urn:dr:mu:programcard:53813014a11f9d16e00f9691
Chapters: [
{
Title: "Introduktion til koncerten",
Description: "P2s Svend Rastrup Andersen klæder dig på til aftenens koncert. Mød også fløjtenisten i Montreal Symfonikerne Tim Hutchins, og hør ham fortælle om orkestrets chefdirigenter, Kent Nagano (nuværende) og Charles Dutoit.",
OffsetMs: 0
},
{
Title: "Wagner: Forspil til Parsifal",
Description: "Parsifal udspiller sig i et univers af gralsriddere og gralsvogtere , der vogter over den hellige gral.",
OffsetMs: 1096360
},
   */
  public static ArrayList<Indslaglisteelement> parsIndslag(JSONArray jsonArray) throws JSONException {
    ArrayList<Indslaglisteelement> liste = new ArrayList<Indslaglisteelement>();
    if (jsonArray == null) return liste;
    for (int n = 0; n < jsonArray.length(); n++) {
      JSONObject o = jsonArray.getJSONObject(n);
      Indslaglisteelement u = new Indslaglisteelement();
      u.titel = o.getString(DRJson.Title.name());
      u.beskrivelse = o.getString(DRJson.Description.name());
      u.offsetMs = o.optInt(DRJson.OffsetMs.name(), -1);
      liste.add(u);
    }
    return liste;
  }

  /*
  Programserie
  {
  Channel: "dr.dk/mas/whatson/channel/P3",
  Webpage: "http://www.dr.dk/p3/programmer/monte-carlo",
  Explicit: true,
  TotalPrograms: 365,
  ChannelType: 0,
  Programs: [],
  Slug: "monte-carlo",
  Urn: "urn:dr:mu:bundle:4f3b8b29860d9a33ccfdb775",
  Title: "Monte Carlo på P3",
  Subtitle: "",
  Description: "Nu kan du dagligt fra 14-16 komme en tur til Monte Carlo, hvor Peter Falktoft og Esben Bjerre vil guide dig rundt. Du kan læne dig tilbage og nyde turen og være på en lytter, når Peter og Esben vender ugens store og små kulturelle begivenheder, kigger på ugens bedste tv og spørger hvad du har #HørtOverHækken. "

Radio-drama
{
Channel: "dr.dk/mas/whatson/channel/P1D",
Webpage: "",
Explicit: true,
TotalPrograms: 3,
ChannelType: 0,
Programs: [ ],
Slug: "efter-fyringerne",
Urn: "urn:dr:mu:bundle:542aa1556187a20ff0bf2709",
Title: "Efter fyringerne",
Subtitle: "",
Description: "I 'Efter fyringerne' lykkes det, gennem private optagelser og interviews med de efterladte, journalist Louise Witt Hansen at skrue historierne bag tre tragiske selvmord sammen."
}

  }*/

  /**
   * Parser et Programserie-objekt
   * @param o  JSON
   * @param ps et eksisterende objekt, der skal opdateres, eller null
   * @return objektet
   * @throws JSONException
   */
  public static Programserie parsProgramserie(JSONObject o, Programserie ps) throws JSONException {
    if (ps == null) ps = new Programserie();
    ps.titel = o.getString(DRJson.Title.name());
    ps.undertitel = o.optString(DRJson.Subtitle.name(), ps.undertitel);
    ps.beskrivelse = o.optString(DRJson.Description.name());
    ps.billedeUrl = fjernHttpWwwDrDk(o.optString(DRJson.ImageUrl.name(), ps.billedeUrl));
    ps.slug = o.getString(DRJson.Slug.name());
    ps.urn = o.optString(DRJson.Urn.name());
    ps.antalUdsendelser = o.optInt(DRJson.TotalPrograms.name(), ps.antalUdsendelser);
    return ps;
  }
}
