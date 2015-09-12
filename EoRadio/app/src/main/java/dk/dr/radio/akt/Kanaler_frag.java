package dk.dr.radio.akt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import dk.dr.radio.data.DRData;
import dk.dr.radio.data.Kanal;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.PagerSlidingTabStrip;
import dk.dr.radio.diverse.Sidevisning;
import dk.dr.radio.v3.R;

public class Kanaler_frag extends Basisfragment implements ViewPager.OnPageChangeListener, Runnable {

  private ViewPager viewPager;
  private KanalAdapter adapter;
  private ArrayList<Kanal> kanaler;


  private Venstremenu_frag venstremenuFrag;
  private PagerSlidingTabStrip kanalfaneblade;
  private int viewPagerScrollState;


  @Override
  public void run() {
    kanaler = new ArrayList<Kanal>();
    for (Kanal k : DRData.instans.grunddata.kanaler) {
      if (!k.p4underkanal) kanaler.add(k);
    }
    if (adapter != null) {
      adapter.kanaler2 = kanaler;
      if (viewPager.getCurrentItem() >= kanaler.size()) {
        viewPager.setCurrentItem(0);
      }
      kanalfaneblade.notifyDataSetChanged();
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d("onCreateView " + this);
    View rod = inflater.inflate(R.layout.kanaler_frag, container, false);

    run();
    adapter = new KanalAdapter(getChildFragmentManager());
    adapter.kanaler2 = kanaler;
    viewPager = (ViewPager) rod.findViewById(R.id.pager);
    viewPager.setAdapter(adapter);

    venstremenuFrag = (Venstremenu_frag) getFragmentManager().findFragmentById(R.id.venstremenu_frag);


    if (savedInstanceState == null) {
      int kanalindex = kanaler.indexOf(DRData.instans.afspiller.getLydkilde().getKanal());
      if (kanalindex == -1) kanalindex = 3; // Hvis vi ikke rammer nogen af de overordnede kanaler, så er det P4
      viewPager.setCurrentItem(kanalindex);
      Sidevisning.vist(Kanal_frag.class, kanaler.get(kanalindex).slug);
    }
    kanalfaneblade = (PagerSlidingTabStrip) rod.findViewById(R.id.tabs);
    kanalfaneblade.setTextSize(getResources().getDimensionPixelSize(R.dimen.metainfo_skrifstørrelse));
    kanalfaneblade.setViewPager(viewPager);
    kanalfaneblade.setOnPageChangeListener(this);
    DRData.instans.grunddata.observatører.add(this);

    return rod;
  }

  @Override
  public void onResume() {
    App.hentEvtNyeGrunddata.run();
    venstremenuFrag.visOpnavigering(false);
    super.onResume();
  }

  @Override
  public void onPause() {
    venstremenuFrag.visOpnavigering(true);
    super.onPause();
  }

  @Override
  public void onDestroyView() {
    viewPager.setAdapter(null); // forårsager crash? - men nødvendig for at undgå https://mint.splunk.com/dashboard/project/cd78aa05/errors/2151178610
    viewPager = null;
    adapter = null;
    kanalfaneblade = null;
    DRData.instans.grunddata.observatører.remove(this);
    super.onDestroyView();
  }

  @Override
  public void onPageSelected(int position) {
    Log.d("onPageSelected( " + position);
    // Husk foretrukken getKanal
    App.prefs.edit().putString(App.FORETRUKKEN_KANAL, kanaler.get(position).kode).commit();
    Sidevisning.vist(Kanal_frag.class, kanaler.get(position).slug);
  }

  @Override
  public void onPageScrollStateChanged(int state) {
    Log.d("onPageScrollStateChanged( " + state);
    viewPagerScrollState = state;
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    if (App.fejlsøgning) Log.d("onPageScrolled( " + position + " " + positionOffset + " " + positionOffsetPixels);
    // Hvis vi er på 0'te side og der trækkes mod højre kan viewpageren ikke komme længere og offsetPixels vil være 0,
    if (position == 0 && positionOffsetPixels == 0 && viewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
      venstremenuFrag.visMenu();
    }
  }

  public class KanalAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {
    public ArrayList<Kanal> kanaler2;
    //public class KanalAdapter extends FragmentStatePagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    public KanalAdapter(FragmentManager fm) {
      super(fm);
    }

    //@Override
    //public float getPageWidth(int position) { return(0.9f); }

    @Override
    public Fragment getItem(int position) {
      Kanal k = kanaler2.get(position);
      Fragment f = Fragmentfabrikering.kanal(k);
      return f;
    }

    @Override
    public int getCount() {
      return kanaler2.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return kanaler2.get(position).navn;
    }


    @Override
    public int getPageIconResId(int position) {
      return kanaler2.get(position).kanallogo_resid;
    }

    @Override
    public String getPageContentDescription(int position) {
      return kanaler2.get(position).navn;
    }

    @Override
    public Bitmap getPageIconBitmap(int position) {
      return kanaler2.get(position).eo_emblemo;
    }
  }
}

