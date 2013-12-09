/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author bwarshaw
 */
public class HtmlUtil {

    public static Dagr buildHtmlDagr(String path, String user, String name) {
        Document doc;
        if (path.contains("http")) {
            try {
                doc = Jsoup.connect(path).get();
            } catch (IOException ioe) {
                Webservice.logger.log(Level.SEVERE, null, ioe);
                return null;
            }
        } else {
            try {
                doc = Jsoup.parse(new File(path), "UTF-8");
            } catch (IOException ioe) {
                Webservice.logger.log(Level.SEVERE, null, ioe);
                return null;
            }
        }
        Dagr root = new Dagr(path, user, name);
        root.annotations = findKeywordsInPath(doc);
        ArrayList<String> subElementLinks = findSubElements(doc);
        for (String elem : subElementLinks) {
            Dagr elemDagr = new Dagr(elem, user, "");
            root.children.add(elemDagr);
        }
        return root;
        /*
         * compose list of all important elements in the document
         * for each element, create a dagr, add it to root's children
         * OR USE abs:href/src
         * return root
         */
    }

    private static String findKeywordsInPath(Document doc) {
        Element meta = doc.select("meta[name=keywords]").first();
        String keywords = "";
        if (meta != null) {
            keywords = doc.select("meta[name=keywords]").first().attr("content");
        }
        String title = doc.title() + " ";
        String annotations = keywords + title;
        int max = 256;
        if (annotations.length() < max) {
            max = annotations.length();
        }
        return annotations.substring(0, max);   // needs to fit into DB
    }

    private static ArrayList<String> findSubElements(Document doc) {
        Elements aUrls = doc.select("a[href]");
        Elements videos = doc.select("video[src]");
        Elements audios = doc.select("audio[src]");
        Elements images = doc.select("img[src]");
        Elements objects = doc.select("object[data]");
        Elements embeds = doc.select("embed[src]");
        ArrayList<String> links = new ArrayList<String>();
        for (Element elem : aUrls) {
            String link = elem.attr("abs:href");
            if (link.length() < 256) {
                links.add(elem.attr("abs:href"));
            }
        }
        for (Element elem : videos) {
            String link = elem.attr("abs:src");
            if (link.length() < 256) {
                links.add(elem.attr("abs:href"));
            }
        }
        for (Element elem : audios) {
            String link = elem.attr("abs:src");
            if (link.length() < 256) {
                links.add(elem.attr("abs:href"));
            }
        }
        for (Element elem : images) {
            String link = elem.attr("abs:src");
            if (link.length() < 256) {
                links.add(elem.attr("abs:href"));
            }
        }
        for (Element elem : objects) {
            String link = elem.attr("abs:data");
            if (link.length() < 256) {
                links.add(elem.attr("abs:href"));
            }
        }
        for (Element elem : embeds) {
            String link = elem.attr("abs:src");
            if (link.length() < 256) {
                links.add(elem.attr("abs:href"));
            }
        }
        Collection<String> empty = new ArrayList<String>();
        empty.add("");
        links.removeAll(empty);
        return links;
    }

    public static void main(String args[]) {
        Dagr espnDagr = buildHtmlDagr("http://www.espn.com", "bill", "");
        for (Dagr d : espnDagr.children) {
            System.out.println(d.filePath);
        }
        System.out.println(espnDagr.annotations);
    }
}
