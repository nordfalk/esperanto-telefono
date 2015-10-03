package dk.dr.radio.data;

import java.util.Date;

import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;

/**
 * Created by j on 03-10-15.
 */
public class EoKanal extends Kanal {
  private static final long serialVersionUID = 1L;
  /** Finder den aktuelle udsendelse på kanalen */
  @Override
  public Udsendelse getUdsendelse() {
    if (udsendelser==null || udsendelser.size() == 0) return null;
    return udsendelser.get(0);
  }

  @Override
  public boolean harStreams() {
    return true;
  }

  @Override
  public boolean erDirekte() {
    return eo_rektaElsendo!=null;
  }
}
