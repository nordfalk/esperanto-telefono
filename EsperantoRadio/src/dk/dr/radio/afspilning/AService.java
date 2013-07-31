/**
 Esperanto-radio por Androjd, farita de Jacob Nordfalk. Kelkaj partoj de la kodo originas de DR Radio 2 por Android,
 vidu http://code.google.com/p/dr-radio-android/

 Esperanto-radio por Androjd estas libera softvaro: vi povas redistribui ĝin kaj/aŭ modifi ĝin kiel oni anoncas en la
 licenco GNU Ĝenerala Publika Licenco (GPL) versio 2.

 Esperanto-radio por Androjd estas distribuita en la espero ke ĝi estos utila, sed SEN AJNA GARANTIO; sen eĉ la implica
 garantio de surmerkatigindeco aŭ taŭgeco por iu aparta celo. Vidu la GNU Ĝenerala Publika Licenco por pli da detaloj.

 Vi devus ricevi kopion de la GNU Ĝenerala Publika Licenco kune kun la programo. Se ne, vidu
 <http://www.gnu.org/licenses/>.
 */
package dk.dr.radio.afspilning;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import dk.dr.radio.util.Log;
import dk.nordfalk.esperanto.radio.App;
import dk.nordfalk.esperanto.radio.Datumoj;
import dk.nordfalk.esperanto.radio.Ludado_akt;
import dk.nordfalk.esperanto.radio.R;
import java.lang.reflect.Method;

/**
 Tidligere AfspillerService - service-del der sørger for at app'en bliver i hukommelsen mens der spilles lyd

 @author j
 */
public class AService extends Service {
  /**
   Service-mekanik. Ligegyldig da vi kører i samme proces.
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  private NotificationManager notificationManager;
  private Notification notification;
  private static final Class[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
  private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };
  private Method mStartForeground;
  private Method mStopForeground;
  private Method mSetForeground;
  private Object[] mStartForegroundArgs = new Object[2];
  private Object[] mStopForegroundArgs = new Object[1];
  private String PROGRAMNAVN = "Esperantoradio";
  /**
   ID til notifikation i toppen. Skal bare være unikt og det samme altid
   */
  private static final int NOTIFIKATION_ID = 117;

  @Override
  public void onCreate() {

    if (Datumoj.evoluiganto) {
      Toast.makeText(this, "AfspillerService onCreate", Toast.LENGTH_SHORT).show();
    }
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    try {
      mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
      mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
    } catch (NoSuchMethodException e) {
      // Running on an older platform.
      try {
        mSetForeground = getClass().getMethod("setForeground", mStopForegroundSignature);
      } catch (NoSuchMethodException ex) {
        // Running on an older platform.
        App.eraro(ex);
      }
    }
  }

  @Override
  public void onStart(Intent intent, int startId) {
    handleCommand(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d("AfspillerService onStartCommand(" + intent + " " + flags + " " + startId);
    handleCommand(intent);
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  private void handleCommand(Intent intent) {
    if (Datumoj.evoluiganto) {
      Toast.makeText(this, "AfspillerService onStartCommand(" + intent, Toast.LENGTH_SHORT).show();
    }
    Log.d("AfspillerService handleCommand(" + intent);

    if (notification == null) {
      notification = new Notification(R.drawable.emblemo, null, 0);

      // PendingIntent er til at pege på aktiviteten der skal startes hvis brugeren vælger notifikationen
      notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Ludado_akt.class), 0);
      notification.flags |= (Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT);
    }
    String kanalNavn = intent == null ? null : intent.getStringExtra("kanalNavn");
    if (kanalNavn == null) kanalNavn = "";
    notification.setLatestEventInfo(this, PROGRAMNAVN, kanalNavn, notification.contentIntent);

    // If we have the new startForeground API, then use it.
    if (mStartForeground != null) {
      mStartForegroundArgs[0] = Integer.valueOf(NOTIFIKATION_ID);
      mStartForegroundArgs[1] = notification;
      try {
        mStartForeground.invoke(this, mStartForegroundArgs);
      } catch (Exception e) {
        // Should not happen.
        App.eraro(e);
      }
      return;
    }
    // Fall back on the old API.
    //setForeground(true);
    mStopForegroundArgs[0] = Boolean.TRUE;
    try {
      mSetForeground.invoke(this, mStopForegroundArgs);
    } catch (Exception e) {
      // Should not happen.
      App.eraro(e);
    }
    notificationManager.notify(NOTIFIKATION_ID, notification);
  }

  @Override
  public void onDestroy() {
    if (Datumoj.evoluiganto) {
      Toast.makeText(this, "AfspillerService onDestroy", Toast.LENGTH_SHORT).show();
    }

    // If we have the new stopForeground API, then use it.
    if (mStopForeground != null) {
      mStopForegroundArgs[0] = Boolean.TRUE;
      try {
        mStopForeground.invoke(this, mStopForegroundArgs);
      } catch (Exception e) {
        // Should not happen.
        App.eraro(e);
      }
      return;
    }

    // Fall back on the old API.  Note to cancel BEFORE changing the
    // foreground state, since we could be killed at that point.

    //Log.d("AfspillerService onDestroy!");
    notificationManager.cancelAll(); // Luk notifikationen
    //setForeground(false);
    mStopForegroundArgs[0] = Boolean.FALSE;
    try {
      mSetForeground.invoke(this, mStopForegroundArgs);
    } catch (Exception e) {
      // Should not happen.
      App.eraro(e);
    }
  }
}
