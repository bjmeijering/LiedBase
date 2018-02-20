package org.gkvassenpeelo.liedbase.bible;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyItem;

public class Bible {

    static final Logger logger = Logger.getLogger(Bible.class);

    private static String DEFAULT_TRANSLATION = "NBV";

    private enum CharType {
        number, character, dash, colon, comma, space
    }
    public static final String ENCODING = "UTF-8";

    private static String LINE_END = System.getProperty("line.separator");

    public Bible() {

    }

    public static String formatLine(String bibleBook, String translation, int chapter, int fromVerse, int toVerse) {
        if (translation != null && translation.equals("NBV"))
            translation = "";
        else
            translation = String.format("(%s)", translation);
        if (fromVerse == toVerse)
            return String.format("%s %s: %s %s", bibleBook, chapter, fromVerse, translation);
        if (fromVerse == 0 && toVerse == 999)
            return String.format("%s %s %s", bibleBook, chapter, translation);

        return String.format("%s %s: %s - %s %s", bibleBook, chapter, fromVerse, toVerse, translation);
    }

    public static List<BiblePartFragment> getBiblePartFromText(String translation, String book, int chapter, int fromVerse, int toVerse) throws BibleException {

        List<BiblePartFragment> fragmentList = new ArrayList<BiblePartFragment>();

        translation = translation.toUpperCase();

        book = book.toLowerCase();

        book = book.replaceAll("ë", "e");
        book = book.replaceAll("ï", "i");
        book = book.replaceAll("ü", "u");

        boolean addVerse = false;
        Scanner s = null;

        try {
            s = new Scanner(ClassLoader.getSystemResourceAsStream("bible/" + translation + "/" + book.replace(" ", "_") + ".txt"), ENCODING);
        } catch (NullPointerException e) {
            throw new BibleException(String.format("Boek %s in vertaling %s niet gevonden", book, translation));
        }

        while (s.hasNextLine()) {

            String line = s.nextLine();

            boolean buildingTitle = false;

            if (line.matches(String.format("#%s", chapter))) {
                // we have the line number on which the book starts
                // continue reading from that line, char by char, until we end up on
                // the right verse
                while (s.hasNextLine()) {
                    line = s.nextLine();

                    // check to see if we are reading the next chapter, if so, return the BiblePartFragment List
                    if (line.startsWith(String.format("#%s", chapter + 1))) {
                        s.close();
                        return clean(fragmentList);
                    }

                    // title encountered, print italic
                    if (line.startsWith("=")) {
                        buildingTitle = true;
                        // start line reading after equals sign
                        line = line.substring(1);
                    } else {
                        buildingTitle = false;
                    }

                    StringBuilder sb = new StringBuilder();
                    CharType prevCharType = null;

                    // all lines start with a verse number in the format: [0-9]+[-]?[0-9]+
                    // determine the versenumber at the start of the line and continue reading character by character
                    for (Character c : line.toCharArray()) {

                        // handle first char and determine displayType
                        if (prevCharType == null) {
                            sb.append(Character.toUpperCase(c));
                            prevCharType = getCharType(c);
                            continue;
                        }

                        // reading a multidigit number
                        if (prevCharType == CharType.number && getCharType(c) == CharType.number) {
                            sb.append(c);
                            continue;
                        }

                        // reading a multidigit number
                        if (prevCharType == CharType.number && getCharType(c) == CharType.dash) {
                            // TODO handle multi verse indicator
                            sb.append(c);
                            continue;
                        }

                        // reading a string
                        if (prevCharType == CharType.character && getCharType(c) == CharType.character) {
                            sb.append(c);
                            continue;
                        }

                        // end of a verse
                        if (prevCharType == CharType.character && getCharType(c) == CharType.number && !buildingTitle) {
                            if (addVerse) {
                                fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, sb.toString()));
                            }
                            prevCharType = getCharType(c);
                            sb = new StringBuilder();
                            sb.append(c);
                            continue;
                        }

                        // end of a verse number
                        if (prevCharType == CharType.number && getCharType(c) == CharType.character) {

                            if (!String.valueOf(c).equals(" ")) {
                                // determine verse number to see if we should start or stop adding stuff to the fragment list
                                try {
                                    if (Integer.parseInt(sb.toString()) >= fromVerse && Integer.parseInt(sb.toString()) <= toVerse) {
                                        fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.superScript, sb.toString()));
                                        addVerse = true;
                                    }
                                    if (Integer.parseInt(sb.toString()) > toVerse) {
                                        s.close();
                                        return clean(fragmentList);
                                    }
                                } catch (NumberFormatException e) {
                                    // TODO verse number probably contains a dash, handle it!
                                }
                                prevCharType = getCharType(c);
                                sb = new StringBuilder();
                            } else {
                                // the previous 'verse number' appeared to be a textual number. Rebuild the StringBuilder with the last added verse text, remove it from the
                                // fragment list and create a new stringbuilder from it.
                                // 'nothing to see here people, move on!'
                                StringBuilder tmpSb = new StringBuilder();
                                if (fragmentList.size() > 0) {
                                    tmpSb.append(fragmentList.get(fragmentList.size() - 1).getContent());
                                    tmpSb.append(sb);
                                    sb = tmpSb;
                                    fragmentList.remove(fragmentList.size() - 1);
                                }
                                prevCharType = CharType.character;
                            }
                            sb.append(c);
                            continue;
                        }
                    }

