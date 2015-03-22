package org.gkvassenpeelo.liedbase.songbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyBuilder;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;

public class SongBook {

    static final Logger logger = Logger.getLogger(SongBook.class);

    /**
     * 
     * @param type
     * @param songNumber
     * @param verse
     * @return
     */
    public static List<SongLine> getSongText(SlideContents.Type type, String songNumber, String verse) {

        List<SongLine> songText = new ArrayList<SongLine>();

        String songBookName = "";
        String songIdentifier = "";

        if (type == SlideContents.Type.psalm) {
            songBookName = "psalmen.txt";
            songIdentifier = "psalm";
        }

        if (type == SlideContents.Type.gezang) {
            songBookName = "gezangen.txt";
            songIdentifier = "gereformeerd kerkboek";
        }

        if (type == SlideContents.Type.lied) {
            songBookName = "liedboek.txt";
            songIdentifier = "lied";
        }

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiturgyBuilder.ENCODING);

        int songInteger = 0;
        int verseInteger = 0;
        String songNumberPostfix = "";
        String nextSongNumberPostfix = "";

        verseInteger = Integer.parseInt(verse);

        if (songNumber.matches("^[0-9]+$")) {
            songInteger = Integer.parseInt(songNumber);
        } else if (songNumber.matches("^[0-9]+[a-z]{1}$")) {
            songInteger = Integer.parseInt(songNumber.substring(0, songNumber.length() - 1));
            songNumberPostfix = songNumber.substring(songNumber.length() - 1, songNumber.length());
            int charValue = songNumberPostfix.charAt(0);
            nextSongNumberPostfix = String.valueOf((char) (charValue + 1));
        }

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line again until we end up on
                // the right verse
                while (s.hasNextLine()) {
                    line = s.nextLine();

                    if (line.equals(verse)) {
                        while (s.hasNextLine()) {
                            line = s.nextLine();
                            // reading next song, return
                            if (line.matches(String.format("^%s %s%s:.*$", songIdentifier, songInteger + 1, songNumberPostfix))) {
                                s.close();

                                cleanup(songText);
                                return songText;
                            }
                            if (line.matches(String.format("^%s %s%s:.*$", songIdentifier, songInteger, nextSongNumberPostfix))) {
                                s.close();

                                cleanup(songText);
                                return songText;
                            }
                            // reading next verse, return
                            if (line.equals("" + (verseInteger + 1))) {
                                s.close();

                                cleanup(songText);
                                return songText;
                            }
                            songText.add(new SongLine(SongLine.DisplayType.normal, line));
                        }
                        s.close();

                        // remove last white line
                        cleanup(songText);
                        return songText;
                    }
                }
            }
        }

        s.close();

        songText.add(new SongLine(SongLine.DisplayType.normal, String.format("Geen tekst gevonden voor %s %s: %s", type.toString(), songNumber, verse)));
        return songText;
    }

    private static void cleanup(List<SongLine> songText) {
        if (((SongLine) songText.get(songText.size() - 1)).getContent().equals("")) {
            songText.remove(songText.size() - 1);
        }
    }

    public static List<List<SongLine>> getOpwekkingSongTekst(String songNumber) {
        List<List<SongLine>> verses = new ArrayList<List<SongLine>>();

        String songBookName = "opwekking.txt";
        String songIdentifier = "opwekking";

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiturgyBuilder.ENCODING);

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line again until we have all song
                // parts

                // do two extra readlines (into oblivion) so the songtitle and
                // the blank line thereafter are skipped
                s.nextLine();
                s.nextLine();

                while (s.hasNextLine()) {

                    // if (StringUtils.isEmpty(songLine) ||
                    // StringUtils.startsWith(songLine, "(c)") ||
                    // "Ned. tekst  arr.: Opwekking".equals(songLine)) {
                    // continue;
                    // }

                    boolean nextSong = false;
                    List<SongLine> verseText = new ArrayList<SongLine>();

                    while (s.hasNextLine() && !nextSong) {

                        // read next line
                        String verseLine = s.nextLine();

                        // check to see if we are not reading too far
                        nextSong = verseLine.matches(String.format("^%s %s$", songIdentifier, Integer.parseInt(songNumber) + 1));

                        if (StringUtils.isEmpty(verseLine)) {
                            if (!StringUtils.isEmpty(verseText.toString()) && !StringUtils.startsWith(verseText.toString(), "Tekst  muziek:")
                                    && !StringUtils.startsWith(verseText.toString(), "(c)")) {
                                verses.add(verseText);
                            }
                            verseText = new ArrayList<SongLine>();
                            continue;
                        }
                        verseText.add(new SongLine(SongLine.DisplayType.normal, verseLine));
                    }

                    return verses;
                }

                s.close();
                return verses;
            }
        }

        s.close();
        return verses;
    }

    public static List<String> getVersesFromSong(SlideContents.Type type, String songNumber) {
        List<String> verses = new ArrayList<String>();

        String songBookName = "";
        String songIdentifier = "";

        if (type == SlideContents.Type.psalm) {
            songBookName = "psalmen.txt";
            songIdentifier = "psalm";
        }

        if (type == SlideContents.Type.gezang) {
            songBookName = "gezangen.txt";
            songIdentifier = "gereformeerd kerkboek";
        }

        if (type == SlideContents.Type.lied) {
            songBookName = "liedboek.txt";
            songIdentifier = "lied";
        }

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiturgyBuilder.ENCODING);

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line until we have all verses
                while (s.hasNextLine()) {
                    String songLine = s.nextLine();

                    // we are reading the next song, stop!
                    String tempSongNumber = songNumber;
                    if (tempSongNumber.matches("^[0-9]*[a-z]{1}$")) {
                        tempSongNumber = songNumber.substring(0, songNumber.length() - 1);
                    }
                    // next song in integers
                    if (songLine.matches(String.format("^%s %s[a-z]*:.*$", songIdentifier, Integer.parseInt(tempSongNumber) + 1))) {
                        s.close();
                        return verses;
                    }
                    // next song alphabetically
                    if (songLine.matches(String.format("^%s %s[a-z]*:.*$", songIdentifier, Integer.parseInt(tempSongNumber)))) {
                        s.close();
                        return verses;
                    }

                    try {
                        Integer.parseInt(songLine);
                        verses.add(songLine);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
        }

        s.close();

        return verses;
    }

    /**
     * 
     * @param line
     * @return
     */
    public static String getSongNumber(String line) {
        if (line.contains(":")) {
            return StringUtils.substringBetween(line, " ", ":").trim();
        } else {
            return StringUtils.substringAfter(line, " ");
        }
    }

}
