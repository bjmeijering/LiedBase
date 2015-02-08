package org.gkvassenpeelo.liedbase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.gkvassenpeelo.liedbase.bible.Bible;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;
import org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService;
import org.gkvassenpeelo.liedbase.liturgy.Gathering;
import org.gkvassenpeelo.liedbase.liturgy.Liturgy;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.Scripture;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.liedbase.liturgy.Welcome;
import org.gkvassenpeelo.liedbase.papermachine.PaperMachine;
import org.gkvassenpeelo.liedbase.slidemachine.SlideMachine;
import org.gkvassenpeelo.liedbase.songbook.SongBook;
import org.gkvassenpeelo.liedbase.songbook.SongLine;

/**
 * TODO: vers nummers correct tonen als geen verzen zijn opgegeven bij een bijbeltekst TODO: Agenda table is verdwenen
 * 
 * 
 * @author hdo20043
 *
 */
public class LiedBase {

	static final Logger logger = Logger.getLogger(LiedBase.class);

	// re-used
	private static final String regex_psalm = "([pP]salm )";
	private static final String regex_gezang = "([gG]ezang(en)?)";
	private static final String regex_lied = "([lL]ied([bB]oek)?)";
	private static final String regex_opwekking = "([oO]pwekking?)";
	private static final String regex_voorganger = "([vV]oorganger|[dD]ominee|[wW]el[ck]om)";

	private Liturgy liturgy = new Liturgy();

	private File sourceFile = new File("liturgie.txt");

	public static final String ENCODING = "UTF-8";

	private List<String> liturgyView = new ArrayList<String>();
	private List<LiturgyPart.Type> litugyOverViewItems = new ArrayList<LiturgyPart.Type>();

	private enum CharType {
		number, character, dash, colon, comma
	}

	public LiedBase() {
		logger.info("LiedBase gestart");

		// all following liturgy part types will appear on liturgy overview
		// slides
		litugyOverViewItems.add(LiturgyPart.Type.scripture);
		litugyOverViewItems.add(LiturgyPart.Type.song);
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
		String regex = String.format("^[ ]*(%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s).*", regex_agenda, regex_end_of_morning_service, regex_end_of_afternoon_service, regex_amen,
				regex_votum, regex_psalm, regex_gezang, regex_lied, regex_opwekking, regex_gebed, regex_collecte, regex_voorganger, regex_law, regex_lecture);

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
			if (!line.startsWith("#") && !StringUtils.isEmpty(line.trim())) {
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
		if (type == LiturgyPart.Type.song || type == LiturgyPart.Type.scripture) {
			line = format(line, LiturgyPart.Type.song);
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

			if (scType == SlideContents.Type.opwekking) {

				for (List<SongLine> verse : SongBook.getOpwekkingSongTekst(SongBook.getSongNumber(line))) {
					Song song = new Song(line, verse);
					lp.addSlide(song);
				}

			} else {

				if (!line.contains(":")) {
					List<String> allVerses = SongBook.getVersesFromSong(scType, SongBook.getSongNumber(line));
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
					Song song = new Song(line, SongBook.getSongText(scType, SongBook.getSongNumber(line), currentVerse));
					song.setVerseNumber(currentVerse);
					lp.addSlide(song);
				}

			}

		} else if (type == LiturgyPart.Type.prair) {
			// nothing to do
		} else if (type == LiturgyPart.Type.gathering) {
			lp.addSlide(new Gathering(getGatheringBenificiaries(line)));

			line = "Collecte";
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

			line = format(line, LiturgyPart.Type.scripture);

			lp.addSlide(new Scripture(biblePart, bibleBook, chapter, fromVerse, toVerse));
		}

		lp.setLine(line);

		liturgy.addLiturgyPart(lp);

		if (litugyOverViewItems.contains(type)) {
			liturgyView.add(line);
		}

		return line;

	}

	public static String format(String line, LiturgyPart.Type type) {

		StringBuilder sb = new StringBuilder();

		// replace incorrect characters
		line = replaceForbiddenChars(line);

		CharType prevCharType = null;

		for (Character c : line.toCharArray()) {

			// handle first round
			if (prevCharType == null) {
				sb.append(Character.toUpperCase(c));
				prevCharType = getCharType(c);
				continue;
			}

			if (prevCharType == CharType.number && getCharType(c) == CharType.number) {
				sb.append(c);
				continue;
			}

			if (prevCharType == CharType.character && getCharType(c) == CharType.character) {
				sb.append(c);
				continue;
			}

			if (prevCharType == CharType.character && getCharType(c) == CharType.number) {
				sb.append(" ");
				sb.append(c);
				prevCharType = getCharType(c);
				continue;
			}

			if (prevCharType == CharType.number && getCharType(c) == CharType.character && type == LiturgyPart.Type.scripture) {
				sb.append(" ");
				if (line.indexOf(c) < 3) {
					sb.append(Character.toUpperCase(c));
				} else {
					sb.append(c);

				}
				prevCharType = getCharType(c);
				continue;
			}

			if (getCharType(c) == CharType.colon) {
				sb.append(c);
				sb.append(" ");
				prevCharType = getCharType(c);
				continue;
			}

			if (getCharType(c) == CharType.comma) {
				sb.append(c);
				sb.append(" ");
				prevCharType = getCharType(c);
				continue;
			}

			sb.append(c);

		}

		return sb.toString();
	}

	private static String replaceForbiddenChars(String line) {

		// start by stripping all spaces
		line = line.replaceAll(" ", "");

		// strip weird dash
		line = line.replace("–", "-");

		return line;
	}

	private static CharType getCharType(char c) {
		try {
			Integer.parseInt(String.valueOf(c));
			return CharType.number;
		} catch (NumberFormatException e) {
			// do nothing
		}

		if ("-".equals(String.valueOf(c))) {
			return CharType.dash;
		}

		if (",".equals(String.valueOf(c))) {
			return CharType.comma;
		}

		if (":".equals(String.valueOf(c))) {
			return CharType.colon;
		}

		return CharType.character;
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

	public Liturgy getLiturgy() {
		return liturgy;
	}

	public List<String> getLiturgyView() {
		return liturgyView;
	}

	public static void main(String[] args) throws Docx4JException, BibleException {

		try {
			LiedBase lb = new LiedBase();
			lb.parseLiturgyScript();

			SlideMachine slideMachine = new SlideMachine(lb.getLiturgy(), lb.getLiturgyView());
			slideMachine.createSlides();
			slideMachine.save();

			PaperMachine pm = new PaperMachine(lb.getLiturgy());
			pm.createDocument();
			pm.save("D:/Projects/LiedBase/target/HelloWord.docx");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
