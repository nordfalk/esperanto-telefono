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

import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import com.google.ads.Ad;
import com.google.ads.AdRequest.ErrorCode;
import android.content.SharedPreferences;
import eo.radio.datumoj.Kanalo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.android.deskclock.AlarmClock_akt;
import dk.dr.radio.afspilning.AfspillerListener;
import dk.dr.radio.afspilning.Ludado;
import dk.dr.radio.diverse.MitGalleri;
import dk.dr.radio.util.Kontakt;
import eo.radio.datumoj.Log;
import dk.dr.radio.util.Network;
import eo.radio.datumoj.Elsendo;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class Ludado_akt extends Activity implements AfspillerListener {
  private Datumoj datumoj;
  private ImageButton startStopButono;
  private ImageView aliaj_elsendoj_antaŭa;
  private ImageView aliaj_elsendoj_venonta;
  private Ludado ludado;
  private TextView statuso;
  private SharedPreferences prefs;
  private MitGalleri aliaj_elsendoj_Gallery;
  private View aliaj_elsendoj_FrameLayout;
  private Button hejmpaĝoButono;
  private WebView hejmpaĝoEkrane;
  private TextView ludasNunTextView;
  private ScrollView ludasNun_ScrollView;
  private LinearLayout.LayoutParams ludasNun_ScrollView_LayoutParams;
  private float ludasNun_ScrollView_LayoutParams_weight_orig;
  private boolean montriReklamojn;
  private static boolean montriReklamojnNePluĈarJamKlakis = false;
  private AdView adView;
  private AdRequest adRequest;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    //requestWindowFeature(Window.FEATURE_ACTION_BAR);
    super.onCreate(savedInstanceState);

    prefs = PreferenceManager.getDefaultSharedPreferences(this);



    // Fuld skærm skjuler den notification vi sætter op så brugeren ikke opdager den,
    // og det er lidt forvirrende så vi slår det fra for nu
    //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN) ;
    setContentView(R.layout.ludado_akt);
    pretiguKontrolojn();

    try {
      datumoj = Datumoj.instanco;
      datumoj.kontroluFonaFadenoStartis();
      ludado = datumoj.ludado;
    } catch (Exception ex) {
      // TODO popop-advarsel til bruger om intern fejl og rapporter til udvikler-driftmeddDialog
      App.videblaEraro(this, ex);
      return;
    }

    registerReceiver(ĉefdatumojĜisdatigitajReciever, new IntentFilter(Datumoj.INTENT_novaj_ĉefdatumoj));
    registerReceiver(elsendojĜisdatigitajReciever, new IntentFilter(Datumoj.INTENT_novaj_elsendoj));

    try {
      montruAktualanKanalon();
      montriAktualanElsendon();
    } catch (Exception e) {
      App.eraro(e);
    }

    // Volumen op/ned skal styre lydstyrken af medieafspilleren, uanset som noget spilles lige nu eller ej
    setVolumeControlStream(AudioManager.STREAM_MUSIC);


    // Vis korrekt knap og/eller start afspilning
    if (savedInstanceState != null) {
      aliaj_elsendoj_FrameLayout.setVisibility(savedInstanceState.getInt("aliaj_elsendoj_videblaj"));
    } else {
      try {
        boolean tujLudi = prefs.getBoolean("tujLudi", true);
        if (tujLudi && ludado.getAfspillerstatus() == Ludado.STATUSO_HALTIS) {
          startiLudadon();
        }
      } catch (Exception e) {
        App.eraro(e);
      }
    }
    montriStartStopButonon();

    ludado.addAfspillerListener(Ludado_akt.this);


    boolean novaInstalo = (prefs.getInt(mesagxo_hash_ŜLOSILO, 0) == 0);
    AppRater.app_launched(this, novaInstalo, savedInstanceState != null);

    montriReklamojn = prefs.getBoolean(MontriReklamojn.ŜLOSILO_montri_reklamojn, false);
    if (montriReklamojnNePluĈarJamKlakis) {
      montriReklamojn = false;
    } else if (!montriReklamojn) {
      MontriReklamojn.app_launched(this, novaInstalo, savedInstanceState != null);
    } else {
      final FrameLayout fono = (FrameLayout) findViewById(R.id.fono);
      adView = new AdView(this, AdSize.BANNER, "a14f874e92f349d");
      fono.addView(adView);
      ((FrameLayout.LayoutParams) adView.getLayoutParams()).gravity = Gravity.RIGHT;
      adRequest = new AdRequest();
      String[] kw = { "Esperanto", "Radio", "Music", "Language", "Android", "app", "Learn", "International" };
      adRequest.setKeywords(new HashSet<String>(Arrays.asList(kw)));
      //adRequest.addTestDevice("9D6456C89382B25FD6B3142C11C1E7A7"); // Samsung Galaxy SII de Jacob
      adView.setAdListener(new AdListener() {
        public void onReceiveAd(Ad ad) {
          adView.setVisibility(View.VISIBLE);
        }

        public void onFailedToReceiveAd(Ad ad, ErrorCode ec) {
          fono.removeView(adView);
          montriReklamojn = false;
        }

        public void onPresentScreen(Ad ad) {
          fono.removeView(adView);
          montriReklamojn = false;
          montriReklamojnNePluĈarJamKlakis = true;
          Toast.makeText(Ludado_akt.this, "Dankon pro via subteno :-)", Toast.LENGTH_LONG).show();
          if (App.uziAnalytics()) {
            App.tracker.trackPageView("reklamoKlako");
          }
        }

        public void onDismissScreen(Ad ad) {
        }

        public void onLeaveApplication(Ad ad) {
        }
      });
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("aliaj_elsendoj_videblaj", aliaj_elsendoj_FrameLayout.getVisibility());
  }
  Runnable montriReklamon = new Runnable() {
    public void run() {
      adView.setVisibility(View.INVISIBLE);
      if (montriReklamojn) adView.loadAd(adRequest);
      Datumoj.instanco.handler.postDelayed(montriReklamon, 120000);
    }
  };

  // For Android 1.6-kompetibilitet bruger vi ikke onBackPressed()
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode != KeyEvent.KEYCODE_BACK) {
      return super.onKeyDown(keyCode, event);
    }

    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    int volumen = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    //boolean lukAfspillerServiceVedAfslutning = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("lukAfspillerServiceVedAfslutning", false);


    if (volumen == 0 && ludado.ludadstatuso != Ludado.STATUSO_HALTIS) {
      // Hvis der er skruet helt ned så stop afspilningen
      ludado.stopAfspilning();
      finish();
    } else if (ludado.ludadstatuso == Ludado.STATUSO_HALTIS) {
      finish();
    } else {
      // Demandu al la uzanto kion fari
      showDialog(1);
    }

    return true;
  }

  @Override
  protected void onDestroy() {
    ludado.addAfspillerListener(Ludado_akt.this);
    unregisterReceiver(ĉefdatumojĜisdatigitajReciever);
    unregisterReceiver(elsendojĜisdatigitajReciever);
    if (adView != null) adView.destroy();
    super.onDestroy();
  }
  AlertDialog internetforbindelseManglerDialog;

  @Override
  protected void onResume() {
    // se om vi er online
    boolean connectionOK = Network.testConnection(getApplicationContext());
    if (connectionOK) {
      // hurra - opdater data fra server
      datumoj.setBaggrundsopdateringAktiv(true);
      if (adView != null) {
        adView.setVisibility(View.INVISIBLE);
        Datumoj.instanco.handler.postDelayed(montriReklamon, 10000);
      }
    } else {
      // Informer brugeren hvis vi er offline
      if (internetforbindelseManglerDialog == null) {
        internetforbindelseManglerDialog = new AlertDialog.Builder(this).create();
        internetforbindelseManglerDialog.setTitle("Retkonekto mankas");
        internetforbindelseManglerDialog.setMessage("Via telefono ne havas konekton al la reto.\nPor aŭskulti radion vi devas enŝalti sendratan reton (WiFI aŭ mobildatumoj).");
      }
      if (!internetforbindelseManglerDialog.isShowing()) {
        internetforbindelseManglerDialog.show();
      }
    }
    super.onResume();
  }

  @Override
  protected void onPause() {
    datumoj.setBaggrundsopdateringAktiv(false);
    Datumoj.instanco.handler.removeCallbacks(montriReklamon);
    super.onPause();
  }

  private void pretiguKontrolojn() {

    statuso = (TextView) findViewById(R.id.status);

    Button selectChannelButton = (Button) findViewById(R.id.player_select_channel_button);
    selectChannelButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivityForResult(new Intent(Ludado_akt.this, ElektiKanalon_akt.class), 117);
      }
    });

    startStopButono = (ImageButton) findViewById(R.id.start_stop_knap);
    startStopButono.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (ludado.getAfspillerstatus() == Ludado.STATUSO_HALTIS) {
          startiLudadonAŭTuneIn();
        } else {
          haltiLudadon();
        }
      }
    });

    ludasNunTextView = (TextView) findViewById(R.id.ludasNun);
    ludasNun_ScrollView = (ScrollView) findViewById(R.id.ludasNun_ScrollView);
    ludasNun_ScrollView_LayoutParams = (LinearLayout.LayoutParams) ludasNun_ScrollView.getLayoutParams();
    ludasNun_ScrollView_LayoutParams_weight_orig = ludasNun_ScrollView_LayoutParams.weight;

    Button playerAboutButton = (Button) findViewById(R.id.player_about_button);
    playerAboutButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(Ludado_akt.this, Pri_akt.class));
      }
    });

    final Animation galerioMalaperu = AnimationUtils.loadAnimation(Ludado_akt.this, android.R.anim.fade_out);
    //galerioMalaperu.setDuration(10000);
    galerioMalaperu.setAnimationListener(new AnimationListener() {
      public void onAnimationStart(Animation arg0) {
      }

      public void onAnimationEnd(Animation arg0) {
        aliaj_elsendoj_FrameLayout.setVisibility(View.GONE);
      }

      public void onAnimationRepeat(Animation arg0) {
      }
    });

    aliaj_elsendoj_FrameLayout = findViewById(R.id.aliaj_elsendoj_FrameLayout);
    aliaj_elsendoj_Gallery = (MitGalleri) findViewById(R.id.aliaj_elsendoj_Gallery);
    aliaj_elsendoj_Gallery.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Elsendo elsendo = (Elsendo) aliaj_elsendoj_Gallery.getAdapter().getItem(position);

        datumoj.ludado.setKanalon(elsendo.kanalNomo, elsendo.sonoUrl);
        datumoj.aktualaElsendo = elsendo;
        montriAktualanElsendon();
        startiLudadonAŭTuneIn();
        if (elsendo.elektoIgasLaGalerioMalaperi) {
          aliaj_elsendoj_FrameLayout.startAnimation(galerioMalaperu);
        }
      }
    });
    //aliaj_elsendoj_Gallery.setSpacing(25); // 25 punkter

    aliaj_elsendoj_antaŭa = (ImageView) findViewById(R.id.aliaj_elsendoj_antaŭa);
    aliaj_elsendoj_antaŭa.setAlpha(200);
    aliaj_elsendoj_antaŭa.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          aliaj_elsendoj_Gallery.onFling(null, null, -1200, 0);
        }
        return true;
      }
    });

    aliaj_elsendoj_venonta = (ImageView) findViewById(R.id.aliaj_elsendoj_venonta);
    aliaj_elsendoj_venonta.setAlpha(200);
    //nextImageView.setVisibility(ImageButton.GONE);
    aliaj_elsendoj_venonta.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          aliaj_elsendoj_Gallery.onFling(null, null, 1200, 0);
        };
        return true;
      }
    });
    aliaj_elsendoj_Gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        aliaj_elsendoj_venonta.setVisibility(position == 0 ? ImageButton.GONE : ImageButton.VISIBLE);
        aliaj_elsendoj_antaŭa.setVisibility(position == parent.getCount() - 1 ? ImageButton.GONE : ImageButton.VISIBLE);
      }

      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    hejmpaĝoEkrane = (WebView) findViewById(R.id.hejmpaĝo);
    if (hejmpaĝoEkrane != null) {
      WebSettings s = hejmpaĝoEkrane.getSettings();
      s.setSupportZoom(true);
      s.setBuiltInZoomControls(true);
      //s.setUseWideViewPort(true);

      hejmpaĝoEkrane.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
      //hejmpaĝo.setInitialScale(75); // 75%
    }


    hejmpaĝoButono = (Button) findViewById(R.id.al_la_hejmpaĝo);
    hejmpaĝoButono.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(datumoj.aktualaKanalo.hejmpaĝoButono)));
      }
    });
  }

  /** Denne metode kaldes når der er valgt en kanal */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //Log.d("onActivityResult " + requestCode + " " + resultCode + " " + data);
    // requestCode er 117
    if (resultCode != RESULT_OK) {
      return;  // Hvis brugeren trykkede på tilbage-knappen eller valgte den samme kanal
    }
    // Afspilning skal starte så når brugeren har valgt kanal
    // sætKanal();
    startiLudadonAŭTuneIn();
    montruAktualanKanalon();

    // Kald for at nulstille skærmen
    try {
      montriAktualanElsendon();
      // Devus havi, sed ŝajnas nenecesa: invalidateOptionsMenu();
    } catch (Exception e) {
      App.videblaEraro(this, e);
    }
  }

  private void startiLudadonAŭTuneIn() {
    if (datumoj.aktualaElsendo.rektaElsendaPriskriboUrl != null && prefs.getBoolean("uzu_tunein", false)) {
      if (ludado.ludadstatuso != Ludado.STATUSO_HALTIS) {
        haltiLudadon();
      }
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("tunein.player://Tune/S160732"));
      if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
        Toast.makeText(Ludado_akt.this, "Startis TuneIn por ludi Muzaikon", Toast.LENGTH_LONG).show();
        startActivity(i);
      } else {
        Toast.makeText(Ludado_akt.this, "Instalu TuneIn kaj poste revenu al la programo kaj klaku denove", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=tunein.player")));
      }
      return;
    }

    startiLudadon();
  }

  private void startiLudadon() {
    //Log.d("startAfspilning");
    try {
      String url = datumoj.aktualaElsendo.sonoUrl;

      ludado.setKanalon(datumoj.aktualaKanalo.nomo, url);
      //startStopButton.setImageResource(R.drawable.buffer_white);
      //visAktuelKanal();
      ludado.startiLudadon();
      montriStartStopButonon();
    } catch (Exception e) {
      if (Datumoj.evoluiganto) App.videblaEraro(this, e);
      else App.eraro(e);
    }

  }

  private void haltiLudadon() {
    ludado.stopAfspilning();
  }

  private void montriStatuson(String txt) {
    statuso.setText(txt);
    Log.d(txt);
  }

  private void montriStartStopButonon() {
    if (ludado == null) {
      return;
    }
    if (ludado.getAfspillerstatus() == Ludado.STATUSO_HALTIS) {
      startStopButono.setImageResource(R.drawable.play);
    } else {
      startStopButono.setImageResource(R.drawable.stop);
    }
  }

  private void montruKonektasProcentojn(int procent) {
    //Log.d( "sætforbinderProcent( " + procent + " )" );

    int afspillerstatus = ludado.getAfspillerstatus();

    if (procent <= 0) {
      if (afspillerstatus == Ludado.STATUSO_KONEKTAS) {
        montriStatuson("Konektas...");
      } else {
        montriStatuson("");
      }
    } else {
      montriStatuson("Konektas... " + procent + "%");
    }
  }

  public void onAfspilningStartet() {
    //Log.d( "onAfspilningStartet()" ) ;
    //startStopButton.setImageResource(R.drawable.pause_white);
    montriStatuson("Ludas");
    montriStartStopButonon();
  }

  public void onAfspilningStoppet() {
    //Log.d( "onAfspilningStoppet()" ) ;
    //startStopButton.setImageResource(R.drawable.play_white);
    montriStatuson("Haltis");
    montriStartStopButonon();
  }

  public void onAfspilningForbinder(int procent) {
    if (procent >= 100) {
      onAfspilningStartet();
    } else {
      //startStopButton.setImageResource(R.drawable.buffer_white);
      montruKonektasProcentojn(procent);
    }
  }
  final static String mesagxo_hash_ŜLOSILO = "drift_statusmeddelelse";
  static int mesagxo_hash_valoro = 0;
  static String mesagxo = ""; // static necesas por certigi ke la valoro pluiros al venonta aktiveco se oni turnas la ekranon
  private BroadcastReceiver ĉefdatumojĜisdatigitajReciever = new BroadcastReceiver() {
    @Override
    public void onReceive(Context ctx, Intent i) {
      //Log.d("stamdataOpdateretReciever");

      mesagxo = datumoj.stamdata.json.optString(mesagxo_hash_ŜLOSILO + App.appInfo.versionCode);
      // Se neniu mesaĝo por tiu ĉi versio ni montru ĝeneralan version
      if (mesagxo.length() == 0) mesagxo = datumoj.stamdata.json.optString(mesagxo_hash_ŜLOSILO);

      // Ni ignoru spacojn
      mesagxo = mesagxo.trim();

      // Tjek i prefs om denne drifmeddelelse allerede er vist.
      // Der er 1 ud af en millards chance for at hashkoden ikke er ændret, den risiko tør vi godt løbe
      mesagxo_hash_valoro = mesagxo.hashCode();
      int gammelHashkode = prefs.getInt(mesagxo_hash_ŜLOSILO, 0);
      //Log.d("drift_statusmeddelelse='" + mesagxo + "' nyHashkode=" + mesagxo_hash_valoro + " gammelHashkode=" + gammelHashkode);
      if (gammelHashkode != mesagxo_hash_valoro && !"".equals(mesagxo)) { // Driftmeddelelsen er ændret. Vis den...
        showDialog(0);
      }
    }
  };

  @Override
  protected Dialog onCreateDialog(final int id) {
    AlertDialog.Builder ab = new AlertDialog.Builder(this);
    if (id == 0) {
      ab.setPositiveButton("Bone", new AlertDialog.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          prefs.edit().putInt(mesagxo_hash_ŜLOSILO, mesagxo_hash_valoro).commit(); // ...og gem ny hashkode i prefs
        }
      });
      ab.setMessage(mesagxo);
    } else {
      ab.setMessage("Ĉu ĉesigi la ludadon?");
      ab.setPositiveButton("Haltigi\nmuzikon", new AlertDialog.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          haltiLudadon();
          finish();
        }
      });
      ab.setNeutralButton("Ludi\nfone", new AlertDialog.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
          finish();
        }
      });
      ab.setNegativeButton("Nuligi", null);
    }
    return ab.create();
  }

  @Override
  public void onPrepareDialog(int id, Dialog d) {
    if (id == 0) ((AlertDialog) d).setMessage(mesagxo);
  }
  private BroadcastReceiver elsendojĜisdatigitajReciever = new BroadcastReceiver() {
    @Override
    public void onReceive(Context ctx, Intent i) {
      //Log.d("udsendelserOpdateretReciever");
      try {
        montriAktualanElsendon();
      } catch (Exception e) {
        App.eraro(e);
      }
    }
  };

  private void montruAktualanKanalon() {
    final Kanalo kanalo = datumoj.aktualaKanalo;

    ImageView aktuelKanalImageView = (ImageView) findViewById(R.id.player_select_channel_billede);
    TextView aktuelKanalTextView = (TextView) findViewById(R.id.player_select_channel_text);

    if (kanalo.emblemo == null) { //
      aktuelKanalTextView.setText(kanalo.nomo);
      aktuelKanalTextView.setVisibility(View.VISIBLE);
      aktuelKanalImageView.setVisibility(View.GONE);
    } else if (kanalo.emblemo.getWidth() < kanalo.emblemo.getHeight() * 2) {
      // Emblemo kun teksto
      aktuelKanalTextView.setVisibility(View.VISIBLE);
      aktuelKanalTextView.setText(kanalo.nomo);
      aktuelKanalImageView.setVisibility(View.VISIBLE);
      aktuelKanalImageView.setImageBitmap(kanalo.emblemo);
    } else {
      // Emblemo kiu enhavas la tekston - do ne montru gxin
      aktuelKanalTextView.setVisibility(View.GONE);
      aktuelKanalImageView.setImageBitmap(kanalo.emblemo);
      aktuelKanalImageView.setVisibility(View.VISIBLE);
      // La bildo plenigu la tutan largxon
      //aktuelKanalImageView.getLayoutParams().width = LayoutParams.FILL_PARENT;
    }


    if (kanalo.hejmpaĝoButono == null) {
      hejmpaĝoButono.setVisibility(View.GONE);
    } else {
      hejmpaĝoButono.setVisibility(View.VISIBLE);
      hejmpaĝoButono.setText("Hejmpaĝo de " + kanalo.nomo);
    }

    if (hejmpaĝoEkrane != null) {
      if (kanalo.hejmpaĝoEkrane == null) {
        hejmpaĝoEkrane.setVisibility(View.GONE);
        ludasNun_ScrollView_LayoutParams.weight = 1;
        ludasNun_ScrollView.invalidate();

      } else {
        hejmpaĝoEkrane.setVisibility(View.VISIBLE);
        ludasNun_ScrollView_LayoutParams.weight = ludasNun_ScrollView_LayoutParams_weight_orig;
        ludasNun_ScrollView.invalidate();

        // Resxargxu la novan retpagxon.
        //hejmpaĝo.loadUrl("about:blank");

        hejmpaĝoEkrane.setWebChromeClient(new WebChromeClient() {
          @Override
          public void onProgressChanged(WebView view, int newProgress) {
            //Log.d("hejmpaĝo newProgress="+newProgress);
            //if (newProgress != 100) return;
						/*
             //hejmpaĝo.flingScroll(50,250);
             hejmpaĝo.findAll("Aktuala");
             hejmpaĝo.findNext(true);
             Log.d("hejmpaĝo.findAll();");
             */
            hejmpaĝoEkrane.setWebChromeClient(null);
            hejmpaĝoEkrane.loadUrl(kanalo.hejmpaĝoEkrane);
          }
        });
        hejmpaĝoEkrane.loadData("&nbsp;", "text/html", "utf-8");
      }
    }

    final ArrayList<Elsendo> elsendoj = kanalo.elsendoj;
    if (elsendoj.size() == 1) {
      aliaj_elsendoj_FrameLayout.setVisibility(View.GONE);
    } else {
      aliaj_elsendoj_FrameLayout.setVisibility(View.VISIBLE);
      aliaj_elsendoj_Gallery.setAdapter(new ArrayAdapter(this, R.layout.ludado_elsendoelemento, R.id.listeelem_overskrift, elsendoj) {
        @Override
        public View getView(int position, View cachedView, ViewGroup parent) {
          View view = super.getView(position, cachedView, parent);
          Elsendo elsendo = elsendoj.get(position);
          TextView listeelem_overskrift = (TextView) view.findViewById(R.id.listeelem_overskrift);
          listeelem_overskrift.setText(elsendo.datoStr);
          TextView listeelem_beskrivelse = (TextView) view.findViewById(R.id.listeelem_beskrivelse);
          String els = elsendo.priskribo;
          if (elsendo.rektaElsendaPriskriboUrl != null && prefs.getBoolean("uzu_tunein", false)) {
            els = "Tuŝu por starti TuneIn por rekta ludado!";
          }
          //els = els.replaceAll("<[^>]*></[^>]*>", "").replaceAll("<p>", "").replaceAll("<a[^>]*>", "");
          els = els.replaceAll("<[^>]*>", "");
          if (elsendo.titolo != null && !elsendo.titolo.equals(elsendo.priskribo)) {
            els = elsendo.titolo + "<br>" + els.trim();
          }
          //Log.d(els);
          //if (els.length()>100) els = els.substring(0,100)+"...";
          listeelem_beskrivelse.setText(Html.fromHtml(els));
          // Ne - malhelpas elektadon:
          //Linkify.addLinks(listeelem_beskrivelse, Linkify.WEB_URLS);
          return view;
        }
      });
      aliaj_elsendoj_Gallery.setSelection(elsendoj.size() - 1);
    }
  }
  DateFormat utcTempoformato = new SimpleDateFormat("HH:mm", Locale.US);

  {
    utcTempoformato.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private void montriAktualanElsendon() {
    Kanalo kanal = datumoj.aktualaKanalo;
    Elsendo elsendo = datumoj.aktualaElsendo;

    ludasNun_ScrollView.setVisibility(View.VISIBLE);
    //Log.d("datumoj.ludasNun= "+datumoj.rektaElsendaPriskribo+" elsendo="+elsendo);

    if (elsendo.rektaElsendaPriskriboUrl != null) { // Rekta elsendo de Muzaiko
      if (datumoj.rektaElsendaPriskribo == null) {
        ludasNunTextView.setText("Atendas informojn...");
      } else {
        ludasNunTextView.setText(Html.fromHtml("Aktuale ludata ĉe " + kanal.nomo
            + " (" + utcTempoformato.format(new Date()) + " UTC)\n<br>\n<br>" + datumoj.rektaElsendaPriskribo.replaceAll("</br>", "<br/>")));
        ludasNunTextView.setMovementMethod(LinkMovementMethod.getInstance());
      }
    } else if (elsendo.datoStr != null) {
      if (kanal.json.optBoolean("uziWebViewPorElsendo") && hejmpaĝoEkrane != null) {
        ludasNun_ScrollView.setVisibility(View.GONE);
        try {
          // http://code.google.com/p/android/issues/detail?id=3552
					/*
           hejmpaĝoEkrane.loadUrl("data:text/html;utf-8," +
           "<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><body>"+
           URLEncoder.encode(
           "Elsendo de "+elsendo.datoStr+" de " + kanal.nomo+"</b>\n"+
           elsendo.priskribo, "utf-8").replaceAll("\\+"," "));
           hejmpaĝoEkrane.loadDataWithBaseURL(mesagxo, mesagxo, mesagxo, mesagxo, mesagxo);
           */
          hejmpaĝoEkrane.loadDataWithBaseURL("fake://not/needed",
              "Elsendo de " + elsendo.datoStr + " de " + kanal.nomo + "<br>\n"
              + elsendo.priskribo, "text/html", "utf-8", "");
        } catch (Exception ex) {
          App.videblaEraro(this, ex);
        }
        hejmpaĝoEkrane.setVisibility(View.VISIBLE);
      } else {
        ludasNunTextView.setText(Html.fromHtml("Elsendo de " + elsendo.datoStr + " de " + kanal.nomo
            + "\n<br>" + elsendo.priskribo));
        Linkify.addLinks(ludasNunTextView, Linkify.WEB_URLS);
      }
    } else {
      // Neniuj informoj venos
      ludasNun_ScrollView.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //Log.d("onCreateOptionsMenu!!!");
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.ludado_menuo, menu);
    if (Datumoj.evoluiganto) menu.add(0, 0, 0, "Kraŝi");
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    //Log.d("onPrepareOptionsMenu!!!");
    MenuItem i = menu.findItem(R.id.kontakti_kanalon); // menu.findItem(105);
    if (datumoj.aktualaKanalo.retpoŝto != null) {
      i.setVisible(true);
      i.setTitle("Skribi al " + datumoj.aktualaKanalo.nomo);
    } else {
      i.setVisible(false);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (App.uziAnalytics()) {
      App.tracker.trackPageView("menuo:" + item.getTitle());
    }


    switch (item.getItemId()) {
      case 0:
        throw new Error("kraŝo :-)");
      case R.id.pri:
        startActivity(new Intent(this, Pri_akt.class));
        break;
      case R.id.agordoj:
        startActivity(new Intent(this, Agordoj_akt.class));
        break;
      case R.id.elŝalti:
        if (ludado.getAfspillerstatus() != Ludado.STATUSO_HALTIS) {
          haltiLudadon();
        }
        finish();
        break;
      case R.id.kundividi:
        datumoj.kundividi(this);
        //TODO: XXX Informoj pri la podkasto ktp!!!
        break;
      case R.id.vekhorloĝo:
        startActivity(new Intent(this, AlarmClock_akt.class));
        break;
      case R.id.kontakti_kanalon:
        String brødtekst =
            "Saluton!\n"
            + (datumoj.aktualaElsendo.titolo != null ? "Mi aŭskultis vian elsendon '" + datumoj.aktualaElsendo.titolo + "'." : "")
            + "\n\nPS. Se vi havas Androjdan telefonon elprovu la Esperanto-radion:\n"
            + "https://market.android.com/details?id=dk.nordfalk.esperanto.radio\n";
        Kontakt.kontakt(this, new String[] { datumoj.aktualaKanalo.retpoŝto }, "Pri " + datumoj.aktualaKanalo.nomo, brødtekst, null);
        break;
    }
    return true;
  }
}