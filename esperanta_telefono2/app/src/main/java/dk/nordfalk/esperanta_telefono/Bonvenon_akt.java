/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.nordfalk.esperanta_telefono;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


/**
 *
 * @author Jacob Nordfalk
 */
public class Bonvenon_akt extends Activity implements OnClickListener {

  View startknap, trænknap, fortsætknap;
  private SharedPreferences prefs;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.bonvenon);

    findViewById(R.id.filmoj).setOnClickListener(this);
    findViewById(R.id.instali).setOnClickListener(this);
    findViewById(R.id.inviti).setOnClickListener(this);

    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    int id = prefs.getInt(EKRANO, 0);
    if (id>0) {
      Toast.makeText(this, "Premu <== (reen) por reveni al la ĉefa ekrano", Toast.LENGTH_LONG).show();
      klak(id);
    }
  }


  public final String EKRANO = "ekrano";

  public void onClick(View v) {
    int id = v.getId();
    prefs.edit().putInt(EKRANO, id).commit();
    klak(id);
  }

  private void klak(int id) {
    if (id==R.id.filmoj) startActivity(new Intent(this, Filmoj_akt.class));
    else if (id==R.id.instali) startActivity(new Intent(this, Instrukcioj_akt.class));
  }

  public void onRestart() {
    super.onRestart();
    prefs.edit().putInt(EKRANO, 0).commit();
  }
}