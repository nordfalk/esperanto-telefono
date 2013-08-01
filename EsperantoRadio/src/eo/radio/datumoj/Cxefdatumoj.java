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
  public Cxefdatumoj(String stamdataStr) throws JSONException {
    leguKanalojn(stamdataStr);
  }

  private void leguKanalojn(String stamdataStr) throws JSONException {
    Cxefdatumoj d = this;
    JSONObject json = d.json = new JSONObject(stamdataStr);

    JSONArray kanaloj = json.getJSONArray("kanaloj");
    int antal = kanaloj.length();
    for (int i = 0; i < antal; i++) {
      JSONObject j = kanaloj.getJSONObject(i);
      Kanalo k = new Kanalo();
      k.kodo = j.getString("kodo");
      k.nomo = j.getString("nomo");
      String rektaElsendaSonoUrl = j.optString("rektaElsendaSonoUrl", null);
      String rektaElsendaPriskriboUrl = j.optString("rektaElsendaPriskriboUrl", null);
      k.hejmpaĝoEkrane = j.optString("hejmpaĝoEkrane", null);
      k.hejmpaĝoButono = j.optString("hejmpaĝoButono", null);
      k.retpoŝto = j.optString("retpoŝto", null);
      k.emblemoUrl = j.optString("emblemoUrl", null);
      k.json = j;
      d.kanaloj.add(k);

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


    for (Kanalo k : d.kanaloj) {
      d.kanalkodoAlKanalo.put(k.kodo, k);
      d.kanalnomoAlKanalo.put(k.nomo, k);
    }

    // Erstat med evt ny værdi
    elsendojUrl = json.optString("elsendojUrl", elsendojUrl);
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
            kanalkodoAlKanalo.put(k.kodo, k);
            kanalnomoAlKanalo.put(k.nomo, k);
            kanaloj.add(k);
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
  public boolean ŝarĝiElsendojnDeRss(boolean nurLokajn) {
    boolean ioEstisSxargxita = false;
    long komenco = System.currentTimeMillis();
    for (Kanalo k : this.kanaloj) {
      String elsendojRssUrl = k.json.optString("elsendojRssUrl", null);
      if (elsendojRssUrl != null) try {
          String dosiero = Kasxejo.akiriDosieron(elsendojRssUrl, false, nurLokajn);
          Log.d((System.currentTimeMillis() - komenco) + " akiris " + elsendojRssUrl);
          if (dosiero == null) continue;
          ArrayList<Elsendo> elsendoj = RssParsado.parsiElsendojnDeRss(new FileInputStream(dosiero));
          if (k.json.optBoolean("elsendojRssIgnoruTitolon", false)) for (Elsendo e : elsendoj) e.titolo = null;
          if (elsendoj.size() > 0) {
            if (k.rektaElsendo != null) elsendoj.add(k.rektaElsendo);
            k.elsendoj = elsendoj;
            ioEstisSxargxita = true;
          }
          Log.d((System.currentTimeMillis() - komenco) + " parsis " + elsendojRssUrl + " kaj ricevis " + elsendoj.size() + " elsendojn");
        } catch (Exception ex) {
          Log.e("Eraro parsante " + elsendojRssUrl, ex);
        }
    }
    return ioEstisSxargxita;
  }
}
