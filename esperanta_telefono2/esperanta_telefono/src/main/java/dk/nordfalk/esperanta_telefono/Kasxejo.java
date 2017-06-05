/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.esperanta_telefono;

import android.util.Log;
import java.io.BufferedInputStream;
import java.util.Date;
import java.net.HttpURLConnection;
import java.io.Closeable;

import java.io.IOException;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Jacob Nordfalk
 */
public class Kasxejo {

  private static final int BUFFERSTR = 4 * 1024;
  private static String lagerDir;
  public static int byteHentetOverNetværk = 0;

  private static void log(String tekst) {
    Log.d("Cache",tekst);
  }

  public static void init(String dir) {
    if (lagerDir != null) {
      return; // vi skifter ikke lager midt i det hele
    }
    lagerDir = dir;
    new File(lagerDir).mkdirs();
    try { // skjul lyd og billeder for MP3-afspillere o.lign.
      new File(dir, ".nomedia").createNewFile();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  static long nu = System.currentTimeMillis();

  public static String hentFil(String url, boolean ændrerSigIkke) throws IOException {
    if (url==null) return null;
    String cacheFilnavn = findLokaltFilnavn(url);
    File cacheFil = new File(cacheFilnavn);

    log("cacheFil lastModified " + new Date(cacheFil.lastModified()) + " for " + url);

    if (cacheFil.exists() && ændrerSigIkke) {
      log("Læser " + cacheFilnavn);
      return cacheFilnavn;
    } else {
      log("Kontakter " + url);

      HttpURLConnection httpForb = (HttpURLConnection) new URL(url).openConnection();

      if (cacheFil.exists()) {
        httpForb.setIfModifiedSince(cacheFil.lastModified());
      }

      httpForb.setConnectTimeout(10000); // 10 sekunder
      try {
        httpForb.connect();
      } catch (IOException e) {
        if (!cacheFil.exists()) {
          throw e; // netværksfejl - og vi har ikke en lokal kopi
        }
        log("Netværksfejl, men der er cachet kopi i " + cacheFilnavn);
        return cacheFilnavn;
      }
      int responseCode = httpForb.getResponseCode();
      if (responseCode == 400 && cacheFil.exists()) {
        httpForb.disconnect();
        log("Netværksfejl, men der er cachet kopi i " + cacheFilnavn);
        return cacheFilnavn;
      }
      if (responseCode == 304) {
        httpForb.disconnect();
        log("Der er cachet kopi i " + cacheFilnavn);
        return cacheFilnavn;
      }
      if (responseCode != 200) {
        throw new IOException(responseCode + " " + httpForb.getResponseMessage() + " for " + url);
      }

      log("Henter " + url + " og gemmer i " + cacheFilnavn);
      boolean ok = false;
      try {
        InputStream is = httpForb.getInputStream();
        FileOutputStream fos = new FileOutputStream(cacheFilnavn);
        kopierOgLuk(is, fos);
        ok = true;
      } finally {
        if (!ok) cacheFil.delete(); // gem ikke halve filer!
      }

      long lastModified = httpForb.getHeaderFieldDate("last-modified", nu);
      log("last-modified" + new Date(lastModified));
      cacheFil.setLastModified(lastModified);

      return cacheFilnavn;
    }
  }

  public static String findLokaltFilnavn(String url) {
    //String cacheFilnavn = url.substring(url.lastIndexOf('/') + 1).replace('?', '_').replace('/', '_').replace('&', '_'); // f.eks.  byvejr_dag1?by=2500&mode=long
    String cacheFilnavn = url.replace('?', '_').replace('/', '_').replace('&', '_'); // f.eks.  byvejr_dag1?by=2500&mode=long
    cacheFilnavn = lagerDir +"/"+ cacheFilnavn;
    log("URL: " + url + "  -> " + cacheFilnavn);
    return cacheFilnavn;
  }

  private static void kopierOgLuk(InputStream in, OutputStream out) throws IOException {
    try {
      byte[] b = new byte[BUFFERSTR];
      int read;
      while ((read = in.read(b)) != -1) {
        out.write(b, 0, read);
        byteHentetOverNetværk += read;
      }
    } finally {
      luk(in);
      luk(out);
    }
  }

  public static void luk(Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static String læsInputStreamSomStreng(InputStream is) throws IOException, UnsupportedEncodingException {

    final char[] buffer = new char[0x3000];
    StringBuilder out = new StringBuilder();
    Reader in = new InputStreamReader(is, "UTF-8");
    int read;
    do {
      read = in.read(buffer, 0, buffer.length);
      if (read>0) {
        out.append(buffer, 0, read);
      }
    } while (read>=0);
    in.close();
    String jsondata = out.toString();
    return jsondata;
  }
}
