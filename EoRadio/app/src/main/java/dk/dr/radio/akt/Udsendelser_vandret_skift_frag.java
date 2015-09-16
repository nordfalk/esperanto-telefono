package dk.dr.radio.akt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Kanal;
import dk.dr.radio.data.Programserie;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.Sidevisning;
import dk.dr.radio.net.volley.DrVolleyResonseListener;
import dk.dr.radio.net.volley.DrVolleyStringRequest;
import dk.dr.radio.v3.R;

public class Udsendelser_vandret_skift_frag extends Basisfragment implements ViewPager.OnPageChangeListener {

  private ViewPager viewPager;

  private Udsendelse startudsendelse;
  private Programserie programserie;
  private ArrayList<Udsendelse> liste;
  private Kanal kanal;
  private UdsendelserAdapter adapter;
  private int antalHentedeSendeplaner;
  private View pager_title_strip;

  @Override
  public String toString() {
    return super.toString() + "/" + kanal + "/" + programserie;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d("onCreateView " + this);

    View rod = inflater.inflate(R.layout.udsendelser_vandret_skift_frag, container, false);

    kanal = DRData.instans.grunddata.kanalFraKode.get(getArguments().getString(Kanal_frag.P_kode));
    startudsendelse = DRData.instans.udsendelseFraSlug.get(getArguments().getString(DRJson.Slug.name()));
    if (startudsendelse == null) { // Fix for https://www.bugsense.com/dashboard/project/cd78aa05/errors/805598045
      if (!App.PRODUKTION) { // https://www.bugsense.com/dashboard/project/cd78aa05/errors/822628124
        App.langToast("startudsendelse==null");
        App.langToast("startudsendelse==null for " + kanal);
      }
      Log.e(new IllegalStateException("startudsendelse==null"));
      // Fjern backstak og hop ud
      FragmentManager fm = getActivity().getSupportFragmentManager();
      fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
      FragmentTransaction ft = fm.beginTransaction();
      ft.replace(R.id.indhold_frag, new Kanaler_frag());
      ft.addToBackStack(null);
      ft.commit();
      Sidevisning.vist(Kanaler_frag.class);

      return rod;
    }
    programserie = DRData.instans.programserieFraSlug.get(startudsendelse.programserieSlug);
    Log.d("onCreateView " + this + " viser " + " / " + startudsendelse);


    viewPager = (ViewPager) rod.findViewById(R.id.pager);
    //noinspection ResourceType
    viewPager.setId(123); // TODO hvorfor? fjern eller forklar hvorfor R.id.pager ikke er god nok
    pager_title_strip = rod.findViewById(R.id.pager_title_strip);
    // Da ViewPager er indlejret i et fragment skal adapteren virke på den indlejrede (child)
    // fragmentmanageren - ikke på aktivitens (getFragmentManager)
    adapter = new UdsendelserAdapter(getChildFragmentManager());
    DRJson.opdateriDagIMorgenIGårDatoStr(App.serverCurrentTimeMillis());

    liste = new ArrayList<Udsendelse>();
    if (programserie == null) {
      liste.add(startudsendelse);
      adapter.liste2 = liste;
      viewPager.setAdapter(adapter);
      hentUdsendelser(0);
    } else {
      liste.addAll(programserie.getUdsendelser());  // EO ŝanĝo
      int n = Programserie.findUdsendelseIndexFraSlug(liste, startudsendelse.slug);
      if (n < 0) {
        liste.add(startudsendelse);
        adapter.liste2 = liste;
        viewPager.setAdapter(adapter);
        hentUdsendelser(0);
      } else {
        // liste.addAll(programserie.getUdsendelser());  // EO ŝanĝo
        adapter.liste2 = liste;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(n);
      }
    }

/*    int n = programserie == null ? -1 : programserie.findUdsendelseIndexFraSlug(startudsendelse.slug);

    Log.d("programserie.udsendelser.indexOf(startudsendelse) = " + n);
    if (n >= 0) {
      liste.addAll(programserie.getUdsendelser());
      viewPager.setAdapter(adapter);
      viewPager.setCurrentItem(n);
    } else {
      liste.add(startudsendelse);
      viewPager.setAdapter(adapter);
      if (programserie == null) hentUdsendelser(0);
    }
    */
    vispager_title_strip();
    viewPager.setOnPageChangeListener(this);
    // Nødvendigt fordi underfragmenter har optionsmenu
    // - ellers nulstilles optionsmenuen ikke når man hopper ud igen!
    setHasOptionsMenu(true);
    return rod;
  }

  private void vispager_title_strip() {
    pager_title_strip.setVisibility(
        !App.prefs.getBoolean("vispager_title_strip", false) ? View.GONE :
            liste.size() > 1 ? View.VISIBLE : View.INVISIBLE);
  }


