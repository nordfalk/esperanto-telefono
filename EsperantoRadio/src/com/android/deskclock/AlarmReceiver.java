/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.deskclock;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.media.AudioManager;
import android.os.PowerManager.WakeLock;
import dk.dr.radio.afspilning.Ludado;
import dk.nordfalk.esperanto.radio.Datumoj;
import dk.nordfalk.esperanto.radio.Ludado_akt;
import dk.nordfalk.esperanto.radio.datumoj.Kanalo;

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {
  /** If the alarm is older than STALE_WINDOW, ignore.  It
   is probably the result of a time or timezone change */
  private final static int STALE_WINDOW = 30 * 60 * 1000;

  @Override
  public void onReceive(final Context context, final Intent intent) {
    Log.v("AlarmReceiver onReceive(" + intent);
    handleIntent(context, intent);
    //XXX TODO Se om det hjælper wakeLock.release();
  }

  private void handleIntent(Context context, Intent intent) {
    if (!Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
      // Unknown intent, bail.
      return;
    }

    Alarm alarm = null;
    // Grab the alarm from the intent. Since the remote AlarmManagerService
    // fills in the Intent to add some extra data, it must unparcel the
    // Alarm object. It throws a ClassNotFoundException when unparcelling.
    // To avoid this, do the marshalling ourselves.
    final String data = intent.getStringExtra(Alarms.ALARM_RAW_DATA);
    if (data != null) {
      alarm = new Alarm(data);
    }

    Alarms.tjekIndlæst(context);



    if (alarm == null) {
      Log.wtf("Failed to parse the alarm from the intent");
      // Make sure we set the next alert if needed.
      Alarms.setNextAlert(context);
      return;
    }

    // Disable this alarm if it does not repeat.
    if (!alarm.daysOfWeek.isRepeatSet()) {
      alarm.enabled = false;
      Alarms.setAlarm(context, alarm);
    } else {
      // Enable the next alert if there is one. The above call to
      // enableAlarm will call setNextAlert so avoid calling it twice.
      Alarms.setNextAlert(context);
    }

    // Intentionally verbose: always log the alarm time to provide useful
    // information in bug reports.
    long now = System.currentTimeMillis();
    Log.v("Recevied alarm set for " + Log.formatTime(alarm.time));

    // Always verbose to track down time change problems.
    if (now > alarm.time + STALE_WINDOW) {
      Log.v("Ignoring stale alarm");
      return;
    }


    /* Close dialogs and window shade */
    Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    context.sendBroadcast(closeDialogs);


    // Play the alarm alert and vibrate the device.
    Intent playAlarm = new Intent(context, Ludado_akt.class);
    //playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm.toString());
    playAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(playAlarm);



    try {
      Datumoj datumoj = Datumoj.kontroluInstanconSxargxita(context);
      Kanalo nyKanal = Datumoj.instans.stamdata.kanalkodoAlKanalo.get(alarm.kanalo);
      if (nyKanal == null) {
        Log.wtf("Alarm: Kanal findes ikke!" + alarm.kanalo + " for alarmstr=" + data);
        datumoj.skiftKanal(datumoj.stamdata.kanaloj.get(0).kodo);
      } else {
        datumoj.skiftKanal(alarm.kanalo);
      }

      Ludado ludado = datumoj.ludado;
      ludado.setKanalon(datumoj.aktualaKanalo.nomo, datumoj.aktualaElsendo.sonoUrl);
      if (ludado.getAfspillerstatus() == Ludado.STATUSO_HALTIS) {
        ludado.startiLudadon();
      }
      ludado.eraroSignifasBrui = true;
      ludado.wakeLock = AlarmAlertWakeLock.createPartialWakeLock(context);
      ludado.wakeLock.acquire(); // preferus temon, eble 120000 ĉi tie,
      Log.v("AlarmReceiver AlarmAlertWakeLock.createPartialWakeLock()");

      // Skru op til 3/5 styrke hvis volumen er lavere end det
      Ludado.minimumaLaŭteco(3);

    } catch (Exception ex) {
      Log.e("argh!", ex);
    }


    /*
     // Trigger a notification that, when clicked, will show the alarm alert
     // dialog. No need to check for fullscreen since this will always be
     // launched from a user action.
     Intent notify = new Intent(context, SettingsActivity.class);
     notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
     PendingIntent pendingNotify = PendingIntent.getActivity(context,
     alarm.id, notify, 0);

     // Use the alarm's label or the default label as the ticker text and
     // main text of the notification.
     String label = alarm.getLabelOrDefault(context);
     Notification n = new Notification(R.drawable.stat_notify_alarm,
     label, alarm.time);
     n.setLatestEventInfo(context, label,
     context.getString(R.string.alarm_notify_text),
     pendingNotify);
     n.flags |= Notification.FLAG_SHOW_LIGHTS
     | Notification.FLAG_ONGOING_EVENT;
     n.defaults |= Notification.DEFAULT_LIGHTS;

     // NEW: Embed the full-screen UI here. The notification manager will
     // take care of displaying it if it's OK to do so.
     Intent alarmAlert = new Intent(context, c);
     alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
     alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
     | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
     n.fullScreenIntent = PendingIntent.getActivity(context, alarm.id, alarmAlert, 0);

     // Send the notification using the alarm id to easily identify the
     // correct notification.
     NotificationManager nm = getNotificationManager(context);
     nm.notify(alarm.id, n);
     */
  }

  /*
   private NotificationManager getNotificationManager(Context context) {
   return (NotificationManager)
   context.getSystemService(Context.NOTIFICATION_SERVICE);
   }
   private void updateNotification(Context context, Alarm alarm, int timeout) {
   NotificationManager nm = getNotificationManager(context);

   // If the alarm is null, just cancel the notification.
   if (alarm == null) {
   if (Log.LOGV) {
   Log.v("Cannot update notification for killer callback");
   }
   return;
   }

   // Launch SetAlarm when clicked.
   Intent viewAlarm = new Intent(context, SetAlarm.class);
   viewAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm.toString());
   PendingIntent intent =
   PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);

   // Update the notification to indicate that the alert has been
   // silenced.
   String label = alarm.getLabelOrDefault(context);
   Notification n = new Notification(R.drawable.stat_notify_alarm,
   label, alarm.time);
   n.setLatestEventInfo(context, label,
   context.getString(R.string.alarm_alert_alert_silenced, timeout),
   intent);
   n.flags |= Notification.FLAG_AUTO_CANCEL;
   // We have to cancel the original notification since it is in the
   // ongoing section and we want the "killed" notification to be a plain
   // notification.
   nm.cancel(alarm.id);
   nm.notify(alarm.id, n);
   }
   */
}