                    // new line encountered
                    if (addVerse) {
                        if (prevCharType == CharType.character && !buildingTitle) {
                            fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.normal, sb.toString()));
                            fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.line_end, LINE_END));
                        } else if (!buildingTitle) {
                            fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.superScript, sb.toString()));
                        } else {
                            fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.italic, sb.toString()));
                            fragmentList.add(new BiblePartFragment(BiblePartFragment.DisplayType.line_end, LINE_END));
                        }
                    }

                }
            }
        }

        s.close();

        return clean(fragmentList);
    }

    // strip all last entries if they are italic or line ends.
    private static List<BiblePartFragment> clean(List<BiblePartFragment> fragmentList) {

        while (fragmentList.get(fragmentList.size() - 1).getDisplayType() == BiblePartFragment.DisplayType.line_end
                || fragmentList.get(fragmentList.size() - 1).getDisplayType() == BiblePartFragment.DisplayType.italic) {
            fragmentList.remove(fragmentList.size() - 1);
            clean(fragmentList);
        }
        return fragmentList;
    }

    public static String getTranslationFromLine(String line) throws BibleException {
        if (line.trim().matches(".*\\([a-zA-Z7]{1,4}\\)$")) {
            if (line.toLowerCase().trim().endsWith("(nbv)")) {
                return "NBV";
            } else if (line.toLowerCase().trim().endsWith("(bgt)")) {
                return "BGT";
            } else if (line.toLowerCase().trim().endsWith("(nbg)")) {
                return "NBG51";
            } else if (line.toLowerCase().trim().endsWith("(sv77)")) {
                return "SV77";
            } else {
                throw new BibleException("Onbekende vertaling in regel: " + line);
            }
        }
        return DEFAULT_TRANSLATION;
    }

    // for each bible book an if statement
    public static String getBibleBookFromLine(String line) throws BibleException {

        line = line.trim();

        if (line.toLowerCase().startsWith("gen")) {
            return "Genesis";
        }
        if (line.toLowerCase().startsWith("exod")) {
            return "Exodus";
        }
        if (line.toLowerCase().startsWith("levi")) {
            return "Leviticus";
        }
        if (line.toLowerCase().startsWith("nume")) {
            return "Numeri";
        }
        if (line.toLowerCase().startsWith("deut")) {
            return "Deuteronomium";
        }
        if (line.toLowerCase().startsWith("jozu")) {
            return "Jozua";
        }
        if (line.toLowerCase().startsWith("rech")) {
            return "Rechters";
        }
        if (line.toLowerCase().startsWith("ruth")) {
            return "Ruth";
        }
        if (line.toLowerCase().matches("^1 ?sam.*")) {
            return "1 Samuël";
        }
        if (line.toLowerCase().matches("^2 ?sam.*")) {
            return "2 Samuël";
        }
        if (line.toLowerCase().matches("^1 ?kon.*")) {
            return "1 Koningen";
        }
        if (line.toLowerCase().matches("^2 ?kon.*")) {
            return "2 Koningen";
        }
        if (line.toLowerCase().matches("^1 ?kro.*")) {
            return "1 Kronieken";
        }
        if (line.toLowerCase().matches("^2 ?kro.*")) {
            return "2 Kronieken";
        }
        if (line.toLowerCase().startsWith("ezra")) {
            return "Ezra";
        }
        if (line.toLowerCase().startsWith("nehe")) {
            return "Nehemia";
        }
        if (line.toLowerCase().startsWith("este")) {
            return "Ester";
        }
        if (line.toLowerCase().startsWith("job")) {
            return "Job";
        }
        if (line.toLowerCase().startsWith("psal")) {
            return "Psalmen";
        }
        if (line.toLowerCase().startsWith("spre")) {
            return "Spreuken";
        }
        if (line.toLowerCase().startsWith("pred")) {
            return "Prediker";
        }
        if (line.toLowerCase().startsWith("hoog")) {
            return "Hooglied";
        }
        if (line.toLowerCase().startsWith("jesa")) {
            return "Jesaja";
        }
        if (line.toLowerCase().startsWith("jere")) {
            return "Jeremia";
        }
        if (line.toLowerCase().startsWith("klaa")) {
            return "Klaagliederen";
        }
        if (line.toLowerCase().startsWith("ezec")) {
            return "Ezechiël";
        }
        if (line.toLowerCase().startsWith("dani")) {
            return "Daniël";
        }
        if (line.toLowerCase().startsWith("hose")) {
            return "Hosea";
        }
        if (line.toLowerCase().startsWith("joel")) {
            return "Joël";
        }
        if (line.toLowerCase().startsWith("amos")) {
            return "Amos";
        }
        if (line.toLowerCase().startsWith("obad")) {
            return "Obadja";
        }
        if (line.toLowerCase().startsWith("jona")) {
            return "Jona";
        }
        if (line.toLowerCase().startsWith("mich")) {
            return "Micha";
        }
        if (line.toLowerCase().startsWith("nahu")) {
            return "Nahum";
        }
        if (line.toLowerCase().startsWith("haba")) {
            return "Habakuk";
        }
        if (line.toLowerCase().startsWith("sefa")) {
            return "Sefanja";
        }
        if (line.toLowerCase().startsWith("hagg")) {
            return "Haggai";
        }
        if (line.toLowerCase().startsWith("zach")) {
            return "Zacharia";
        }
        if (line.toLowerCase().startsWith("male")) {
            return "Maleachi";
        }
        if (line.toLowerCase().matches("matt?h?e.*")) {
            return "Matteüs";
        }
        if (line.toLowerCase().startsWith("marcu")) {
            return "Marcus";
        }
        if (line.toLowerCase().startsWith("lucas")) {
            return "Lucas";
        }
        if (line.toLowerCase().startsWith("johan")) {
            return "Johannes";
        }
        if (line.toLowerCase().startsWith("hande")) {
            return "Handelingen";
        }
        if (line.toLowerCase().startsWith("romei")) {
            return "Romeinen";
        }
        if (line.toLowerCase().matches("^1 ?kor.*")) {
            return "1 Korintiërs";
        }
        if (line.toLowerCase().matches("^2 ?kor.*")) {
            return "2 Korintiërs";
        }
        if (line.toLowerCase().startsWith("galat")) {
            return "Galaten";
        }
        if (line.toLowerCase().startsWith("efezi")) {
            return "Efeziërs";
        }
        if (line.toLowerCase().startsWith("filip")) {
            return "Filippenzen";
        }
        if (line.toLowerCase().startsWith("kolos")) {
            return "Kolossenzen";
        }
        if (line.toLowerCase().matches("^1 ?tes.*")) {
            return "1 Tessalonicenzen";
        }
        if (line.toLowerCase().matches("^2 ?tes.*")) {
            return "2 Tessalonicenzen";
        }
        if (line.toLowerCase().matches("^1 ?tim.*")) {
            return "1 Timoteüs";
        }
        if (line.toLowerCase().matches("^2 ?tim.*")) {
            return "2 Timoteüs";
        }
        if (line.toLowerCase().startsWith("titus")) {
            return "Titus";
        }
        if (line.toLowerCase().startsWith("filem")) {
            return "Filemon";
        }
        if (line.toLowerCase().startsWith("hebre")) {
            return "Hebreeën";
        }
        if (line.toLowerCase().startsWith("jakob")) {
            return "Jakobus";
        }
        if (line.toLowerCase().matches("^1 ?pet.*")) {
            return "1 Petrus";
        }
        if (line.toLowerCase().matches("^2 ?pet.*")) {
            return "2 Petrus";
        }
        if (line.toLowerCase().matches("^1 ?joh.*")) {
            return "1 Johannes";
        }
        if (line.toLowerCase().matches("^2 ?joh.*")) {
            return "2 Johannes";
        }
        if (line.toLowerCase().matches("^3 ?joh.*")) {
            return "3 Johannes";
        }
        if (line.toLowerCase().startsWith("judas")) {
            return "Judas";
        }
        if (line.toLowerCase().startsWith("openb")) {
            return "Openbaring";
        }
        throw new BibleException("Bijbelboek niet gevonden in liturgieregel: " + line);
    }

    public static int getChapterFromLine(String line) throws BibleException {

        try {
//            line = LiturgyModel.format(line, LiturgyItem.Type.scripture);

            if (line.contains(":")) {
                String s = StringUtils.substringBefore(line, ":");
                return Integer.parseInt(StringUtils.substringBefore(StringUtils.substringAfterLast(s, " "), ":").trim());
            } else {
                if (line.matches("^[0-9]{1} .*")) {
                    line = StringUtils.substringAfter(line, " ");
                }
                if (line.contains("(")) {
                    return Integer.parseInt(StringUtils.substringBetween(line, " ", "(").trim());
                } else {
                    return Integer.parseInt(StringUtils.substringAfter(line, " ").trim());
                }
            }
        } catch (Exception e) {
            throw new BibleException("Het hoofdstuk kon niet worden bepaald in liturgieregel: " + line, e);
        }
    }

    public static int getStartVerseFromLine(String line) {
        if (line.contains(":")) {
            if (line.contains("-")) {
                return Integer.parseInt(StringUtils.substringBetween(line, ":", "-").trim());
            } else {
                if (line.contains("(")) {
                    return Integer.parseInt(StringUtils.substringBetween(line, ":", "(").trim());
                } else {
                    return Integer.parseInt(StringUtils.substringAfterLast(line, ":").trim());
                }
            }
        } else {
            return 0;
        }
    }

    public static int getEndVerseFromLine(String line) {
        if (line.contains(":")) {
            if (line.contains("-")) {
                if (line.contains("(")) {
                    return Integer.parseInt(StringUtils.substringBetween(line, "-", "(").trim());
                } else {
                    return Integer.parseInt(StringUtils.substringAfterLast(line, "-").trim());
                }
            } else {
                if (line.contains("(")) {
                    return Integer.parseInt(StringUtils.substringBetween(line, ":", "(").trim());
                } else {
                    return Integer.parseInt(StringUtils.substringAfterLast(line, ":").trim());
                }
            }
        } else {
            return 999;
        }
    }

    private static CharType getCharType(char c) {
        try {
            Integer.parseInt(String.valueOf(c));
            return CharType.number;
        } catch (NumberFormatException e) {
            // do nothing
        }
        //
        // if (":".equals(String.valueOf(c))) {
        // return CharType.colon;
        // }

        return CharType.character;
    }

}
