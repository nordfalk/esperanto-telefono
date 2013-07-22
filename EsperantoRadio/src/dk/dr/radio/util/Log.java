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

package dk.dr.radio.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import dk.nordfalk.esperanto.radio.Datumoj;

/**
 * Loggerklasse
 * - hvor man slipper for at angive tag
 * - man kan logge objekter (få kaldt toString)
 * - cirkulær buffer tillader at man kan gemme loggen til fejlrapportering
 * @author j
 */
public class Log {
  public static final String TAG = "EoRadio";

  // Fjernet da det ser ud til at overbelaste regnearket
  public static final boolean RAPPORTER_VELLYKKET_AFSPILNING = false;

  public static StringBuilder log = new StringBuilder(18000);

  private static void logappend(String s) {
    s = s.trim();
    if (s.length()==0) return;
    // Roterende log
    log.append(s);
    log.append('\n');
    if (log.length()>17500) log.delete(0, 7000);
  }

  /** Logfunktion uden TAG som tager et objekt. Sparer bytekode og tid */
  public static void d(Object o) {
    String s = String.valueOf(o);
    android.util.Log.d(TAG, s);
    logappend(s);
  }

  public static void e(Exception e) {
    e("fejl", e);
  }

  public static void e(String tekst, Exception e) {
    android.util.Log.e(TAG, tekst, e);
    //e.printStackTrace();
    logappend(android.util.Log.getStackTraceString(e));
    //NEJ; IKKE FORGRUNDSTRÅD!!! if (Datumoj.evoluiganto) Toast.makeText(Datumoj.appCtx, tekst+"\n"+e, Toast.LENGTH_SHORT).show();
  }


  public static void kritiskFejlStille(final Exception e) {
    BugSenseHandler.sendException(e);
    Log.e(e);
  }


  public static void kritiskFejl(final Context akt, final Exception e) {
    BugSenseHandler.sendException(e);
    Log.e(e);

    Builder ab=new AlertDialog.Builder(akt);
    ab.setTitle("Bedaŭrinde okazis eraro");
    ab.setMessage(e.toString());
    ab.setNegativeButton("Ignori", null);
    ab.setPositiveButton("Raporti", new Dialog.OnClickListener() {
      public void onClick(DialogInterface arg0, int arg1) {
        String brødtekst = "Skribu kio okazis:\n\n\n---\n";
        brødtekst += "\nErarsxpuro;\n"+android.util.Log.getStackTraceString(e);
        brødtekst += "\n\n" + new MedieafspillerInfo().lavTelefoninfo(akt);
        Kontakt.kontakt(akt, "Eraro en EsperantoRadio", brødtekst, Log.log.toString());
      }

    });
    ab.create().show();
  }
}
