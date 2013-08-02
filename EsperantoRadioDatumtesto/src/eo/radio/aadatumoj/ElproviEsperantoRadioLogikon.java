/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eo.radio.aadatumoj;

import eo.radio.datumoj.Cxefdatumoj;
import eo.radio.datumoj.Kasxejo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
    Kasxejo.init(new File("datumoj"));
    //String ĉefdatumoj2Str = Kasxejo.hentUrlSomStreng(kanalojUrl);
    String ĉefdatumoj2Str = Kasxejo.læsInputStreamSomStreng(new FileInputStream(
        "../EsperantoRadio/res/raw/esperantoradio_kanaloj_v" + ĉefdatumojID + ".json"));
    final Cxefdatumoj ĉefdatumoj2 = new Cxefdatumoj(ĉefdatumoj2Str);
    ĉefdatumoj2.ŝarĝiElsendojnDeRss(false);
  }
}
