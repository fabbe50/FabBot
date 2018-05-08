package com.fabbe50.teemobeats.music;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 23/09/2017 - 10:52 PM.
 */
class Lyrics {
    private final static String songLyricsURL = "http://www.songlyrics.com";

    static List<String> getSongLyrics(String band, String songTitle) throws IOException {
        List<String> lyrics = new ArrayList<>();

        Document doc = Jsoup.connect(songLyricsURL + "/" + band.replace(" ", "-").toLowerCase() + "/"+songTitle.replace(" ", "-").toLowerCase() + "-lyrics/").get();
        String title = doc.title();
        System.out.println(title);
        Element p = doc.select("p.songLyricsV14").get(0);
        for (Node e : p.childNodes()) {
            if (e instanceof TextNode) {
                lyrics.add(((TextNode)e).getWholeText());
            }
        }
        return lyrics;
    }

    static List<String> getSongLyrics(String URL) throws IOException {
        List<String> lyrics = new ArrayList<>();

        Document doc = Jsoup.connect(URL).get();
        String title = doc.title();
        System.out.println(title);
        Element p = doc.select("p.songLyricsV14").get(0);
        for (Node e : p.childNodes()) {
            if (e instanceof TextNode) {
                lyrics.add(((TextNode)e).getWholeText());
            }
        }
        return lyrics;
    }
}
