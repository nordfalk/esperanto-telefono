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
package dk.dr.radio.diverse;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import dk.dr.radio.util.Log;
import dk.dr.radio.afspilning.Ludado;


/*
 * Denne klasse sørger for at stoppe afspilning hvis telefonen ringer
 */
public class Opkaldshaandtering extends PhoneStateListener {
  private Ludado service;
  private boolean venterPåKaldetAfsluttes;

  public Opkaldshaandtering(Ludado service) {
    this.service = service;
  }

  @Override
  public void onCallStateChanged(int state, String incomingNumber) {
    int afspilningsstatus = service.getAfspillerstatus();
    switch (state) {
      case TelephonyManager.CALL_STATE_OFFHOOK:
        Log.d("Offhook state detected");
        if (afspilningsstatus != Ludado.STATUSO_HALTIS) {
          venterPåKaldetAfsluttes = true;
          service.stopAfspilning();
        }
        break;
      case TelephonyManager.CALL_STATE_RINGING:
        Log.d("Ringing detected");
        if (afspilningsstatus != Ludado.STATUSO_HALTIS) {
          venterPåKaldetAfsluttes = true;
          service.stopAfspilning();
        }
        break;
      case TelephonyManager.CALL_STATE_IDLE:
        Log.d("Idle state detected");
        if (venterPåKaldetAfsluttes) {
          try {
            service.startiLudadon();
          } catch (Exception e) {
            Log.e(e);
          }
          venterPåKaldetAfsluttes = false;
        }
        break;
      default:
        Log.d("Unknown phone state=" + state);
    }
  }
}
