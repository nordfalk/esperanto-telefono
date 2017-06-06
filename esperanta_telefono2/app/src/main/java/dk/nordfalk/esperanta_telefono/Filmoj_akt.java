/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.esperanta_telefono;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * Hjælpeklasse til at holde info om et klip
 * @author Jacob Nordfalk
 */
class Klip {
  HashMap<String, String> egenskaber = new HashMap<String, String>();
  Bitmap thumb;

  public Klip() {
  }

  public String toString() {
    return egenskaber.toString();
  }
}

/**
 *
 * @author j
 */
public class Filmoj_akt extends Activity implements OnItemClickListener {

  /** Listen over videoklip - en klassevariabel der kun indlæses én gang */
  ArrayList<Klip> videoklip = new ArrayList<Klip>();
  ParseKlipAsyncTask klipAsyncTask = new ParseKlipAsyncTask();
  ListView listView;
  KlipAdapter klipadapter = new KlipAdapter();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Burde bruge SD-kortet
    Kasxejo.init(getCacheDir().getPath());
    if (videoklip.isEmpty()) klipAsyncTask.execute();
    //if (videoklip.isEmpty()) klipAsyncTask.doInBackground();

    listView = new ListView(this);
    listView.setAdapter(klipadapter);
    listView.setOnItemClickListener(this);
    //listView.setId(117); // sæt ID så tilstand blir gemt ved skærmvending
    setContentView(listView);
  }



  public class ParseKlipAsyncTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object... arg0) {
      //InputStream is = new URL("http://gdata.youtube.com/feeds/api/users/Esperantoestas/uploads").openStream();
      //InputStream is = new FileInputStream(Kasxejo.hentFil("http://gdata.youtube.com/feeds/api/users/Esperantoestas/uploads", false));

      String adreso = "http://code.google.com/p/esperanto-telefono/wiki/EsperantoFilmetoj";
      InputStream is;
      File fil = new File(Kasxejo.findLokaltFilnavn(adreso));

      try {
        if (fil.exists()) {
          is = new FileInputStream(fil);
        } else {
          fil = null;
          is = getResources().openRawResource(R.raw.esperanto_filmetoj);
        }
        ArrayList<Klip> klip = parseTekst(is);
        is.close();
        videoklip.addAll(klip);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      publishProgress();
      for (Klip k : videoklip) {
        try {
          System.out.println(k);
          k.thumb = BitmapFactory.decodeFile(Kasxejo.hentFil(k.egenskaber.get("bildo"), true));
          publishProgress();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      //File fil = new File(Kasxejo.hentFil(adreso, false);

      return "ok";
    }

    @Override
    protected void onProgressUpdate(Object... values) {
      //System.out.println("onProgressUpdate()");
      klipadapter.notifyDataSetChanged();
    }
  }

  private static ArrayList<Klip> parseTekst(InputStream is) throws Exception {
    String dat = Kasxejo.læsInputStreamSomStreng(is);

    String pre = dat.split("<pre.*?>")[1];
    pre = pre.substring(0,pre.indexOf("</pre>"));

    HashSet egenskaber = new HashSet(Arrays.asList("published", "updated", "content"));
    ArrayList<Klip> liste = new ArrayList<Klip>();
    Klip k = new Klip();
    String kategorio = "senkategoria";
    for (String s : pre.split("\n\r?\n")) {
      s = s.trim();
      if (s.length() == 0) continue;
      System.out.println("s="+s);

      if (s.startsWith("--")) { // Kategorio
        kategorio = s.substring(2, s.length()-2).trim();
      } else {
        k = new Klip();
        liste.add(k);
        for (String l : s.split("\n")) {
          l = l.trim();
          int n = l.indexOf(":");
          if (n==-1) continue;
          k.egenskaber.put(l.substring(0,n).trim(), l.substring(n+1).trim());
        }
        System.out.println(s + " ---> "+k);
      }
    }
    return liste;
  }



  public class KlipAdapter extends BaseAdapter {
    public int getCount() { return videoklip.size(); }

    public Object getItem(int position) { return position; } // bruges ikke
    public long getItemId(int position) { return position; } // bruges ikke

    public View getView(int position, View view, ViewGroup parent) {
      if (view==null) view = getLayoutInflater().inflate(R.layout.listeelement, null);
      TextView listeelem_overskrift = (TextView) view.findViewById(R.id.listeelem_overskrift);
      TextView listeelem_beskrivelse = (TextView) view.findViewById(R.id.listeelem_beskrivelse);
      ImageView listeelem_billede = (ImageView) view.findViewById(R.id.listeelem_billede);

      Klip k = videoklip.get(position);
      listeelem_overskrift.setText( k.egenskaber.get("nomo"));
      listeelem_beskrivelse.setText( k.egenskaber.get("priskribo") );
      listeelem_billede.setImageBitmap(k.thumb);

      return view;
    }
  }

  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    Klip k = videoklip.get(position);
    System.out.println("K="+k);

    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(k.egenskaber.get("filmo")));
    startActivity(intent);
  }
}
