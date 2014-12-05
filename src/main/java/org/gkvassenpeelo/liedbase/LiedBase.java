package org.gkvassenpeelo.liedbase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.bible.Bible;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService;
import org.gkvassenpeelo.liedbase.liturgy.Gathering;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart.Type;
import org.gkvassenpeelo.liedbase.liturgy.Scripture;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.liedbase.liturgy.Welcome;
import org.gkvassenpeelo.slidemachine.SlideMachine;
import org.gkvassenpeelo.slidemachine.model.BiblePartFragment;
import org.gkvassenpeelo.slidemachine.model.GenericSlideContent;
import org.pptx4j.Pptx4jException;

public class LiedBase {

    static final Logger logger = Logger.getLogger(LiedBase.class);

    // re-used
    private static final String regex_psalm = "([pP]salm(en)?)";
    private static final String regex_gezang = "([gG]ezang(en)?)";
    private static final String regex_lied = "([lL]ied([bB]oek)?)";
    private static final String regex_opwekking = "([oO]pwekking?)";
    private static final String regex_voorganger = "([vV]oorganger|[dD]ominee)";

    SlideMachine sm = new SlideMachine();

    private List<LiturgyPart> liturgy = new LinkedList<LiturgyPart>();
    private File targetFile = new File("presentatie.pptx");
    private File sourceFile = new File("liturgie.txt");

    private static final String ENCODING = "UTF-8";

    public LiedBase() {
        logger.info("LiedBase gestart");
    }

