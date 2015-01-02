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
package dk.dr.radio.afspilning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import dk.nordfalk.esperanto.radio.Datumoj;
import dk.dr.radio.data.Log;
import dk.nordfalk.esperanto.radio.App;

/**
 * BroadcastReceiver som aktiverer afspilleren og evt instantierer den.
 * I tilfælde af at processen har været smidt ud af hukommelsen er dette
 * her faktisk den første kode der køres, derfor er et fuldt
 * initialiseringstjek nødvendigt
 * @author j
 */
public class AfspillerReciever extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      Ludado afspiller = Datumoj.instanco.ludado;
      int flag = intent.getIntExtra("flag", 0);
      Log.d("AfspillerReciever onReceive(" + intent + ") flag " + flag + " afspillerstatus =" + afspiller.ludadstatuso);


      if (flag == Ludado.WIDGET_START_ELLER_STOP) {
        if (afspiller.ludadstatuso == Ludado.STATUSO_HALTIS) {
          afspiller.startiLudadon();
        } else {
          afspiller.stopAfspilning();
        }
      }

    } catch (Exception ex) {
      App.eraro(ex);
    }
  }
}
