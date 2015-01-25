package dk.dr.radio.akt;

import android.content.Intent;
import android.graphics.Color;
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

import com.android.volley.Request;
import com.androidquery.AQuery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Favoritter;
import dk.dr.radio.data.Programserie;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.Sidevisning;
import dk.dr.radio.diverse.volley.DrVolleyResonseListener;
import dk.dr.radio.diverse.volley.DrVolleyStringRequest;
import dk.nordfalk.esperanto.radio.R;

public class Favoritprogrammer_frag extends Basisfragment implements AdapterView.OnItemClickListener, Runnable {

  private ListView listView;
  private ArrayList<Object> liste = new ArrayList<Object>(); // Indeholder både udsendelser og -serier
  protected View rod;
  Favoritter favoritter = DRData.instans.favoritter;
  private static long sidstOpdateretAntalNyeUdsendelser;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rod = inflater.inflate(R.layout.senest_lyttede, container, false);

    AQuery aq = new AQuery(rod);
    listView = aq.id(R.id.listView).adapter(adapter).itemClicked(this).getListView();
    listView.setEmptyView(aq.id(R.id.tom).typeface(App.skrift_gibson).text(
//        "Ingen favoritter\nGå ind på en programserie og tryk på hjertet for at gøre det til en favorit"
            Html.fromHtml("<b>Saml dine favoritter her</b><br><br>Klik på hjertet på dine yndlingsprogrammer. Du får nem adgang til dine favoritter – og du kan hurtigt se, når der er kommet nye udsendelser.")

        ).getView()
    );
    listView.setCacheColorHint(Color.WHITE);

    aq.id(R.id.overskrift).typeface(App.skrift_gibson_fed).text("Dine favoritter").getTextView();

    favoritter.observatører.add(this);
    run();
    if (favoritter.getAntalNyeUdsendelser() < 0 || sidstOpdateretAntalNyeUdsendelser > System.currentTimeMillis() + 1000 * 60 * 10) {
      // Opdatering af nye antal udsendelser er ikke sket endnu - eller det er mere end end ti minutter siden.
      DRData.instans.favoritter.startOpdaterAntalNyeUdsendelser.run();
      sidstOpdateretAntalNyeUdsendelser = System.currentTimeMillis();
    }

    udvikling_checkDrSkrifter(rod, this + " rod");
    return rod;
  }

  @Override
  public void onDestroyView() {
    favoritter.observatører.remove(this);
    super.onDestroyView();
  }


  @Override
  public void run() {
    App.forgrundstråd.removeCallbacks(this); // Ingen gentagne kald
    liste.clear();
    try {
      ArrayList<String> pss = new ArrayList<String>(favoritter.getProgramserieSlugSæt());
      Collections.sort(pss);
      Log.d(this + " psss = " + pss);
      for (final String programserieSlug : pss) {
        Programserie programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
        if (programserie != null) liste.add(programserie);
        else {
          if (DRData.instans.programserieSlugFindesIkke.contains(programserieSlug)) continue;
          Log.d("programserieSlug gav ingen værdi, henter for " + programserieSlug);
          final int offset = 0;
          String url = DRData.getProgramserieUrl(programserieSlug) + "&offset=" + offset;
          Request<?> req = new DrVolleyStringRequest(url, new DrVolleyResonseListener() {
            @Override
            public void fikSvar(String json, boolean fraCache, boolean uændret) throws Exception {
              Log.d("favoritter fikSvar(" + fraCache + " " + url);
              if (uændret) return;
              if (json != null && !"null".equals(json)) {
                JSONObject data = new JSONObject(json);
                Programserie programserie = DRJson.parsProgramserie(data, null);
                JSONArray prg = data.getJSONArray(DRJson.Programs.name());
                ArrayList<Udsendelse> udsendelser = DRJson.parseUdsendelserForProgramserie(prg, null, DRData.instans);
                programserie.tilføjUdsendelser(offset, udsendelser);
                DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
              } else {
                DRData.instans.programserieSlugFindesIkke.add(programserieSlug);
                Log.d("programserieSlugFindesIkke for " + programserieSlug);
              }
              App.forgrundstråd.postDelayed(Favoritprogrammer_frag.this, 250); // Vent 1/4 sekund på eventuelt andre svar
            }
          });
          App.volleyRequestQueue.add(req);
        }
      }
      Log.d(this + " liste = " + liste);
    } catch (Exception e1) {
      Log.rapporterFejl(e1);
    }
    adapter.notifyDataSetChanged();
  }


  private BaseAdapter adapter = new Basisadapter() {
    @Override
    public int getCount() {
      return liste.size();
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
      try {
        if (v == null) v = getLayoutInflater(null).inflate(R.layout.listeelem_2linjer, parent, false);
        AQuery aq = new AQuery(v);

        Object obj = liste.get(position);
        if (obj instanceof Programserie) {
          Programserie ps = (Programserie) obj;
          aq.id(R.id.linje1).text(ps.titel).typeface(App.skrift_gibson_fed).textColor(Color.BLACK);
          int n = favoritter.getAntalNyeUdsendelser(ps.slug);
          String txt = (n == 1 ? n + " ny udsendelse" : n + " nye udsendelser");
          aq.id(R.id.linje2).text(txt).typeface(App.skrift_gibson);
          aq.id(R.id.stiplet_linje).visibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        } else {
          Udsendelse udsendelse = (Udsendelse) obj;
          aq.id(R.id.linje1).text(DRJson.datoformat.format(udsendelse.startTid)).typeface(App.skrift_gibson);
          aq.id(R.id.linje2).text(udsendelse.titel).typeface(App.skrift_gibson);
          aq.id(R.id.stiplet_linje).visibility(View.VISIBLE);
        }
        v.setBackgroundResource(0);


        udvikling_checkDrSkrifter(v, this.getClass() + " ");
      } catch (Exception e) {
        Log.rapporterFejl(e);
      }

      return v;
    }
  };

  @Override
  public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
    Object obj = liste.get(position);
    if (obj instanceof Programserie) {
      Programserie programserie = (Programserie) obj;
      Fragment f = new Programserie_frag();
      f.setArguments(new Intent()
          .putExtra(DRJson.SeriesSlug.name(), programserie.slug)
          .getExtras());
      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.indhold_frag, f)
          .addToBackStack(null)
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          .commit();
      Sidevisning.vist(Programserie_frag.class, programserie.slug);

    } else {
      Udsendelse udsendelse = (Udsendelse) obj;
      Fragment f = new Udsendelse_frag();
      f.setArguments(new Intent()
//        .putExtra(Udsendelse_frag.BLOKER_VIDERE_NAVIGERING, true)
//        .putExtra(P_kode, titel.kode)
          .putExtra(DRJson.Slug.name(), udsendelse.slug)
          .getExtras());
      getActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.indhold_frag, f)
          .addToBackStack(null)
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          .commit();
      Sidevisning.vist(Udsendelse_frag.class, udsendelse.slug);

    }

  }

}