  private void opdaterListe() {
    if (viewPager == null) return;
    Udsendelse udsFør = liste.size()>viewPager.getCurrentItem() ? liste.get(viewPager.getCurrentItem()) : null;
    liste = new ArrayList<Udsendelse>();
    liste.addAll(programserie.getUdsendelser());
    if (Programserie.findUdsendelseIndexFraSlug(liste, startudsendelse.slug) < 0) {
      liste.add(startudsendelse);
      // hvis startudsendelse ikke er med i listen, så hent nogle flere, i håb om at komme hen til
      // startudsendelsen (hvis vi ikke allerede har forsøgt 7 gange)
      if (antalHentedeSendeplaner++ < 7) {
        hentUdsendelser(programserie.getUdsendelser().size());
      }
    }
    int nEft = udsFør==null?0:Programserie.findUdsendelseIndexFraSlug(liste, udsFør.slug);
    if (nEft < 0) nEft = liste.size() - 1; // startudsendelsen
    adapter.liste2 = liste;
    adapter.notifyDataSetChanged();
    if (App.fejlsøgning) Log.d("xxx setCurrentItem " + viewPager.getCurrentItem() + "   nEft=" + nEft);
    viewPager.setCurrentItem(nEft, false); // - burde ikke være nødvendig, vi har defineret getItemPosition
    vispager_title_strip();
/*
    if (programserie.getUdsendelser().size() < programserie.antalUdsendelser) {
      hentUdsendelser(programserie.getUdsendelser().size());
    }
    // hvis vi er sidst i listen og der er flere at hente, så hent nogle flere,
    // i håb om at komme hen til den aktuelle startudsendelse (hvis vi ikke allerede har forsøgt 7 gange)
    if (nEft==liste.size()-1 && nEft<programserie.antalUdsendelser-1 && antalHentedeSendeplaner++ < 7) {
      // da det element vi viser lige nu
      hentUdsendelser(programserie.getUdsendelser().size()-1);
    }
*/
  }

  @Override
  public void onDestroyView() {
    //if (viewPager!=null) viewPager.setAdapter(null); - forårsager crash... har ikke kigget nærmere på hvorfor
    viewPager = null;
    adapter = null;
    pager_title_strip = null;
    super.onDestroyView();
  }

  private void hentUdsendelser(final int offset) {
    if (!App.ÆGTE_DR) return;
    String url = DRData.getProgramserieUrl(startudsendelse.programserieSlug) + "&offset=" + offset;
    Log.d("XXX url=" + url);

    Request<?> req = new DrVolleyStringRequest(url, new DrVolleyResonseListener() {
      @Override
      public void fikSvar(String json, boolean fraCache, boolean uændret) throws Exception {
        if (uændret) return;
        Log.d("fikSvar(" + fraCache + " " + url);
        if (json != null && !"null".equals(json)) {
          JSONObject data = new JSONObject(json);
          if (offset == 0) {
            programserie = DRJson.parsProgramserie(data, programserie);
            DRData.instans.programserieFraSlug.put(startudsendelse.programserieSlug, programserie);
          }
          JSONArray prg = data.getJSONArray(DRJson.Programs.name());
          ArrayList<Udsendelse> udsendelser = DRJson.parseUdsendelserForProgramserie(prg, kanal, DRData.instans);
          programserie.tilføjUdsendelser(offset, udsendelser);
          //programserie.tilføjUdsendelser(Arrays.asList(startudsendelse));
          opdaterListe();
        }
      }
    }).setTag(this);
    App.volleyRequestQueue.add(req);
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
  }

  @Override
  public void onPageSelected(int position) {
    if (programserie != null && position == liste.size() - 1 && antalHentedeSendeplaner++ < 7) { // Hent flere udsendelser
      hentUdsendelser(programserie.getUdsendelser() == null ? 0 : programserie.getUdsendelser().size());
    }
    Sidevisning.vist(Udsendelse_frag.class, liste.get(position).slug);
  }

  @Override
  public void onPageScrollStateChanged(int state) {
  }

  //  public class UdsendelserAdapter extends FragmentPagerAdapter {
  public class UdsendelserAdapter extends FragmentStatePagerAdapter {

    public ArrayList<Udsendelse> liste2;

    public UdsendelserAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      Udsendelse u = liste2.get(position);
      Fragment f = Fragmentfabrikering.udsendelse(u);
      f.getArguments().putString(Kanal_frag.P_kode, kanal.kode);
      f.getArguments().putString(Udsendelse_frag.AKTUEL_UDSENDELSE_SLUG, getArguments().getString(Udsendelse_frag.AKTUEL_UDSENDELSE_SLUG));
      return f;
    }

    /*
     * Denne metode kaldes af systemet efter et kald til notifyDataSetChanged()
     * Se http://developer.android.com/reference/android/support/v4/view/PagerAdapter.html :
     * Data set changes must occur on the main thread and must end with a call to notifyDataSetChanged()
     * similar to AdapterView adapters derived from BaseAdapter. A data set change may involve pages being
     * added, removed, or changing position. The ViewPager will keep the current page active provided
     * the adapter implements the method getItemPosition(Object).
     * @param object fragmentet
     * @return dets (nye) position
     *
     */
    /*
     @Override
     public int getItemPosition(Object object) {
     if (!(object instanceof Fragment)) {
     Log.rapporterFejl(new Exception("getItemPosition gav ikke et fragment!??!"), ""+object);
     return POSITION_NONE;
     }
     Bundle arg = ((Fragment) object).getArguments();
     String slug = arg.getString(DRJson.Slug.name());
     if (slug==null) {
     Log.rapporterFejl(new Exception("getItemPosition gav fragment uden slug!??!"), ""+arg);
     return POSITION_NONE;
     }
     int nyPos = Programserie.findUdsendelseIndexFraSlug(liste, slug);
     Log.d("xxx getItemPosition "+object+" "+arg +"   - nyPos="+nyPos);
     return nyPos;
     }
     */
    @Override
    public int getCount() {
      return liste2.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      Udsendelse u = liste2.get(position);
      String dato = DRJson.datoformat.format(u.startTid);
      if (dato.equals(DRJson.iDagDatoStr)) dato = getString(R.string.i_dag);
      else if (dato.equals(DRJson.iMorgenDatoStr)) dato = getString(R.string.i_morgen);
      else if (dato.equals(DRJson.iGårDatoStr)) dato = getString(R.string.i_går);
      return dato;
      //return DRJson.datoformat.format(u.startTid);
      //return ""+u.episodeIProgramserie+" "+u.slug;
    }
  }
}

