package dk.dr.radio.akt;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.Collections;

import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.HentedeUdsendelser;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.Sidevisning;
import dk.nordfalk.esperanto.radio.R;

public class Hentede_udsendelser_frag extends Basisfragment implements AdapterView.OnItemClickListener, Runnable, View.OnClickListener {
  private ListView listView;
  private ArrayList<Udsendelse> liste = new ArrayList<Udsendelse>();
  protected View rod;
  HentedeUdsendelser hentedeUdsendelser = DRData.instans.hentedeUdsendelser;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rod = inflater.inflate(R.layout.senest_lyttede, container, false);

    AQuery aq = new AQuery(rod);
    listView = aq.id(R.id.listView).adapter(adapter).itemClicked(this).getListView();
    View emptyView = aq.id(R.id.tom).typeface(App.skrift_gibson)
        .text(Html.fromHtml(getString(R.string.Du_har_ingen_downloads___)))
        .getView();

    listView.setEmptyView(emptyView);
    listView.setCacheColorHint(Color.WHITE);

    aq.id(R.id.overskrift).typeface(App.skrift_gibson_fed).text(R.string.Downloadede_udsendelser).getTextView();


    hentedeUdsendelser.observatører.add(this);
    run();
    udvikling_checkDrSkrifter(rod, this + " rod");
    return rod;
  }

  @Override
  public void onDestroyView() {
    hentedeUdsendelser.observatører.remove(this);
    super.onDestroyView();
  }


  @Override
  public void run() {
    liste.clear();
    liste.addAll(hentedeUdsendelser.getUdsendelser());
    Collections.reverse(liste);
    adapter.notifyDataSetChanged();
  }

  /*
    private static View.OnTouchListener farvKnapNårDenErTrykketNed = new View.OnTouchListener() {
      public boolean onTouch(View view, MotionEvent me) {
        ImageView ib = (ImageView) view;
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
          ib.setColorFilter(App.color.blå, PorterDuff.Mode.MULTIPLY);
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
        } else {
          ib.setColorFilter(null);
        }
        return false;
      }
    };
  */
  private BaseAdapter adapter = new Basisadapter() {
    @Override
    public int getCount() {
      return liste.size();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

      Udsendelse udsendelse = liste.get(position);
      AQuery aq;
      if (v == null) {
        v = getLayoutInflater(null).inflate(R.layout.hentede_udsendelser_listeelem_2linjer, parent, false);
        v.setBackgroundResource(0);
        aq = new AQuery(v);
        aq.id(R.id.startStopKnap).clicked(Hentede_udsendelser_frag.this);
        aq.id(R.id.slet).clicked(Hentede_udsendelser_frag.this);
        aq.id(R.id.hør).clicked(Hentede_udsendelser_frag.this);
//            .getView().setOnTouchListener(farvKnapNårDenErTrykketNed);
        aq.id(R.id.linje1).typeface(App.skrift_gibson_fed);
        aq.id(R.id.linje2).typeface(App.skrift_gibson);
      } else {
        aq = new AQuery(v);
      }
      // Skjul stiplet linje over øverste listeelement
      aq.id(R.id.stiplet_linje).visibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
      aq.id(R.id.startStopKnap).tag(udsendelse); // sæt udsendelsen ind som tag, så vi kan se dem i onClick()
      aq.id(R.id.slet).tag(udsendelse);
      aq.id(R.id.hør).tag(udsendelse);

      Cursor c = hentedeUdsendelser.getStatusCursor(udsendelse);
      if (c == null) {
        aq.id(R.id.startStopKnap).visible().image(R.drawable.dri_radio_spil_graa40);
        aq.id(R.id.progressBar).gone();
        aq.id(R.id.linje1).text(udsendelse.titel).textColor(App.color.grå40);
        aq.id(R.id.linje2).text(DRJson.datoformat.format(udsendelse.startTid) + " - Ikke hentet");
        return v;
      }

      int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
      String statustekst = HentedeUdsendelser.getStatustekst(c);
      int iAlt = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) / 1000000;
      int hentet = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) / 1000000;
      c.close();
      aq.id(R.id.linje2).text(DRJson.datoformat.format(udsendelse.startTid).toUpperCase() + " - " + statustekst.toUpperCase());

      if (status != DownloadManager.STATUS_SUCCESSFUL && status != DownloadManager.STATUS_FAILED) {
        // Genopfrisk hele listen om 1 sekund
        App.forgrundstråd.removeCallbacks(Hentede_udsendelser_frag.this);
        App.forgrundstråd.postDelayed(Hentede_udsendelser_frag.this, 1000);
        ProgressBar progressBar = aq.id(R.id.progressBar).visible().getProgressBar();
        progressBar.setMax(iAlt);
        progressBar.setProgress(hentet);
        aq.id(R.id.startStopKnap).visible().image(R.drawable.dri_radio_stop_graa40);
      } else {
        aq.id(R.id.progressBar).gone();
        aq.id(R.id.startStopKnap).gone();
      }
      aq.id(R.id.linje1).text(udsendelse.titel)
          .textColor(status == DownloadManager.STATUS_SUCCESSFUL ? Color.BLACK : App.color.grå60);

      udvikling_checkDrSkrifter(v, this.getClass() + " ");


      return v;
    }
  };

  @Override
  public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
    Udsendelse udsendelse = liste.get(position);
    visUdsendelse_frag(udsendelse);
  }

  private void visUdsendelse_frag(Udsendelse udsendelse) {
    if (udsendelse == null) return;
    // Tjek om udsendelsen er i RAM, og put den ind hvis den ikke er
    if (!DRData.instans.udsendelseFraSlug.containsKey(udsendelse.slug)) {
      DRData.instans.udsendelseFraSlug.put(udsendelse.slug, udsendelse);
    }
    Fragment f = udsendelse.nytFrag();
    f.setArguments(new Intent()
//        .putExtra(Udsendelse_frag.BLOKER_VIDERE_NAVIGERING, true)
//        .putExtra(P_kode, getKanal.kode)
        .putExtra(DRJson.Slug.name(), udsendelse.slug)
        .getExtras());
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.indhold_frag, f)
        .addToBackStack(null)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .commit();
    Sidevisning.vist(Udsendelse_frag.class, udsendelse.slug);


  }

  @Override
  public void onClick(View v) {
    try {
      final Udsendelse u = (Udsendelse) v.getTag();
      if (v.getId() == R.id.hør) {
        DRData.instans.afspiller.setLydkilde(u);
        DRData.instans.afspiller.startAfspilning();
      } else if (v.getId() == R.id.slet) {
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.Slet_udsendelse)
            .setMessage(R.string.Vil_du_slette_denne_udsendele_du_kan_altid_hente_den_igen_)
            .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface d, int w) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                      // Animeret fjernelse af listeelement
                      int pos = liste.indexOf(u);
                      final View le = listView.getChildAt(pos);
                      if (le==null) { // fix for https://mint.splunk.com/dashboard/project/cd78aa05/errors/2732198295
                        hentedeUdsendelser.slet(u);
                        Log.rapporterFejl(new NullPointerException("sletning index "+pos+" på liste med "+liste.size()+" elementer"));
                        return;
                      }
                      le.animate().alpha(0).translationX(le.getWidth()).withEndAction(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                        @Override
                        public void run() {
                          le.setAlpha(1);
                          le.setTranslationX(0);
                          hentedeUdsendelser.slet(u);
                        }
                      });
                    } else {
                      hentedeUdsendelser.slet(u);
                    }
                  }
                })
            .setNegativeButton(android.R.string.cancel, null)
            .show();

      } else {
        Cursor c = hentedeUdsendelser.getStatusCursor(u);
        if (c != null) {
          hentedeUdsendelser.stop(u);
          c.close();
        } else {
          if (u.streamsKlar()) hentedeUdsendelser.hent(u); // vi har streams, hent dem
          else visUdsendelse_frag(u); // hack - vis udsendelsessiden, den indlæser streamsne
        }
      }
    } catch (Exception e) {
      Log.rapporterFejl(e);
    }
  }
}

