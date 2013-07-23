package dk.nordfalk.esperanto.radio.datumoj;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * http://code.google.com/p/feedgoal/
 *
 * @author Jacob Nordfalk
 */
public class RssParsado {

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

/*
</p>
D/EsperantoRadio(23188):
D/EsperantoRadio(23188): <p><a href="http://peranto.posterous.com/teknika-progreso-gravas-ankau-por-ni">Permalink</a>
D/EsperantoRadio(23188):
D/EsperantoRadio(23188): 	| <a href="http://peranto.posterous.com/teknika-progreso-gravas-ankau-por-ni#comment">Leave a comment&nbsp;&nbsp;&raquo;</a>
D/EsperantoRadio(23188):
D/EsperantoRadio(23188): </p>
*/
	static Pattern puriguPosterous2 = Pattern.compile("<p><a href=\"http://\\w+.posterous.com/[\\w-]+\">Permalink</a>.+?Leave a comment.+?</a>", Pattern.DOTALL);

	/** Parser et youtube RSS feed og returnerer det som en liste at Elsendo-objekter */
	public static ArrayList<Elsendo> parsuElsendojnDeRss(InputStream is) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser p = factory.newPullParser();
		p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		p.setInput(is, null);
		ArrayList<Elsendo> liste = new ArrayList<Elsendo>();
		Elsendo k = null;
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
			System.out.println("<" + ns + ":" + tag + ">");

			if ("item".equals(tag)) {
				if (k!=null && k.sonoUrl!=null) liste.add(k);
				k = new Elsendo();
			} else if (k==null) {
				continue; // Nur sercxu por 'item'
			} else if ("pubDate".equals(tag)) {
				k.dato = new Date(Date.parse(p.nextText()));
				k.datoStr = Cxefdatumoj.datoformato.format(k.dato);
			} else if ("image".equals(tag)) {
				k.emblemoUrl = p.nextText();
			} else if ("enclosure".equals(tag)) {
				if ("audio/mpeg".equals(p.getAttributeValue(null, "type"))) {
					k.sonoUrl = p.getAttributeValue(null, "url");
				}
			} else if ("link".equals(tag)) {
				k.ligilo = p.nextText();
			} else if (ns==null && "title".equals(tag)) {
				k.titolo = p.nextText();
			} else if ("description".equals(tag)) {
				k.priskribo = p.nextText().trim();
				k.priskribo = puriguPosterous1.matcher(k.priskribo).replaceAll("");
				while (k.priskribo.startsWith("<p>")) k.priskribo = k.priskribo.substring(3).trim();
				while (k.priskribo.startsWith("</div>")) k.priskribo = k.priskribo.substring(6).trim();
				k.priskribo = puriguPosterous2.matcher(k.priskribo).replaceAll("");

			} else if ("content".equals(ns) && "encoded".equals(tag)) {
				k.priskribo = p.nextText();
			} else if (k.priskribo!=null) {
				continue;
			} else if ("summary".equals(tag)) {
				k.priskribo = p.nextText();
			}
		}
		if (k!=null && k.sonoUrl!=null) liste.add(k);
		is.close();
		Collections.reverse(liste); // Inversa sinsekvo
		return liste;
	}


	/** Parser et youtube RSS feed og returnerer det som en liste at Elsendo-objekter */
	public static ArrayList<Elsendo> parsuElsendojnDeRssPeranto(InputStream is) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser p = factory.newPullParser();
		p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		p.setInput(is, null);
		ArrayList<Elsendo> liste = new ArrayList<Elsendo>();
		Elsendo k = null;
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
			System.out.println("<" + ns + ":" + tag + ">");

			if ("item".equals(tag)) {
				if (k!=null && k.sonoUrl!=null) liste.add(k);
				k = new Elsendo();
			} else if (k==null) {
				continue; // Nur sercxu por 'item'
			} else if ("pubDate".equals(tag)) {
				k.dato = new Date(Date.parse(p.nextText()));
				k.datoStr = Cxefdatumoj.datoformato.format(k.dato);
			} else if ("image".equals(tag)) {
				k.emblemoUrl = p.nextText();
			} else if ("enclosure".equals(tag)) {
				if ("audio/mpeg".equals(p.getAttributeValue(null, "type"))) {
					k.sonoUrl = p.getAttributeValue(null, "url");
				}
			} else if ("link".equals(tag)) {
				k.ligilo = p.nextText();
			} else if (ns==null && "title".equals(tag)) {
				k.titolo = p.nextText();
			} else if ("description".equals(tag)) {
				k.priskribo = p.nextText().trim();
				k.priskribo = puriguPosterous1.matcher(k.priskribo).replaceAll("");
				while (k.priskribo.startsWith("<p>")) k.priskribo = k.priskribo.substring(3).trim();
				while (k.priskribo.startsWith("</div>")) k.priskribo = k.priskribo.substring(6).trim();
				k.priskribo = puriguPosterous2.matcher(k.priskribo).replaceAll("");

			} else if ("content".equals(ns) && "encoded".equals(tag)) {
				k.priskribo = p.nextText();
			} else if (k.priskribo!=null) {
				continue;
			} else if ("summary".equals(tag)) {
				k.priskribo = p.nextText();
			}
		}
		if (k!=null && k.sonoUrl!=null) liste.add(k);
		is.close();
		Collections.reverse(liste); // Inversa sinsekvo
		return liste;
	}

}
