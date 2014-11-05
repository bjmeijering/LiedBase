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
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.liturgy.Gathering;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.liedbase.liturgy.Welcome;
import org.gkvassenpeelo.slidemachine.SlideMachine;
import org.gkvassenpeelo.slidemachine.model.GenericSlideContent;
import org.pptx4j.Pptx4jException;

public class LiedBase {

    static final Logger logger = Logger.getLogger(LiedBase.class);

    // re-used
    private String regex_psalm = "([pP]salm(en)?)";
    private String regex_gezang = "([gG]ezang(en)?)";
    private String regex_lied = "([lL]ied([bB]oek)?)";
    private String regex_opwekking = "([oO]pwekking?)";
    private String regex_voorganger = "([vV]oorganger|[dD]ominee)";

    SlideMachine sm = new SlideMachine();

    private List<LiturgyPart> liturgy = new LinkedList<LiturgyPart>();
    private File targetFile = new File("presentatie.pptx");
    private File sourceFile = new File("liturgie.txt");

    public LiedBase() {

        logger.setLevel(Level.INFO);

        Layout layout = new PatternLayout("%d{HH:mm:ss} %5p: %m%n");
        Appender fileAppender;
        try {
            fileAppender = new RollingFileAppender(layout, "liedbase.log");
            fileAppender.setName("User log");
            fileAppender.setLayout(layout);
            logger.addAppender(fileAppender);
        } catch (IOException e) {
            logger.error(String.format("Kan bestand niet aanmaken: %s", "user.log"));
        }

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

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream(songBookName));

        while (s.hasNextLine()) {

            String line = s.nextLine();

            if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
                // we have the line number on which the song starts
                // continue reading from that line again until we end up on
                // the right verse
                while (s.hasNextLine()) {
                    String songLine = s.nextLine();

                    // we are reading the next song, stop it!
                    if (songLine.matches(String.format("^%s %s:.*$", songIdentifier, Integer.parseInt(songNumber) + 1))) {
                        break;
                    }

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

        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream(songBookName), "UTF-8");

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
                            if (!StringUtils.isEmpty(verseText.toString()) &&
                                    !StringUtils.startsWith(verseText.toString(), "Tekst  muziek:") &&
                                    !StringUtils.startsWith(verseText.toString(), "(c)")) {
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
        String regex_collecte = "([cC]ollecte)";
        String regex_law = "([wW]et)";
        String regex_lecture = "([pP]reek)";
        String regex = String.format("^[ ]*(%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s).*", regex_end_of_morning_service, regex_end_of_afternoon_service, regex_amen, regex_votum,
                regex_psalm, regex_gezang, regex_lied, regex_opwekking, regex_gebed, regex_collecte, regex_voorganger, regex_law, regex_lecture);

        // check liturgy part type
        Pattern liturgyPattern = Pattern.compile(regex);
        java.util.regex.Matcher m = liturgyPattern.matcher(line);

        if (!line.matches(regex)) {
            throw new LiedBaseError("Onbekend liturgie onderdeel: " + line);
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
     * @throws IOException
     */
    public void parseLiturgyScript() {

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
            parseLiturgyScriptLine(line);
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
     */
    private void parseLiturgyScriptLine(String line) {

        LiturgyPart.Type type = null;

        try {

            type = getLiturgyPartTypeFromLiturgyLine(line);

        } catch (LiedBaseError e) {
            logger.warn(e.getMessage());
            return;
        }

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

            if (scType != SlideContents.Type.opwekking) {
                // for each verse, create a songSlideContents and add it to the
                // liturgyPart
                StringTokenizer st = new StringTokenizer(StringUtils.substringAfter(line, ":"), ",");
                while (st.hasMoreTokens()) {
                    String currentVerse = st.nextToken().trim();
                    Song song = new Song(line, getSongText(scType, getSongNumber(line), currentVerse));
                    song.setVerseNumber(currentVerse);
                    lp.addSlide(song);
                }
            } else {

                for (String verse : getOpwekkingSongTekst(getSongNumber(line))) {
                    Song song = new Song(line, verse);
                    lp.addSlide(song);
                }

            }
        } else if (type == LiturgyPart.Type.prair) {
            // nothing to do
        } else if (type == LiturgyPart.Type.gathering) {
            lp.addSlide(new Gathering(getGatheringBenificiaries(line)));
        } else if (type == LiturgyPart.Type.welcome) {
            lp.addSlide(new Welcome(getVicarName(line)));
        }

        liturgy.add(lp);

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
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.EndMorningService());
                } else if (lp.getType() == LiturgyPart.Type.endOfAfternoonService) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.EndAfternoonService());
                } else if (lp.getType() == LiturgyPart.Type.amen) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Amen());
                } else if (lp.getType() == LiturgyPart.Type.law) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Law());
                } else if (lp.getType() == LiturgyPart.Type.lecture) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Lecture());
                }

                // after each liturgy part, add an empty slide, except for the last one!
                if (lp.getType() != LiturgyPart.Type.endOfMorningService && lp.getType() != LiturgyPart.Type.endOfAfternoonService) {
                    sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Blank());
                }

            }

        } catch (JAXBException e) {
            logger.error(e.getMessage());
            System.exit(1);
        } catch (Pptx4jException e) {
            logger.error(e.getMessage());
            System.exit(1);
        } catch (Docx4JException e) {
            logger.error(e.getMessage());
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

    public static void main(String[] args) throws Docx4JException {

        // check arguments, duh!
        if (args.length == 0) {
            // use the defaults and continue
        }

        LiedBase lb = new LiedBase();

        lb.parseLiturgyScript();
        lb.createSlides();
        lb.save();
    }
}
