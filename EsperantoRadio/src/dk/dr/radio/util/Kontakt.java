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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import dk.nordfalk.esperanto.radio.Datumoj;
import dk.nordfalk.esperanto.radio.datumoj.Utilajxoj;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author j
 */
public class Kontakt {



  public static void kontakt(Context akt, String emne, String txt, String vedhæftning) {

    String[] modtagere = null;
    try {
      modtagere = Utilajxoj.jsonArrayTilArrayListString(Datumoj.instans.stamdata.json.getJSONArray("feedback_modtagere")).toArray(new String[0]);
    } catch (Exception ex) {
      Log.e("JSONParsning af feedback_modtagere", ex);
      modtagere = new String[] { "jacob.nordfalk@gmail.com"};
    }
    kontakt(akt, modtagere, emne, txt, vedhæftning);
  }

	public static void kontakt(Context akt, String[] modtagere, String emne, String txt, String vedhæftning) {
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
    i.setType("text/plain");
		i.putExtra(android.content.Intent.EXTRA_EMAIL, modtagere);
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, emne);

    // Fejler i Android 4.1 Jelly Bean med
    //  file:// attachment paths must point to file:///storage/sdcard0. Ignoring attachment [obscured file path]
    // Løsning: Se http://stephendnicholas.com/archives/974
    if (vedhæftning!=null) try {
      String xmlFilename = "protokolo.txt";
      FileOutputStream fos = akt.openFileOutput(xmlFilename, akt.MODE_WORLD_READABLE);
      fos.write(vedhæftning.getBytes());
      fos.close();
      Uri uri = Uri.fromFile(new File("/mnt/sdcard/../.."+akt.getFilesDir()+"/"+xmlFilename));
      i.putExtra(android.content.Intent.EXTRA_STREAM, uri);
    } catch (Exception e) {
      Log.e(e);
      txt += "\n"+e;
    }
		i.putExtra(android.content.Intent.EXTRA_TEXT, txt);
		akt.startActivity(Intent.createChooser(i, null));
	}
}
