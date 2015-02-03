package dk.dr.radio.data;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Date;

import dk.dr.radio.akt.EoUdsendelse_frag;
import dk.dr.radio.akt.Udsendelse_frag;
import dk.dr.radio.diverse.Log;

/**
 * Repræsenterer en udsendelse
 * Created by j on 28-01-14.
 */
public class Udsendelse extends Lydkilde implements Comparable<Udsendelse>, Cloneable {
  // Fix for https://www.bugsense.com/dashboard/project/cd78aa05/errors/1415558087
  // - at proguard obfuskering havde
  // Se også http://stackoverflow.com/questions/16210831/serialization-deserialization-proguard
  private static final long serialVersionUID = -9161602458987716481L;

  public String titel;
  public String beskrivelse;
  public String billedeUrl; // Bemærk - kan være tom
  public String kanalSlug;  // Bemærk - kan være tom!
  public String programserieSlug;  // Bemærk - kan være tom!

  public Date startTid;
  public String startTidKl;
  public Date slutTid;
  public String slutTidKl;
  public String dagsbeskrivelse;

  public transient ArrayList<Playlisteelement> playliste;
  /** API'ets udmelding på, om der er nogle streams eller ej. Desværre er API'et ikke pålideligt, så den eneste måde reelt at vide det er faktisk at hente streamsne */
  public boolean kanNokHøres;
  /** Efter at streams er hentet, om der er en egnet streams til direkte afspilning */
  public boolean kanStreames;
  /** Efter at streams er hentet, om der er mulighed for at hente udsendelsen ned til offline brug */
  public boolean kanHentes;
  public String produktionsnummer;
  public String shareLink;
  //public transient int startposition;// hvis der allerede er lyttet til denne senestLyttet så notér det her så afspilning kan fortsætte herfra
  public int episodeIProgramserie;

  //// EO
  public ArrayList<String> sonoUrl = new ArrayList<String>();
  public String rektaElsendaPriskriboUrl;
  public String ligilo;
  //// EO

  public Udsendelse(String s) {
    titel = s;
  }

  public Udsendelse() {
  }

/*
  public String toString() {
    return kanalSlug + startTid + (beskrivelse.length() > 30 ? beskrivelse.substring(0, 15) + "..." : beskrivelse);
  }
*/
@Override
public String toString() {
  return slug + "/" + episodeIProgramserie;//startTid + "/" + slutTid;
}

  @Override
  public String getStreamsUrl() {
    return DRData.getUdsendelseStreamsUrlFraUrn(urn);
  }


  @Override
  public Kanal getKanal() {
    Kanal k = DRData.instans.grunddata.kanalFraSlug.get(kanalSlug);
    if (k == null) {
      Log.d(kanalSlug + " manglede i grunddata.kanalFraSlug - der var "+DRData.instans.grunddata.kanalFraSlug.keySet());
      return Grunddata.ukendtKanal;
    }
    //if (Kanal.P4kode.equals(k.kode)) {
    //  Log.rapporterFejl(new IllegalStateException("Vi fik P4 overkanalen - ved ikke hvilken underkanal"), kanalSlug);
    //  return Grunddata.ukendtKanal;
    //}
    return k;
  }

  @Override
  public boolean erDirekte() {
    return false;
  }

  @Override
  public Udsendelse getUdsendelse() {
    return this;
  }

  @Override
  public String getNavn() {
    return titel;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Udsendelse)) return false;
    Udsendelse u = (Udsendelse) o;
    if (slug != null) return slug.equals(u.slug);
    return false;
  }

  @Override
  public int compareTo(Udsendelse u2) {
    int e = episodeIProgramserie;
    int e2 = u2.episodeIProgramserie;
    if (e != e2) return e2 < e ? -1 : 1;
    if (slug == null) return u2.slug == null ? 0 : 1;
    return slug.compareTo(u2.slug);
  }

  public boolean streamsKlar() {
    return hentetStream != null || streams != null && streams.size() > 0;
  }

  /**
   * Finder index på playlisteelement, der spiller på et bestemt offset i udsendelsen
   * @param offsetMs Tidspunkt
   * @param indeks   Gæt på index, f.eks fra sidste kald
   * @return korrekt indeks
   */
  public int findPlaylisteElemTilTid(int offsetMs, int indeks) {
    if (playliste == null || playliste.size() == 0) return -1;
    if (indeks < 0 || playliste.size() <= indeks) {
      indeks = 0;
    } else if (offsetMs < playliste.get(indeks).offsetMs - 10000) {
      indeks = 0; // offsetMs mere end 10 sekunder tidligere end startgæt => søg fra starten
    }
    ;

    // Søg nu fremad til næste nummer er for langt
    while (indeks < playliste.size() - 1 && playliste.get(indeks + 1).offsetMs < offsetMs) {
      Log.d("findPlaylisteElemTilTid() skip playliste[" + indeks + "].offsetMs=" + playliste.get(indeks).offsetMs);
      indeks++;
    }
    return indeks;
  }

  public Fragment nytFrag() {
    if (getKanal().eo_datumFonto!=null) return new EoUdsendelse_frag();
    return new Udsendelse_frag();
  }

  public Udsendelse kopio() {
    try {
      return (Udsendelse) this.clone();
    } catch (Exception e) {
      Log.rapporterFejl(e);
    }
    return this;
  }
}
