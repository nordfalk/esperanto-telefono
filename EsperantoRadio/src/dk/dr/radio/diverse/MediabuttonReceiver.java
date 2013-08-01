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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import eo.radio.datumoj.Log;
import android.view.KeyEvent;
import dk.dr.radio.afspilning.Ludado;
import dk.dr.radio.afspilning.AfspillerReciever;

public class MediabuttonReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("MediabuttonReciever recived event.");

    String intentAction = intent.getAction();

    if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
      return;
    }

    KeyEvent MediaButtonEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

    if (MediaButtonEvent == null) {
      return;
    }

    int keycode = MediaButtonEvent.getKeyCode();
    int action = MediaButtonEvent.getAction();

    if (action == KeyEvent.ACTION_DOWN) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      Boolean MediaButtonEnable = prefs.getBoolean("MediaButtonEnable", false);

      if (MediaButtonEnable) {
        switch (keycode) {

          case KeyEvent.KEYCODE_HEADSETHOOK:
          case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            Log.d("MediabuttonReciever supported event.");
            Intent startStopI = new Intent(context, AfspillerReciever.class);
            startStopI.putExtra("flag", Ludado.WIDGET_START_ELLER_STOP);
            context.sendBroadcast(startStopI);
            break;
          case KeyEvent.KEYCODE_MEDIA_NEXT:
          case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
          case KeyEvent.KEYCODE_MEDIA_STOP:
          default:
            Log.d("MediabuttonReciever got not yet supported media key enent.");
            return;
        }

        //Do not send the broadcast to the receivers with
        //lower priority
        abortBroadcast();

      }
    }
  }
}