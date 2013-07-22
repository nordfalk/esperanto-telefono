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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import dk.nordfalk.esperanto.radio.Datumoj;

/**
 *
 * @author j
 */
public class MedieafspillerInfo {

  public String build_prop_stagefright;

  public String lavTelefoninfo(Context a) {

    String ret ="Program: "+a.getPackageName()+" version "+Datumoj.appInfo.versionName
        +"\nTelefonmodel: "+Build.MODEL +" "+Build.PRODUCT
        +"\nAndroid v"+Build.VERSION.RELEASE
        +"\nsdk: "+Build.VERSION.SDK
        +"\nAndroid_ID: "+Secure.getString(a.getContentResolver(), Secure.ANDROID_ID);

    return ret;
  }

}
