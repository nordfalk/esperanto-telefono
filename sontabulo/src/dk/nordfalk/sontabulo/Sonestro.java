package dk.nordfalk.sontabulo;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;



public class Sonestro {
	
	private  SoundPool soundPool;
	private  HashMap<Integer, Integer> sonujo;
	private  AudioManager  audioManager;
	private  Context ctx;
		
	public void init(Context aktiveco) {
  		 ctx = aktiveco;
	     soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
	     sonujo = new HashMap<Integer, Integer>();
	     audioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
	} 
	
	public void regustruSonon(int i,int sono)
	{
    if (sonujo.containsKey(i)) {
      Log.d("Sontabulo", "jam ekzistas "+sonujo.get(i)+"=?"+sono+" al indekso "+i);
      return;
    }
    Log.d("Sontabulo", "ŝarĝas "+sono+" al indekso "+i);
		sonujo.put(i, soundPool.load(ctx, sono, 1));
	}
	
	public void ludu(int index) {
	     int lauxteco = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
       Log.d("Sontabulo","Ludas kun lauxteco "+lauxteco + " maks estus "+audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
	     soundPool.play(sonujo.get(index), lauxteco, lauxteco, 1, 0, 1f);
	}
	
}