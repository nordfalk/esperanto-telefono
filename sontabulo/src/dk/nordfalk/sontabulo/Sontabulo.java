package dk.nordfalk.sontabulo;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;

public class Sontabulo extends Activity {
    private static Sonestro sonestro;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Sontabulo","onCreate() xx komenco: "+System.currentTimeMillis());

        if (sonestro ==null) {
          sonestro = new Sonestro();
          sonestro.init(getApplicationContext());
        }
        
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int nu = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // skru op til 2/3 styrke hvis volumen er lavere end det
        if (nu<2*max/3) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2*max/3, AudioManager.FLAG_SHOW_UI);

        String[] tekstoj = {
"saluton_mejzi",
"saluton_pingveno",
"ho_vidu_jen_ankoraux_unu_pingveno",
"kien_iras_cxiuj_pingvenoj_nun",
"kiel_multe_da_novaj_amikoj_vi_ekhavis_hodiaux",
"gxis_pingvenoj",
"hoj_kiel_bela_vi_estas",
"rigardu_tiuj_du_fajrobrigadanojn",
"atentu_pri_la_eta_nigra_kato",
"pardonu_eta_nigra_kato",
"gxis_revido_eta_nigra_kato",
"ah_cxu_vi_bakas_kukon",
"vi_unue_lavu_la_manojn",
"njam_njam",
"mmm_gxi_aspektas_tre_bone",
"oj_gxi_aspektas_tre_bone",
"cxu_iu_havas_naskigxtagon",
"jes_ja_estas_la_naskigxtago_de_eduardo",
};

        int komenca_id = R.raw.s00_saluton_mejzi;

        TableLayout tl = new TableLayout(this);
        for (int i=0; i<tekstoj.length; i++) {
          final int id = i;
          sonestro.regustruSonon(id, komenca_id+i);

          Button butono = new Button(this);
          String teksto = tekstoj[i].replace('_', ' ');
          teksto = teksto.replaceAll("gx", "ĝ").replaceAll("cx", "ĉ").replaceAll("jx", "ĵ").replaceAll("ux", "ux");
          teksto = teksto.substring(0,1).toUpperCase() + teksto.substring(1);
          butono.setText(teksto);
          butono.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              sonestro.ludu(id);
            }
          });
          tl.addView(butono);
        }

        ScrollView sv = new ScrollView(this);
        sv.addView(tl);
        setContentView(sv);

        Log.d("Sontabulo","onCreate() fino "+System.currentTimeMillis());

    }
}