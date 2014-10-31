package org.gkvassenpeelo.liedbase;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.liturgy.Gathering;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.Prair;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.slidemachine.SlideMachine;
import org.gkvassenpeelo.slidemachine.model.GenericSlideContent;
import org.pptx4j.Pptx4jException;

public class LiedBase {

    // re-used
    private String regex_psalm = "([pP]salm(en)?)";
    private String regex_gezang = "([gG]ezang(en)?)";
    private String regex_lied = "([lL]ied([bB]oek)?)";
    private String regex_opwekking = "([oO]pwekking?)";

    private List<LiturgyPart> liturgy = new LinkedList<LiturgyPart>();

    /**
     * 
     * @param type
     * @param songNumber
     * @param verse
     * @return
     */
    private String getSongText(SlideContents.Type type, String songNumber, String verse) {
        if (type == SlideContents.Type.psalm) {
            Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("psalmen.txt"));
            int lineNum = 0;
            while (s.hasNextLine()) {
                String line = s.nextLine();
                lineNum++;
                if (line.matches("^psalm " + songNumber + ": 1.*$")) {
                    System.out.println(lineNum + " " + line);
                    // we have the line number on which the psalm starts
                    // continue reading from that line again until we end up on the right verse
                    while (s.hasNextLine()) {
                        String psalmLine = s.nextLine();
                        if (psalmLine.equals(verse)) {
                            StringBuilder verseText = new StringBuilder();
                            while (s.hasNextLine()) {
                                String verseLine = s.nextLine();
                                if (StringUtils.isEmpty(verseLine)) {
                                    return verseText.toString();
                                }
                                verseText.append(verseLine);
                                verseText.append(System.getProperty("line.separator"));
                            }
                            return verseText.toString();
                        }
                    }
                }
            }

        }
        return String.format("Geen tekst gevonden voor %s %s: %s", type.toString(), songNumber, verse);
    }

    /**
     * 
     * @param name
     * @return
     * @throws LiedBaseError
     */
    private LiturgyPart.Type getLiturgyPartTypeFromLiturgyLine(String name) throws LiedBaseError {

        String regex_gebed = "([gG]ebed)";
        String regex_collecte = "([cC]ollecte)";
        String regex_voorganger = "([vV]oorganger|[dD]ominee)";
        String regex_law = "([wW]et)";
        String regex_lecture = "([pP]reek)";
        String regex = String.format("^[ ]*(%s|%s|%s|%s|%s|%s|%s|%s|%s).*", regex_psalm, regex_gezang, regex_lied, regex_opwekking,
                regex_gebed, regex_collecte, regex_voorganger, regex_law, regex_lecture);

        // check liturgy part type
        Pattern liturgyPattern = Pattern.compile(regex);
        java.util.regex.Matcher m = liturgyPattern.matcher(name);

        if (!name.matches(regex)) {
            throw new LiedBaseError("Onbekend liturgie onderdeel: " + name);
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
        }

        return null;
    }

    /**
     * 
     * @param inputString
     */
    public void parseLiturgyScript(String inputString) {
        StringTokenizer st = new StringTokenizer(inputString, System.getProperty("line.separator"));

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            parseLiturgyScriptLine(line);
        }

    }

    /**
     * 
     * @param line
     * @return
     */
    private String getSongNumber(String line) {
        return StringUtils.substringBetween(line, " ", ":").trim();
    }

    /**
     * 
     * @param line
     */
    private void parseLiturgyScriptLine(String line) {
        try {
            LiturgyPart.Type type = getLiturgyPartTypeFromLiturgyLine(line);

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

                // for each verse, create a songSlideContents and add it to the liturgyPart
                StringTokenizer st = new StringTokenizer(StringUtils.substringAfter(line, ":"), ",");
                while (st.hasMoreTokens()) {
                    String currentVerse = st.nextToken();
                    lp.addSlide(new Song(line, getSongText(scType, getSongNumber(line), currentVerse.trim())));
                }
            } else if (type == LiturgyPart.Type.prair) {
                lp.addSlide(new Prair());
            } else if (type == LiturgyPart.Type.gathering) {
                lp.addSlide(new Gathering());
            }

            liturgy.add(lp);

        } catch (LiedBaseError e) {
            System.err.println(e.getMessage());
        }

    }

    /**
     * 
     */
    public void createSlides() {

        try {
            // Liturgy parsed and created, time to create SlideMachine
            SlideMachine sm = new SlideMachine();
            sm.setTargetFile("/target/presentation.pptx");
            sm.init();

            for (LiturgyPart lp : liturgy) {

                if (lp.getType() == LiturgyPart.Type.song) {

                    for (SlideContents sc : lp.getSlides()) {

                        GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.Song();

                        gsc.setHeader(sc.getHeader());
                        gsc.setBody(sc.getBody());

                        sm.addSlide(gsc);

                    }
                } else if (lp.getType() == LiturgyPart.Type.gathering) {
                    GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.Gathering();
                    ((org.gkvassenpeelo.slidemachine.model.Gathering) gsc).setFirstBenificiary("De driehoek");
                    ((org.gkvassenpeelo.slidemachine.model.Gathering) gsc).setSecondBenificiary("Kerk");
                    sm.addSlide(gsc);
                } else if (lp.getType() == LiturgyPart.Type.welcome) {
                 
                } else if (lp.getType() == LiturgyPart.Type.prair) {
                 
                } else if (lp.getType() == LiturgyPart.Type.law) {
                    
                } else if (lp.getType() == LiturgyPart.Type.lecture) {

                }

                // after each liturgy part, add an empty slide
                sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Blank());

            }

            sm.save();

        } catch (JAXBException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Pptx4jException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Docx4JException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
