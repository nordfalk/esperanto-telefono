/**
 Esperanto-radio por Androjd, farita de Jacob Nordfalk.
 Kelkaj partoj de la kodo originas de DR Radio 2 por Android, vidu
 http://code.google.com/p/dr-radio-android/

 Esperanto-radio por Androjd estas libera softvaro: vi povas redistribui
 ĝin kaj/aŭ modifi ĝin kiel oni anoncas en la licenco GNU Ĝenerala Publika
 Licenco (GPL) versio 2.

 Esperanto-radio por Androjd estas distribuita en la espero ke ĝi estos utila,
 sed SEN AJNA GARANTIO; sen eĉ la implica garantio de surmerkatigindeco aŭ
 taŭgeco por iu aparta celo.
 Vidu la GNU Ĝenerala Publika Licenco por pli da detaloj.

 Vi devus ricevi kopion de la GNU Ĝenerala Publika Licenco kune kun la
 programo. Se ne, vidu <http://www.gnu.org/licenses/>.
 */
package dk.dr.radio.data.eo.data;

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
import java.util.List;
import java.util.Locale;

import dk.dr.radio.data.afproevning.FilCache;
import dk.dr.radio.diverse.Log;


public class Grundata {

  public JSONObject json;
  public List<Kanal> kanaler = new ArrayList<Kanal>();
  public static final DateFormat datoformato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
  public HashMap<String, Kanal> kanalFraKode = new HashMap<String, Kanal>();
  public HashMap<String, Kanal> kanalFraNavn = new HashMap<String, Kanal>();
  public String elsendojUrl = "http://esperanto-radio.com/radio.txt";

  /**
   * Liste over de kanaloj der vises 'Spiller lige nu' med info om musiknummer på skærmen
   */
  //public Set<String> kanalojDerSkalViseSpillerNu = new HashSet<String>();
  public Grundata(String ĉefdatumojJson) throws JSONException {
    json = new JSONObject(ĉefdatumojJson);
    
    // Erstat med evt ny værdi
    elsendojUrl = json.optString("elsendojUrl", elsendojUrl);

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
        k.udsendelser.add(el);
      }
    }


    for (Kanal k : kanaler) {
      kanalFraKode.put(k.kode, k);
      kanalFraNavn.put(k.navn, k);
    }
  }

  public void leguElsendojn(String radioTxt) {
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

          Kanal k = kanalFraNavn.get(e.kanalSlug);
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
            kanalFraNavn.put(k.navn, k);
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
    if (elsendojRssUrl != null) {
      try {
        Log.d("============ parsas RSS de "+k.kode +" =============");
        String dosiero = FilCache.hentFil(elsendojRssUrl, false);
        Log.d(" akiris " + elsendojRssUrl);
        if (dosiero == null) return;
        ArrayList<Udsendelse> elsendoj;
        if ("vinilkosmo".equals(k.kode)) {
          elsendoj = RssParsado.parsiElsendojnDeRssVinilkosmo(new FileInputStream(dosiero));
        } else {
          elsendoj = RssParsado.parsiElsendojnDeRss(new FileInputStream(dosiero));
        }
        if (k.eo_json.optBoolean("elsendojRssIgnoruTitolon", false)) for (Udsendelse e : elsendoj) e.titel = null;
        if (elsendoj.size() > 0) {
          if (k.eo_rektaElsendo != null) elsendoj.add(k.eo_rektaElsendo);
          k.udsendelser = elsendoj;
          k.eo_datumFonto = "rss";
        }
        Log.d(" parsis " + elsendojRssUrl + " kaj ricevis " + elsendoj.size() + " elsendojn");
      }catch (Exception ex) {
       Log.e("Eraro parsante " + elsendojRssUrl, ex);
     }
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
}
