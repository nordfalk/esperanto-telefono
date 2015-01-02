package dk.dr.radio.data;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Jacob Nordfalk
 */
public class FilCache {

  private static final int BUFFERSTR = 4 * 1024;
  private static String lagerDir;
  public static int byteHentetOverNetværk = 0;
  private static boolean ŝpurado = true;


  private static void log(String tekst) {
    Log.d(tekst);
  }

  public static void init(File dir) {
    if (lagerDir != null) {
      return; // vi skifter ikke lager midt i det hele
    }
    lagerDir = dir.getPath();
    dir.mkdirs();
    try { // skjul lyd og billeder for MP3-afspillere o.lign.
      new File(dir, ".nomedia").createNewFile();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Henter en fil fra cachen eller fra webserveren
   * @param url
   * @param ændrerSigIkke Hvis true vil cachen aldrig forsøge at kontakte serveren hvis der er en lokal fil.
   * God til f.eks. billeder og andre ting der ikke ændrer sig
   * @return Stien til hvor filen findes lokalt
   * @throws IOException
   */
  public static String akiriDosieron(String url, boolean ændrerSigIkke, boolean nurLoka) throws IOException {
    String cacheFilnavn = lokaDosiero(url);
    File cacheFil = new File(cacheFilnavn);

    if (ŝpurado) log("cacheFil lastModified " + new Date(cacheFil.lastModified()) + " for " + url);
    long nu = System.currentTimeMillis();

    if (cacheFil.exists() && (ændrerSigIkke || nurLoka)) {
      if (ŝpurado) log("Læser " + cacheFilnavn);
      return cacheFilnavn;
    } else {
      if (nurLoka) return null;
      long hentHvisNyereEnd = cacheFil.lastModified();

      int prøvIgen = 3;
      while (prøvIgen > 0) {
        prøvIgen = prøvIgen - 1;
        log("Kontakter " + url);
        HttpURLConnection httpForb = (HttpURLConnection) new URL(url).openConnection();

        if (cacheFil.exists()) {
          httpForb.setIfModifiedSince(hentHvisNyereEnd);
        }

        httpForb.addRequestProperty("Accept-Encoding", "gzip");
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
        int responseCode = 0;
        try { //Try-catch hack due to many exceptions.. this actually helped a lot with erroneous image loadings
          responseCode = httpForb.getResponseCode();
        } catch (IOException e) {
          httpForb.disconnect();
          if(prøvIgen == 0){
            throw e;
          }
          continue;
        }
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
          if (prøvIgen == 0)
            throw new IOException(responseCode + " " + httpForb.getResponseMessage() + " for " + url);
          // Prøv igen
          log("Netværksfejl, vi venter lidt og prøver igen");
          log(responseCode + " " + httpForb.getResponseMessage() + " for " + url);
          try {
            Thread.sleep(50);
          } catch (InterruptedException ex) {
          }
          // try { Thread.sleep(100); } catch (InterruptedException ex) { }
          continue;
        }

        if (ŝpurado) log("Henter " + url + " og gemmer i " + cacheFilnavn);
        InputStream is = httpForb.getInputStream();
        FileOutputStream fos = new FileOutputStream(cacheFilnavn+"_tmp");
        String indkodning = httpForb.getHeaderField("Content-Encoding");
        Log.d("indkodning: "+indkodning);
        if ("gzip".equals(indkodning)) {
          is = new GZIPInputStream(is); // Pak data ud
        }
        kopierOgLuk(is, fos);
        Log.d(httpForb.getHeaderField("Content-Length") + " blev til "+new File(cacheFilnavn).length());
        cacheFil.delete();
        new File(cacheFilnavn+"_tmp").renameTo(cacheFil);

        if (!false) {
          long lastModified = httpForb.getHeaderFieldDate("last-modified", nu);
          log("last-modified " + new Date(lastModified));
          cacheFil.setLastModified(lastModified);
        }

        return cacheFilnavn;
      }
    }
    throw new IllegalStateException("Dette burde aldrig ske!");
  }

  /**
   * Giver filnavn på hvor URL er gemt i cachet.
   * Hvis filen ikke findes i cachen vil der stadig blive returneret et filnavn.
   * Brug new File(FilCache.findLokaltFilnavn(url)).exists() for at afgøre om en URL findes cachet lokalt
   * @param url
   * @return Stien til hvor filen (muligvis) findes lokalt.
   */
  public static String lokaDosiero(String url) {
    // String cacheFilnavn = url.substring(url.lastIndexOf('/') +
    // 1).replace('?', '_').replace('/', '_').replace('&', '_'); // f.eks.
    // byvejr_dag1?by=2500&mode=long
    String cacheFilnavn = url.replace('?', '_').replace('/', '_').replace('&', '_'); // f.eks.
                                                                                     // byvejr_dag1?by=2500&mode=long
    cacheFilnavn = lagerDir + "/" + cacheFilnavn;
    if (ŝpurado) log("URL: " + url + "  -> " + cacheFilnavn);
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

    // Det kan være nødvendigt at hoppe over BOM mark - se http://android.forums.wordpress.org/topic/xml-pull-error?replies=2
    //is.read(); is.read(); is.read(); // - dette virker kun hvis der ALTID er en BOM
    // Hop over BOM - hvis den er der!
    is = new BufferedInputStream(is);  // bl.a. FileInputStream understøtter ikke mark, så brug BufferedInputStream
    is.mark(1); // vi har faktisk kun brug for at søge én byte tilbage
    if (is.read() == 0xef) {
      is.read();
      is.read();
    } // Der var en BOM! Læs de sidste 2 byte
    else is.reset(); // Der var ingen BOM - hop tilbage til start


    final char[] buffer = new char[0x3000];
    StringBuilder out = new StringBuilder();
    Reader in = new InputStreamReader(is, "UTF-8");
    int read;
    do {
      read = in.read(buffer, 0, buffer.length);
      if (read > 0) {
        out.append(buffer, 0, read);
      }
    } while (read >= 0);
    in.close();
    String jsondata = out.toString();
    return jsondata;
  }

  public static String hentUrlSomStreng(String url) throws IOException {
    String x = akiriDosieron(url, false, false);
    return læsInputStreamSomStreng(new FileInputStream(x));
  }
}
