/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.dr.radio.data;

import java.io.File;
import java.io.FileInputStream;

import dk.dr.radio.data.afproevning.FilCache;

/**
 *
 * @author j
 */
public class EoElproviEsperantoRadioLogikon {
  public static final int ĉefdatumojID = 8;
  private static final String ŜLOSILO_ĈEFDATUMOJ = "esperantoradio_kanaloj_v" + ĉefdatumojID;
  private static final String kanalojUrl = "http://javabog.dk/privat/" + ŜLOSILO_ĈEFDATUMOJ + ".json";
  private static final String ŜLOSILO_ELSENDOJ = "elsendoj";
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    System.out.println(  Diverse.unescapeHtml3("&#8217;") );
    System.out.println(  Diverse.unescapeHtml3("Nikolin&#8217; dum la intervjuo.") );
    //Date.parse("Mon, 13 Aug 2012 05:25:10 +0000");
    //Date.parse("Thu, 01 Aug 2013 12:01:01 +02:00");
    DRData.instans = new DRData();

    FilCache.init(new File("datumoj"));
    //String ĉefdatumoj2Str = Kasxejo.hentUrlSomStreng(kanalojUrl);
    String ĉefdatumoj2Str = Diverse.læsStreng(new FileInputStream(
        "../EsperantoRadiov3/res/raw/esperantoradio_kanaloj_v" + ĉefdatumojID + ".json"));
    System.out.println("===================================================================1");
    System.out.println("===================================================================");
    Grunddata ĉefdatumoj2 = new Grunddata();
    System.out.println("===================================================================2");
    System.out.println("===================================================================");
    ĉefdatumoj2.eo_parseFællesGrunddata(ĉefdatumoj2Str);
    System.out.println("===================================================================3");
    System.out.println("===================================================================");
    String radioTxtStr = Diverse.læsStreng(new FileInputStream(FilCache.hentFil(ĉefdatumoj2.radioTxtUrl, true)));
    ĉefdatumoj2.leguRadioTxt(radioTxtStr);
    System.out.println("===================================================================3");
    System.out.println("===================================================================");
    ĉefdatumoj2.ŝarĝiElsendojnDeRss(true);
    //ĉefdatumoj2.ŝarĝiElsendojnDeRssUrl("http://radioverda.squarespace.com/storage/audio/radioverda.xml",
    //ĉefdatumoj2.ŝarĝiElsendojnDeRssUrl("http://radioverda.squarespace.com/programoj/rss.xml",
    //    ĉefdatumoj2.kanalkodoAlKanalo.get("radioverda"), true);


    System.out.println("===================================================================");
    System.out.println("===================================================================");
    System.out.println("===================================================================");
    ĉefdatumoj2.rezumo();
    System.out.println("===================================================================");
    System.out.println("===================================================================");
    System.out.println("===================================================================");
    ĉefdatumoj2.forprenuMalplenajnKanalojn();
  }
}
