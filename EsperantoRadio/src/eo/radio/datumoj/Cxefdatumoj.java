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
package eo.radio.datumoj;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Cxefdatumoj {

  public JSONObject json;
  public List<Kanalo> kanaloj = new ArrayList<Kanalo>();
  public ArrayList<Elsendo> elsendoj = new ArrayList<Elsendo>();
  public static final DateFormat datoformato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
  public HashMap<String, Kanalo> kanalkodoAlKanalo = new HashMap<String, Kanalo>();
  public HashMap<String, Kanalo> kanalnomoAlKanalo = new HashMap<String, Kanalo>();
  public String elsendojUrl = "http://esperanto-radio.com/radio.txt";

  /**
   * Liste over de kanaloj der vises 'Spiller lige nu' med info om musiknummer på skærmen
   */
  //public Set<String> kanalojDerSkalViseSpillerNu = new HashSet<String>();
  public Cxefdatumoj(String ĉefdatumojJson) throws JSONException {
    json = new JSONObject(ĉefdatumojJson);
    
    // Erstat med evt ny værdi
    elsendojUrl = json.optString("elsendojUrl", elsendojUrl);

    JSONArray kanalojJs = json.getJSONArray("kanaloj");
    int antal = kanalojJs.length();
    for (int i = 0; i < antal; i++) {
      JSONObject kJs = kanalojJs.getJSONObject(i);
      Kanalo k = new Kanalo();
      k.kodo = kJs.optString("kodo", null);
      if (k.kodo==null) continue;
      k.nomo = kJs.getString("nomo");
      String rektaElsendaSonoUrl = kJs.optString("rektaElsendaSonoUrl", null);
      String rektaElsendaPriskriboUrl = kJs.optString("rektaElsendaPriskriboUrl", null);
      k.hejmpaĝoEkrane = kJs.optString("hejmpaĝoEkrane", null);
      k.hejmpaĝoButono = kJs.optString("hejmpaĝoButono", null);
      k.retpoŝto = kJs.optString("retpoŝto", null);
      k.emblemoUrl = kJs.optString("emblemoUrl", null);
      k.json = kJs;
      kanaloj.add(k);

      if (rektaElsendaSonoUrl != null) {
        Elsendo el = new Elsendo();
        el.dato = new Date();
        el.kanalNomo = k.nomo;
        el.datoStr = "REKTA";
        el.titolo = "";
        el.priskribo = "(tuŝu por kaŝi)";
        el.elektoIgasLaGalerioMalaperi = true;
        el.sonoUrl = rektaElsendaSonoUrl;
        el.rektaElsendaPriskriboUrl = rektaElsendaPriskriboUrl;
        k.rektaElsendo = el;
        k.elsendoj.add(el);
      }
    }


    for (Kanalo k : kanaloj) {
      kanalkodoAlKanalo.put(k.kodo, k);
      kanalnomoAlKanalo.put(k.nomo, k);
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
          Elsendo e = new Elsendo();
          String[] x = unuo.split("\n");
          /*
           3ZZZ en Esperanto
           2011-09-29
           http://www.melburno.org.au/3ZZZradio/mp3/2011-09-26.3ZZZ.radio.mp3
           Anonco : el retmesaĝo de Floréal Martorell « Katastrofo ĉe Vinilkosmo/ Eurokka Kanto : informo pri la kompaktdisko Hiphopa Kompilo 2 « Miela obsedo » Legado: el la verko de Ken Linton Kanako el Kananam ĉapitro 12 « Stranga ĝardeno » Lez el Monato de aŭgusto /septembro « Tantamas ŝtopiĝoj » de Ivo durwael Karlo el Monato » Eksplofas la popola kolero » [...]
           */
          e.kanalNomo = x[0];
          e.datoStr = x[1];
          e.dato = datoformato.parse(x[1]);
          e.sonoUrl = x[2];
          e.priskribo = x[3];
          elsendoj.add(e);

          Kanalo k = kanalnomoAlKanalo.get(e.kanalNomo);
          // Jen problemo. "Esperanta Retradio" nomiĝas "Peranto" en
          // http://esperanto-radio.com/radio.txt . Ni solvas tion serĉante ankaŭ por la kodo
          // "kodo": "peranto",
          // "nomo": "Esperanta Retradio",

          if (k == null) {
            k = kanalkodoAlKanalo.get(e.kanalNomo.toLowerCase());
            if (k != null) e.kanalNomo = k.nomo;
          }

          if (k == null) {
            Log.d("Nekonata kanalnomo - ALDONAS GXIN: " + e.kanalNomo);
            k = new Kanalo();
            k.json = new JSONObject();
            k.kodo = k.nomo = e.kanalNomo;
            k.datumFonto = "aldonita de radio.txt";
            kanalkodoAlKanalo.put(k.kodo, k);
            kanalnomoAlKanalo.put(k.nomo, k);
            kanaloj.add(k);
          } else if (k.datumFonto==null) {
            k.datumFonto = "radio.txt";
          }
          //Log.d("Aldonas elsendon "+e.toString());
          k.elsendoj.add(e);
        } catch (Exception e) {
          Log.e("Ne povis legi unuon: " + unuo, e);
        }
      }
    }

    for (Kanalo k : kanaloj) Collections.reverse(k.elsendoj);
  }
  
  /**
   * @return true se io estis ŝarĝita
   */
  public void ŝarĝiElsendojnDeRss(boolean nurLokajn) {
    for (Kanalo k : kanaloj) {
      ŝarĝiElsendojnDeRssUrl(k.json.optString("elsendojRssUrl", null), k, nurLokajn);
      ŝarĝiElsendojnDeRssUrl(k.json.optString("elsendojRssUrl1", null), k, nurLokajn);
      //ŝarĝiElsendojnDeRssUrl(k.json.optString("elsendojRssUrl2", null), k, nurLokajn);
    }
  }

  public void ŝarĝiElsendojnDeRssUrl(String elsendojRssUrl, Kanalo k, boolean nurLokajn) {
    if (elsendojRssUrl != null) {
      try {
        Log.d("============ parsas RSS de "+k.kodo+" =============");
        String dosiero = Kasxejo.akiriDosieron(elsendojRssUrl, false, nurLokajn);
        Log.d(" akiris " + elsendojRssUrl);
        if (dosiero == null) return;
        ArrayList<Elsendo> elsendoj;
        if ("vinilkosmo".equals(k.kodo)) {
          elsendoj = RssParsado.parsiElsendojnDeRssVinilkosmo(new FileInputStream(dosiero));
        } else {
          elsendoj = RssParsado.parsiElsendojnDeRss(new FileInputStream(dosiero));
        }
        if (k.json.optBoolean("elsendojRssIgnoruTitolon", false)) for (Elsendo e : elsendoj) e.titolo = null;
        if (elsendoj.size() > 0) {
          if (k.rektaElsendo != null) elsendoj.add(k.rektaElsendo);
          k.elsendoj = elsendoj;
          k.datumFonto = "rss";
        }
        Log.d(" parsis " + elsendojRssUrl + " kaj ricevis " + elsendoj.size() + " elsendojn");
      }catch (Exception ex) {
       Log.e("Eraro parsante " + elsendojRssUrl, ex);
     }
    }
  }

  public void forprenuMalplenajnKanalojn() {
    for (Iterator<Kanalo> ki =this.kanaloj.iterator(); ki.hasNext(); ) {
      Kanalo k = ki.next();
      if (k.elsendoj.isEmpty()) {
        Log.d("============ FORPRENAS "+k.kodo+", ĉar ĝi ne havas elsendojn! "+k.datumFonto);
      }
    }
  }

  public void rezumo() {
    for (Kanalo k : this.kanaloj) {
      Log.d("============ "+k.kodo+" ============= "+k.elsendoj.size()+" "+k.datumFonto);
      int n = 0;
      for (Elsendo e : k.elsendoj) {
        Log.d(n++ +" "+ e.datoStr+" "+e.titolo+" "+e.sonoUrl+" "+e.priskribo);
        if (n>300) {
          Log.d("...");
          break;
        }
      }
    }
  }
}
