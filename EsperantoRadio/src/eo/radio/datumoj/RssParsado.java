package eo.radio.datumoj;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * http://code.google.com/p/feedgoal/
 *
 * @author Jacob Nordfalk
 */
class RssParsado {

  /* posterous poluas la fluon per la sekva, kiun ni forprenu!
   <div class='p_embed_description'>
   <span class='p_id3'>teknika_progreso.mp3</span>
   <a href="http://peranto.posterous.com/private/bjuifdqJJD">Listen on Posterous</a>
   </div>
   </div>
   </p>
   p_embed p_image_embed
   p_embed p_audio_embed
   p_embed_description
   */
  static Pattern puriguPosterous1 = Pattern.compile("<div class='p_embed...[^i].+?</div>", Pattern.DOTALL);

  static Pattern puriguPosterous2 = Pattern.compile("<p><a href=\"http://\\w+.posterous.com/[\\w-]+\">Permalink</a>.+?Leave a comment.+?</a>", Pattern.DOTALL);

  static Pattern puriguVinilkosmo = Pattern.compile("<p class=\"who\">.+?</p>", Pattern.DOTALL);

  /** Parser et youtube RSS feed og returnerer det som en liste at Elsendo-objekter */
  static ArrayList<Elsendo> parsiElsendojnDeRss(InputStream is) throws Exception {
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    XmlPullParser p = factory.newPullParser();
    p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
    p.setInput(is, null);
    ArrayList<Elsendo> liste = new ArrayList<Elsendo>();
    Elsendo e = null;
    while (true) {
      int eventType = p.next();
      if (eventType == XmlPullParser.END_DOCUMENT) {
        break;
      }
      if (eventType != XmlPullParser.START_TAG) {
        continue;
      }
      String ns = p.getPrefix(); // namespace
      String tag = p.getName();
      //System.out.println("<" + ns + ":" + tag + ">");

      if ("item".equals(tag)) {
        if (e != null && e.sonoUrl != null) liste.add(e);
        e = new Elsendo();
      } else if (e == null) {
        continue; // Nur sercxu por 'item'
      } else if ("pubDate".equals(tag)) {
        e.datoStr = p.nextText().replaceAll(":00$", "00");// "Thu, 01 Aug 2013 12:01:01 +02:00" -> ..." +0200"
        //Log.d("xxxxx "+e.datoStr);
        e.dato = new Date(Date.parse(e.datoStr));
        e.datoStr = Cxefdatumoj.datoformato.format(e.dato);
      } else if ("image".equals(tag)) {
        e.emblemoUrl = p.nextText();
      } else if ("enclosure".equals(tag)) {
        if ("audio/mpeg".equals(p.getAttributeValue(null, "type"))) {
          e.sonoUrl = p.getAttributeValue(null, "url");
        }
      } else if ("link".equals(tag)) {
        e.ligilo = p.nextText();
      } else if (ns == null && "title".equals(tag)) {
        e.titolo = p.nextText();
      } else if ("description".equals(tag)) {
        e.priskribo = p.nextText().trim();
        e.priskribo = puriguPosterous1.matcher(e.priskribo).replaceAll("");

        Pattern puriguRadioverdaSquarespace  = Pattern.compile("<div class='p_embed...[^i].+?</div>", Pattern.DOTALL);


        while (e.priskribo.startsWith("<p>")) e.priskribo = e.priskribo.substring(3).trim();
        while (e.priskribo.startsWith("</div>")) e.priskribo = e.priskribo.substring(6).trim();
        e.priskribo = puriguPosterous2.matcher(e.priskribo).replaceAll("");

      } else if ("content".equals(ns) && "encoded".equals(tag)) {
        e.priskribo = p.nextText();
      } else if (e.priskribo != null) {
        continue;
      } else if ("summary".equals(tag)) {
        e.priskribo = p.nextText();
      }
    }
    if (e != null && e.sonoUrl != null) liste.add(e);
    is.close();
    Collections.reverse(liste); // Inversa sinsekvo
    return liste;
  }

  //public static final DateFormat vinilkosmoDatoformato = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ", Locale.US);

  /** Parser et youtube RSS feed og returnerer det som en liste at Elsendo-objekter */
  static ArrayList<Elsendo> parsiElsendojnDeRssVinilkosmo(InputStream is) throws Exception {
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    XmlPullParser p = factory.newPullParser();
    p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
    p.setInput(is, null);
    ArrayList<Elsendo> liste = new ArrayList<Elsendo>();
    Elsendo e = null;
    while (true) {
      int eventType = p.next();
      if (eventType == XmlPullParser.END_DOCUMENT) {
        break;
      }
      if (eventType != XmlPullParser.START_TAG) {
        continue;
      }
      String ns = p.getPrefix(); // namespace
      String tag = p.getName();
      //System.out.println("<" + ns + ":" + tag + ">");

      if ("entry".equals(tag)) {
        if (e != null && e.sonoUrl != null) liste.add(e);
        e = new Elsendo();
      } else if (e == null) {
        continue;
      } else if ("title".equals(tag)) {
        e.titolo = p.nextText();
      } else if ("published".equals(tag)) {
        e.datoStr = p.nextText().split("T")[0];
        //Log.d("e.datoStr="+e.datoStr);
        e.dato = Cxefdatumoj.datoformato.parse(e.datoStr);
        e.datoStr = Cxefdatumoj.datoformato.format(e.dato);
      } else if ("link".equals(tag)) {
        String type = p.getAttributeValue(null, "type");
        String href = p.getAttributeValue(null, "href");
        if ("audio/mpeg".equals(type)) {
          e.sonoUrl = href;
        } else if ("image/jpeg".equals(type) && e.emblemoUrl==null) {
          e.emblemoUrl=href;
        } else if ("text/html".equals(type)) {
          e.ligilo=href;
        }
      } else if ("content".equals(tag)) {
        e.priskribo = p.nextText().trim();
        e.priskribo = puriguVinilkosmo.matcher(e.priskribo).replaceAll("");
        while (e.priskribo.startsWith("<p>")) e.priskribo = e.priskribo.substring(3).trim();
        while (e.priskribo.startsWith("</div>")) e.priskribo = e.priskribo.substring(6).trim();
        e.priskribo = puriguPosterous2.matcher(e.priskribo).replaceAll("");
      }
    }
    if (e != null && e.sonoUrl != null) liste.add(e);
    is.close();
    Collections.reverse(liste); // Inversa sinsekvo
    return liste;
  }

}
