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

import dk.dr.radio.afspilning.Ludado;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.RemoteViews;
import dk.nordfalk.esperanto.radio.R;
import dk.dr.radio.afspilning.AfspillerReciever;
import dk.dr.radio.data.Log;
import dk.nordfalk.esperanto.radio.App;
import dk.nordfalk.esperanto.radio.Datumoj;
import dk.nordfalk.esperanto.radio.Ludado_akt;
import java.util.Arrays;

public class AfspillerWidget extends AppWidgetProvider {
  public void onReceive(Context context, Intent intent) {
    Log.d(this + " onReceive(" + intent);
    super.onReceive(context, intent);
  }

  /**
   * Kaldes når ikonet oprettes
   */
  @Override
  public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    Log.d(this + " onUpdate (levende ikon oprettet) - appWidgetIds = " + Arrays.toString(appWidgetIds));

    try {
      // for sørge for at vores knapper får tilknyttet intentsne
      opdaterUdseende(ctx, appWidgetManager, appWidgetIds[0]);
    } catch (Exception ex) {
      App.eraro(ex);
    }
  }

  /*
   @Override
   public void onDeleted(Context ctx, int[] appWidgetIds) {
   Log.d(this+" onDeleted( widgetId="+widgetId);
   if (widgetId != -1) try {
   Context actx = ctx.getApplicationContext();
   actx.unregisterReceiver(afspillerServiceReciever);
   } catch (Exception e) { Log.e(e); }// Er ikke set ske, men for en sikkerheds skyld
   widgetId = -1;
   super.onDeleted(ctx, appWidgetIds);
   }
   */
  public static void opdaterUdseende(Context ctx, AppWidgetManager appWidgetManager, int appWidgetId) {
    Log.d("AfspillerWidget opdaterUdseende()");
    RemoteViews updateViews = new RemoteViews(ctx.getPackageName(), R.layout.vivanta_emblemo_widget);

    Intent startStopI = new Intent(ctx, AfspillerReciever.class);
    startStopI.putExtra("flag", Ludado.WIDGET_START_ELLER_STOP);
    PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, startStopI, PendingIntent.FLAG_UPDATE_CURRENT);
    updateViews.setOnClickPendingIntent(R.id.startStopKnap, pi);


    Intent åbnAktivitetI = new Intent(ctx, Ludado_akt.class);
    PendingIntent pi2 = PendingIntent.getActivity(ctx, 0, åbnAktivitetI, PendingIntent.FLAG_UPDATE_CURRENT);
    updateViews.setOnClickPendingIntent(R.id.yderstelayout, pi2);


    /*
     boolean visProgressbar = false;
     boolean visKanalnavn = false;
     boolean visKanaltekst = false;
     int startStopKnapResId = R.drawable.widget_radio_play;
     */

    Datumoj drData = Datumoj.instanco;
    if (drData != null) {
      Resources res = ctx.getResources();
      String kanalkode = drData.aktualaKanalkodo;
      // tjek om der er et billede i 'drawable' med det navn filnavn
      int id = res.getIdentifier("kanal_" + kanalkode.toLowerCase(), "drawable", ctx.getPackageName());


      if (id != 0) {
        // Element med billede
        updateViews.setViewVisibility(R.id.kanalnavn, View.GONE);
        updateViews.setViewVisibility(R.id.billede, View.VISIBLE);
        updateViews.setImageViewResource(R.id.billede, id);
      } else {
        // Element uden billede
        updateViews.setViewVisibility(R.id.kanalnavn, View.VISIBLE);
        updateViews.setViewVisibility(R.id.billede, View.GONE);
        updateViews.setTextViewText(R.id.kanalnavn, drData.aktualaKanalo.nomo);
      }



      int afspillerstatus = drData.ludado.getAfspillerstatus();

      if (afspillerstatus == Ludado.STATUSO_HALTIS) {
        updateViews.setImageViewResource(R.id.startStopKnap, R.drawable.widget_radio_play);
        updateViews.setViewVisibility(R.id.progressbar, View.INVISIBLE);
      } else if (afspillerstatus == Ludado.STATUSO_KONEKTAS) {
        updateViews.setImageViewResource(R.id.startStopKnap, R.drawable.widget_radio_stop);
        updateViews.setViewVisibility(R.id.progressbar, View.VISIBLE);
      } else if (afspillerstatus == Ludado.STATUSO_LUDAS) {
        updateViews.setImageViewResource(R.id.startStopKnap, R.drawable.widget_radio_stop);
        updateViews.setViewVisibility(R.id.progressbar, View.INVISIBLE);
      } else {
        Log.e(new Exception("Ugyldig afspillerstatus: " + afspillerstatus));
        updateViews.setImageViewResource(R.id.startStopKnap, R.drawable.icon_playing);
        updateViews.setViewVisibility(R.id.progressbar, View.INVISIBLE);
      }
    } else {
      // Ingen instans eller service oprettet - dvs ludado kører ikke
      updateViews.setImageViewResource(R.id.startStopKnap, R.drawable.widget_radio_play);
      updateViews.setViewVisibility(R.id.progressbar, View.INVISIBLE);
      updateViews.setViewVisibility(R.id.kanalnavn, View.GONE);
      updateViews.setViewVisibility(R.id.billede, View.GONE);
      // Vis P3 i mangel af info om valgt kanal??
      //updateViews.setImageViewResource(R.id.billede, R.drawable.kanal_p3);
    }

    appWidgetManager.updateAppWidget(appWidgetId, updateViews);
  }
}
