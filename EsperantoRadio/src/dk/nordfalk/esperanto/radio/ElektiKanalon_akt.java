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
package dk.nordfalk.esperanto.radio;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import dk.dr.radio.data.Kanal;
import dk.dr.radio.util.ImageViewTilBlinde;

public class ElektiKanalon_akt extends ListActivity {
  private KanalAdapter adapter;
  private View[] listeElementer;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.elekti_kanalon_akt);

    adapter = new KanalAdapter();
    registerReceiver(ĉefdatumojĜisdatigitajReciever, new IntentFilter(Datumoj.INTENT_novaj_ĉefdatumoj));


    // Da der er tale om et fast lille antal kanaler er der ikke grund til det store bogholderi
    // Så vi husker bare viewsne i er array
    listeElementer = new View[Datumoj.instanco.ĉefdatumoj.kanaler.size()];

    setListAdapter(adapter);
    // Sæt baggrunden normalt ville man gøre det fra XML eller med
    //getListView().setBackgroundResource(R.drawable.main_app_bg);

    ListView lv = getListView();
    /*
     // Vi ønsker en mørkere udgave af baggrunden, så vi indlæser den
     // her og sætter et farvefilter.
     Drawable baggrund = getResources().getDrawable(R.drawable.main_app_bg);
     baggrund = baggrund.mutate();
     baggrund.setColorFilter(0xffa0a0a0, Mode.MULTIPLY);

     lv.setBackgroundDrawable(baggrund);
     */
    int fono = 0xFFe0e0e0;
    lv.setBackgroundColor(fono);
    lv.setDivider(new ColorDrawable(0xff808080)); // 80ffffff
    lv.setDividerHeight(2);

    // Sørg for at baggrunden bliver tegnet, også når listen scroller.
    // Se http://android-developers.blogspot.com/2009/01/why-is-my-list-black-android.html
    lv.setCacheColorHint(fono);
  }
  private BroadcastReceiver ĉefdatumojĜisdatigitajReciever = new BroadcastReceiver() {
    @Override
    public void onReceive(Context ctx, Intent i) {
      //Log.d("stamdataOpdateretReciever elektikanalon");
      listeElementer = new View[Datumoj.instanco.ĉefdatumoj.kanaler.size()];
      adapter.notifyDataSetChanged();
    }
  };

  @Override
  protected void onDestroy() {
    unregisterReceiver(ĉefdatumojĜisdatigitajReciever);
    super.onDestroy();
  }

  private class KanalAdapter extends BaseAdapter {
    public View getView(int position, View convertView, ViewGroup parent) {

      View view = listeElementer[position];

      if (view != null) return view; // Elementet er allede konstrueret

      Kanal kanalo = Datumoj.instanco.ĉefdatumoj.kanaler.get(position);

      //System.out.println("getView " + position + " kanal_" + kanalkode.toLowerCase() + " type = " + id);
      view = mInflater.inflate(R.layout.elekti_kanalon_elemento, null);
      ImageViewTilBlinde billede = (ImageViewTilBlinde) view.findViewById(R.id.billede);
      ImageViewTilBlinde ikon = (ImageViewTilBlinde) view.findViewById(R.id.ikon);
      TextView textView = (TextView) view.findViewById(R.id.tekst);

      //Log.d("billedebilledebilledebillede"+billede+ikon+textView);

      // Sæt og højttalerikon for kanal
      if (Datumoj.instanco.aktualaKanalo == kanalo) {
        ikon.setImageResource(R.drawable.icon_playing);
        ikon.blindetekst = "Spiller nu";
      } else
        ikon.setVisibility(View.INVISIBLE);

      String aldonaTeksto = kanalo.udsendelser.size() > 1 ? " (" + kanalo.udsendelser.size() + ")" : "";
      // tjek om der er et billede i 'drawable' med det navn filnavn
      //int id = res.getIdentifier("kanal_"+kanalkode.toLowerCase(), "drawable", getPackageName());
      Bitmap kanalo_emblemo = Datumoj.instanco.emblemoj.get(kanalo.emblemoUrl);
      if (kanalo_emblemo != null) {
        // Element med billede
        billede.setVisibility(View.VISIBLE);
        billede.setImageBitmap(kanalo_emblemo);
        billede.blindetekst = kanalo.nomo;
        if (kanalo_emblemo.getWidth() < kanalo_emblemo.getHeight() * 2) {
          // Emblemo kun teksto
          textView.setText(kanalo.nomo + aldonaTeksto);
        } else {
          // Emblemo kiu enhavas la tekston - do ne montru gxin
          //textView.setText(aldonaTeksto);
          textView.setVisibility(View.GONE);
          textView.setText("");
          // La bildo plenigu la tutan largxon
          billede.getLayoutParams().width = LayoutParams.FILL_PARENT;
        }
      } else {
        // Element uden billede
        billede.setVisibility(View.GONE);
        textView.setText(kanalo.nomo + aldonaTeksto);
      }
      textView.setVisibility(View.VISIBLE);


      listeElementer[position] = view; // husk til næste gang
      return view;
    }
    LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Resources res = getResources();

    public int getCount() {
      return Datumoj.instanco.ĉefdatumoj.kanaler.size();
    }

    public Object getItem(int position) {
      return null;
    }

    public long getItemId(int position) {
      return position;
    }
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    String kanalkode = Datumoj.instanco.ĉefdatumoj.kanaler.get(position).kodo;


    //Kanal kanal = drData.stamdata.kanalkodoAlKanalo.get(kanalkode);
    //Toast.makeText(this, "Klik på "+position+" "+kanal.nomo, Toast.LENGTH_LONG).show();

    if (kanalkode.equals(Datumoj.instanco.aktualaKanalkodo)) setResult(RESULT_CANCELED);
    else setResult(RESULT_OK);  // Signalér til kalderen at der er skiftet kanal!!

    // Ny kanal valgt - send valg til ludado (ændrer også drData.aktualaKanalkodo)
    Datumoj.instanco.ŝanĝiKanalon(kanalkode);

    // Hop tilbage til kalderen (hovedskærmen)
    finish();
  }
}