    /**
     * 
     * @param type
     * @param songNumber
     * @param verse
     * @return
     */
    private String getSongText(SlideContents.Type type, String songNumber, String verse) {
        
        logger.trace("foo");

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

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), ENCODING);

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line again until we end up on
                // the right verse
                while (s.hasNextLine()) {
                    String songLine = s.nextLine();

                    if (songLine.equals(verse)) {
                        StringBuilder verseText = new StringBuilder();
                        while (s.hasNextLine()) {
                            String verseLine = s.nextLine();
                            if (StringUtils.isEmpty(verseLine)) {
                                s.close();
                                return verseText.toString();
                            }
                            verseText.append(verseLine);
                            verseText.append(System.getProperty("line.separator"));
                        }
                        s.close();
                        return verseText.toString();
                    }
                }
            }
        }

        s.close();

        return String.format("Geen tekst gevonden voor %s %s: %s", type.toString(), songNumber, verse);
    }

    private List<String> getOpwekkingSongTekst(String songNumber) {
        List<String> verses = new ArrayList<String>();

        String songBookName = "opwekking.txt";
        String songIdentifier = "opwekking";

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), ENCODING);

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line again until we have all song parts

                // do two extra readlines (into oblivion) so the songtitle and the blank line thereafter are skipped
                s.nextLine();
                s.nextLine();

                while (s.hasNextLine()) {

                    // if (StringUtils.isEmpty(songLine) || StringUtils.startsWith(songLine, "(c)") || "Ned. tekst  arr.: Opwekking".equals(songLine)) {
                    // continue;
                    // }

                    boolean nextSong = false;
                    StringBuilder verseText = new StringBuilder();

                    while (s.hasNextLine() && !nextSong) {

                        // read next line
                        String verseLine = s.nextLine();

                        // check to see if we are not reading too far
                        nextSong = verseLine.matches(String.format("^%s %s$", songIdentifier, Integer.parseInt(songNumber) + 1));

                        if (StringUtils.isEmpty(verseLine)) {
                            if (!StringUtils.isEmpty(verseText.toString()) && !StringUtils.startsWith(verseText.toString(), "Tekst  muziek:")
                                    && !StringUtils.startsWith(verseText.toString(), "(c)")) {
                                verses.add(verseText.toString());
                            }
                            verseText = new StringBuilder();
                            continue;
                        }
                        verseText.append(verseLine);
                        verseText.append(System.getProperty("line.separator"));
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

    /**
     * 
     * @param line
     * @return
     * @throws LiedBaseError
     */
    private LiturgyPart.Type getLiturgyPartTypeFromLiturgyLine(String line) throws LiedBaseError {

        String regex_end_of_morning_service = "(([eE]inde)?.*[mM]orgendienst)";
        String regex_end_of_afternoon_service = "(([eE]inde)?.*[mM]iddagdienst)";
        String regex_amen = "(([gG]ezongen)?.*[aA]men)";
        String regex_votum = "([vV]otum)";
        String regex_gebed = "(([gG]ebed)|([bB]idden)|([dD]anken))";
        String regex_collecte = "(([cC]|[kK])olle[ck]te)";
        String regex_law = "([wW]et)";
        String regex_lecture = "([pP]reek)";
        String regex_agenda = "([aA]genda)";
        String regex = String.format("^[ ]*(%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s).*", regex_agenda, regex_end_of_morning_service, regex_end_of_afternoon_service, regex_amen, regex_votum,
                regex_psalm, regex_gezang, regex_lied, regex_opwekking, regex_gebed, regex_collecte, regex_voorganger, regex_law, regex_lecture);

        // check liturgy part type
        Pattern liturgyPattern = Pattern.compile(regex);
        java.util.regex.Matcher m = liturgyPattern.matcher(line);

        if (!line.matches(regex)) {
            return LiturgyPart.Type.scripture;
        }

        m.find();

        if (m.group(1).matches(regex_psalm)) {
            return LiturgyPart.Type.song;
        } else if (m.group(1).matches(regex_gezang)) {
            return LiturgyPart.Type.song;
        } else if (m.group(1).matches(regex_lied)) {
            return LiturgyPart.Type.song;
        } else if (m.group(1).matches(regex_opwekking)) {
            return LiturgyPart.Type.song;
        } else if (m.group(1).matches(regex_gebed)) {
            return LiturgyPart.Type.prair;
        } else if (m.group(1).matches(regex_collecte)) {
            return LiturgyPart.Type.gathering;
        } else if (m.group(1).matches(regex_agenda)) {
            return LiturgyPart.Type.agenda;
        } else if (m.group(1).matches(regex_voorganger)) {
            return LiturgyPart.Type.welcome;
        } else if (m.group(1).matches(regex_law)) {
            return LiturgyPart.Type.law;
        } else if (m.group(1).matches(regex_lecture)) {
            return LiturgyPart.Type.lecture;
        } else if (m.group(1).matches(regex_votum)) {
            return LiturgyPart.Type.votum;
        } else if (m.group(1).matches(regex_amen)) {
            return LiturgyPart.Type.amen;
        } else if (m.group(1).matches(regex_end_of_morning_service)) {
            return LiturgyPart.Type.endOfMorningService;
        } else if (m.group(1).matches(regex_end_of_afternoon_service)) {
            return LiturgyPart.Type.endOfAfternoonService;
        }

        return null;
    }

    /**
     * 
     * @param inputString
     * @throws BibleException
     * @throws IOException
     */
    public void parseLiturgyScript() throws BibleException {

        String inputString = null;

        try {
            inputString = FileUtils.readFileToString(getSourceFile());
        } catch (IOException e) {
            logger.error(String.format("Invoerbestand '%s' niet gevonden", getSourceFile().getAbsolutePath()));
            System.exit(1);
        }

        if (StringUtils.isEmpty(inputString)) {
            logger.info("liturgie is leeg, niets te doen...");
            System.exit(0);
        }

        StringTokenizer st = new StringTokenizer(inputString, System.getProperty("line.separator"));

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (!line.startsWith("#")) {
                parseLiturgyScriptLine(line);
            }
        }

    }

    private File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File file) {
        this.sourceFile = file;
    }

    /**
     * 
     * @param line
     * @return
     */
    private String getSongNumber(String line) {
        if (line.contains(":")) {
            return StringUtils.substringBetween(line, " ", ":").trim();
        } else {
            return StringUtils.substringAfter(line, " ");
        }
    }

    /**
     * 
     * @param line
     * @throws BibleException
     */
    private String parseLiturgyScriptLine(String line) throws BibleException {

        LiturgyPart.Type type = null;

        try {

            type = getLiturgyPartTypeFromLiturgyLine(line);

        } catch (LiedBaseError e) {
            logger.warn(e.getMessage());
            return line;
        }

        // apply the right formatting to 'line'
        line = format(line, type);

        // create a new liturgy part
        LiturgyPart lp = new LiturgyPart(type);

        if (type == LiturgyPart.Type.song) {

            SlideContents.Type scType = null;

            // determine songtype
            String regex = String.format("^[ ]*(%s|%s|%s|%s).*", regex_psalm, regex_gezang, regex_lied, regex_opwekking);
            Pattern songPattern = Pattern.compile(regex);
            java.util.regex.Matcher m = songPattern.matcher(line);

            m.find();
            if (m.group(1).matches(regex_psalm)) {
                scType = SlideContents.Type.psalm;
            } else if (m.group(1).matches(regex_gezang)) {
                scType = SlideContents.Type.gezang;
            } else if (m.group(1).matches(regex_lied)) {
                scType = SlideContents.Type.lied;
            } else if (m.group(1).matches(regex_opwekking)) {
                scType = SlideContents.Type.opwekking;
            }

            if (scType == SlideContents.Type.opwekking) {

                for (String verse : getOpwekkingSongTekst(getSongNumber(line))) {
                    Song song = new Song(line, verse);
                    lp.addSlide(song);
                }

            } else {

                // quick and dirty fix for character appended songs
                if (getSongNumber(line).matches("179a") || getSongNumber(line).matches("179b")) {
                    line = line + ": 1";
                }

                if (!line.contains(":")) {
                    List<String> allVerses = getVersesFromSong(scType, getSongNumber(line));
                    String displayLine = String.format("%s%s", line, ": ");
                    for (String verse : allVerses) {
                        displayLine += verse + ", ";
                    }
                    line = StringUtils.substringBeforeLast(displayLine, ",");
                }
                // for each verse, create a songSlideContents and add it to the
                // liturgyPart
                StringTokenizer st = new StringTokenizer(StringUtils.substringAfter(line, ":"), ",");
                while (st.hasMoreTokens()) {
                    String currentVerse = st.nextToken().trim();
                    Song song = new Song(line, getSongText(scType, getSongNumber(line), currentVerse));
                    song.setVerseNumber(currentVerse);
                    lp.addSlide(song);
                }

            }

        } else if (type == LiturgyPart.Type.prair) {
            // nothing to do
        } else if (type == LiturgyPart.Type.gathering) {
            lp.addSlide(new Gathering(getGatheringBenificiaries(line)));
        } else if (type == LiturgyPart.Type.welcome) {
            lp.addSlide(new Welcome(getVicarName(line)));
        } else if (type == LiturgyPart.Type.endOfMorningService) {
            EndOfMorningService ems = new EndOfMorningService();
            ems.setTime(getTimeFromLine(line));
            ems.setVicarName(getNextVicarFromLine(line));
            lp.addSlide(ems);
        } else if (type == LiturgyPart.Type.scripture) {
            String bibleBook = Bible.getBibleBookFromLine(line);
            int chapter = Bible.getChapterFromLine(line);
            int fromVerse = Bible.getStartVerseFromLine(line);
            int toVerse = Bible.getEndVerseFromLine(line);
            String translation = Bible.getTranslationFromLine(line);

            List<BiblePartFragment> biblePart = Bible.getBiblePart(translation, Bible.getBibleBookFromLine(line), chapter, fromVerse, toVerse);

            lp.addSlide(new Scripture(biblePart, bibleBook, chapter, fromVerse, toVerse));
        }

        liturgy.add(lp);

        return line;

    }

    private List<String> getVersesFromSong(org.gkvassenpeelo.liedbase.liturgy.SlideContents.Type type, String songNumber) {
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

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), ENCODING);

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line until we have all verses
                while (s.hasNextLine()) {
                    String songLine = s.nextLine();

                    // we are reading the next song, stop it!
                    if (songLine.matches(String.format("^%s %s:.*$", songIdentifier, Integer.parseInt(songNumber) + 1))) {
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

    private String format(String line, Type type) {

        if (type == LiturgyPart.Type.song) {

            //
            // opwekking does not contain ':', return it with upper case first char
            //
            if (!line.contains(":")) {
                line = line.trim();
                return Character.toUpperCase(line.charAt(0)) + line.substring(1);
            }

            StringBuilder result = new StringBuilder();

            //
            // first part is the part before the ':'
            //
            String firstPart = StringUtils.substringBefore(line, ":").trim();

            // make the first character upper case
            firstPart = Character.toUpperCase(firstPart.charAt(0)) + firstPart.substring(1);

            String[] parts = firstPart.split(" ");
            if (parts.length != 2) {
                logger.warn(String.format("Regel '%s' kon niet netjes worden opgemaakt", line));
                return line;
            }
            result.append(String.format("%s %s:", parts[0], parts[1]));

            //
            // second part is the part after the ':'
            //
            String secondPart = StringUtils.substringAfter(line, ":");

            parts = secondPart.split(",");
            int i = 0;
            for (String s : parts) {
                result.append(String.format(" %s", s.trim()));
                if (++i < parts.length) {
                    result.append(",");
                }
            }

            return result.toString();
        }

        return line;
    }

    String getTimeFromLine(String line) {

        String data = StringUtils.substringAfter(line, ":");

        return StringUtils.substringBefore(data, ",").trim();
    }

    String getNextVicarFromLine(String line) {

        String data = StringUtils.substringAfter(line, ":");

        return StringUtils.substringAfter(data, ",").trim();
    }

    private String getVicarName(String line) {

        Pattern p = Pattern.compile(regex_voorganger + ":?(.*)");
        Matcher m = p.matcher(line);

        m.find();

        return m.group(2).trim();
    }

    private List<String> getGatheringBenificiaries(String line) {

        List<String> benificiaries = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(StringUtils.substringAfter(line, ":"), ",");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            benificiaries.add(s);
        }

        return benificiaries;
    }

    /**
     * 
     */
    public void createSlides() {

        try {
            // Liturgy parsed and created, time to create Slides
            sm.setTargetFile(getTargetFile());
            sm.init();

            for (LiturgyPart lp : liturgy) {

                if (lp.getType() == LiturgyPart.Type.song) {

                    for (SlideContents sc : lp.getSlides()) {

                        GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.Song();

                        ((org.gkvassenpeelo.slidemachine.model.Song) gsc).setCurrentVerse(((Song) sc).getVerseNumber());
                        gsc.setHeader(sc.getHeader());
                        gsc.setBody(sc.getBody());

                        sm.addSlide(gsc);

                    }
                } else if (lp.getType() == LiturgyPart.Type.gathering) {
                    GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.Gathering();
                    ((org.gkvassenpeelo.slidemachine.model.Gathering) gsc).setFirstBenificiary(((org.gkvassenpeelo.liedbase.liturgy.Gathering) lp.getSlides().get(0))
                            .getFirstGatheringBenificiary());
                    ((org.gkvassenpeelo.slidemachine.model.Gathering) gsc).setSecondBenificiary(((org.gkvassenpeelo.liedbase.liturgy.Gathering) lp.getSlides().get(0))
                            .getSecondGatheringBenificiary());
                    sm.addSlide(gsc);
                } else if (lp.getType() == LiturgyPart.Type.welcome) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Welcome(((Welcome) lp.getSlides().get(0)).getVicarName()));
                } else if (lp.getType() == LiturgyPart.Type.prair) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Prair());
                } else if (lp.getType() == LiturgyPart.Type.votum) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Votum());
                } else if (lp.getType() == LiturgyPart.Type.endOfMorningService) {

                    GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.EndMorningService();

                    ((org.gkvassenpeelo.slidemachine.model.EndMorningService) gsc).setTime(((org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService) lp.getSlides().get(0))
                            .getTime());
                    ((org.gkvassenpeelo.slidemachine.model.EndMorningService) gsc).setVicarName(((org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService) lp.getSlides().get(0))
                            .getVicarName());

                    sm.addSlide(gsc);
                } else if (lp.getType() == LiturgyPart.Type.endOfAfternoonService) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.EndAfternoonService());
                } else if (lp.getType() == LiturgyPart.Type.amen) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Amen());
                } else if (lp.getType() == LiturgyPart.Type.law) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Law());
                } else if (lp.getType() == LiturgyPart.Type.lecture) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Lecture());
                } else if (lp.getType() == LiturgyPart.Type.scripture) {
                    GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.Scripture(lp.getSlides().get(0));
                    sm.addSlide(gsc);
                }

                // after each liturgy part, add an empty slide, except for the last one!
                if (lp.getType() != LiturgyPart.Type.endOfMorningService && lp.getType() != LiturgyPart.Type.endOfAfternoonService) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Blank());
                }

            }

        } catch (JAXBException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        } catch (Pptx4jException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        } catch (Docx4JException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File filename) {
        this.targetFile = filename;
    }

    public void save() {
        try {
            sm.save();
        } catch (Docx4JException e) {
            logger.error(String.format("Het bestand kon niet worden opgeslagen op: %s", getTargetFile().getAbsolutePath()));
            logger.error("Controleer of het niet is geopend in PowerPoint...");
            System.exit(1);
        }
        logger.info(String.format("Presentatie opgeslagen op: %s", getTargetFile().getAbsolutePath()));
    }

    public static void main(String[] args) throws Docx4JException, BibleException {

        try {
            LiedBase lb = new LiedBase();

            lb.parseLiturgyScript();
            lb.createSlides();
            lb.save();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
