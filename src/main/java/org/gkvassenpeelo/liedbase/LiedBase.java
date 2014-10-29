package org.gkvassenpeelo.liedbase;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.slidemachine.SlideMachine;
import org.gkvassenpeelo.slidemachine.model.GenericSlideContent;
import org.pptx4j.Pptx4jException;

public class LiedBase {

    private enum LiturgyPartType {
        psalm, gezang, lied
    };

    private List<LiturgyPart> liturgy = new LinkedList<LiturgyPart>();

    /**
     * 
     * @param type
     * @param songNumber
     * @param verse
     * @return
     */
    private String getSongText(LiturgyPartType type, String songNumber, String verse) {
        if (type == LiturgyPartType.psalm) {
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
                                if(StringUtils.isEmpty(verseLine)) {
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
    private LiturgyPartType getLiturgyPartTypeFromLiturgyLine(String name) throws LiedBaseError {

        String regex_psalm = "([pP]salm(en)?)";
        String regex_gezang = "([gG]ezang(en)?)";
        String regex_lied = "([lL]ied([bB]oek)?)";
        String regex = String.format("^[ ]*(%s|%s|%s).*", regex_psalm, regex_gezang, regex_lied);

        // check songbook name
        Pattern songbookPattern = Pattern.compile(regex);
        java.util.regex.Matcher m = songbookPattern.matcher(name);

        if (!name.matches(regex)) {
            throw new LiedBaseError("Onbekend liturgie onderdeel: " + name);
        }

        m.find();

        if (m.group(1).matches(regex_psalm)) {
            return LiturgyPartType.psalm;
        } else if (m.group(1).matches(regex_gezang)) {
            return LiturgyPartType.gezang;
        } else if (m.group(1).matches(regex_lied)) {
            return LiturgyPartType.lied;
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
        return StringUtils.substringBetween(line, " ", ":");
    }

    /**
     * 
     * @param line
     */
    private void parseLiturgyScriptLine(String line) {
        try {
            LiturgyPartType type = getLiturgyPartTypeFromLiturgyLine(line);

            // create a new liturgy part
            LiturgyPart lp = new LiturgyPart();

            // for each verse, create a songSlideContents and add it to the liturgyPart
            StringTokenizer st = new StringTokenizer(StringUtils.substringAfter(line, ":"), ",");
            while (st.hasMoreTokens()) {
                String currentVerse = st.nextToken();
                lp.addSlide(new Song(line, getSongText(type, getSongNumber(line), currentVerse)));
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

                for (SlideContents sc : lp.getSlides()) {

                    GenericSlideContent gsc = new org.gkvassenpeelo.slidemachine.model.Song();

                    gsc.setHeader(sc.getHeader());
                    gsc.setBody(sc.getBody());

                    sm.addSlide(gsc);

                }

                // after each liturgy part, add an empty slide
                sm.addSlide(new org.gkvassenpeelo.slidemachine.model.Song());

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
