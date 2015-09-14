package dk.dr.radio.akt;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Kanal;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.Log;

/**
 * Oprettet af Jacob Nordfalk den 12-09-15.
 */
public class Fragmentfabrikering {

  public static Fragment udsendelse(Udsendelse udsendelse) {
    Fragment fragment = new EoUdsendelse_frag();
    Bundle args = new Bundle();
    args.putString(DRJson.Slug.name(), udsendelse.slug);
    fragment.setArguments(args);
    return fragment;
  }

  public static Fragment kanal(Kanal k) {
    Fragment f = new EoKanal_frag();
    Bundle b = new Bundle();
    b.putString(Kanal_frag.P_kode, k.kode);
    f.setArguments(b);
    return f;
  }
}