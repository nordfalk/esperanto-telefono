/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eo.radio.datumoj;

import eo.radio.datumoj.Cxefdatumoj;
import eo.radio.datumoj.Kasxejo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author j
 */
public class ElproviEsperantoRadioLogikon {
  public static final int ĉefdatumojID = 8;
  private static final String ŜLOSILO_ĈEFDATUMOJ = "esperantoradio_kanaloj_v" + ĉefdatumojID;
  private static final String kanalojUrl = "http://javabog.dk/privat/" + ŜLOSILO_ĈEFDATUMOJ + ".json";
  private static final String ŜLOSILO_ELSENDOJ = "elsendoj";
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    //Date.parse("Mon, 13 Aug 2012 05:25:10 +0000");
    //Date.parse("Thu, 01 Aug 2013 12:01:01 +02:00");

    Kasxejo.init(new File("datumoj"));
    //String ĉefdatumoj2Str = Kasxejo.hentUrlSomStreng(kanalojUrl);
    String ĉefdatumoj2Str = Kasxejo.læsInputStreamSomStreng(new FileInputStream(
        "../EsperantoRadio/res/raw/esperantoradio_kanaloj_v" + ĉefdatumojID + ".json"));
    Cxefdatumoj ĉefdatumoj2 = new Cxefdatumoj(ĉefdatumoj2Str);
    String elsendojStr = Kasxejo.hentUrlSomStreng(ĉefdatumoj2.elsendojUrl);
    ĉefdatumoj2.leguElsendojn(elsendojStr);
    ĉefdatumoj2.ŝarĝiElsendojnDeRss(false);
    //ĉefdatumoj2.ŝarĝiElsendojnDeRssUrl("http://radioverda.squarespace.com/storage/audio/radioverda.xml",
    //ĉefdatumoj2.ŝarĝiElsendojnDeRssUrl("http://radioverda.squarespace.com/programoj/rss.xml",
    //    ĉefdatumoj2.kanalkodoAlKanalo.get("radioverda"), true);


    ĉefdatumoj2.rezumo();
    ĉefdatumoj2.forprenuMalplenajnKanalojn();
  }
}
