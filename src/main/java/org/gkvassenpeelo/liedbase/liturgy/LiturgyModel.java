package org.gkvassenpeelo.liedbase.liturgy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.LiedBaseError;
import org.gkvassenpeelo.liedbase.bible.Bible;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;
import org.gkvassenpeelo.liedbase.songbook.SongBook;
import org.gkvassenpeelo.liedbase.songbook.SongBookException;
import org.gkvassenpeelo.liedbase.songbook.SongLine;

public class LiturgyModel {

	private Logger logger;

	// re-used
	private static final String regex_psalm = "([pP]salm )";
	private static final String regex_gezang = "([gG]ezang(en)?)";
	private static final String regex_lied = "([lL]ied([bB]oek)?)";
	private static final String regex_opwekking = "([oO]pwekking?)";
	private static final String regex_voorganger = "([vV]oorganger|[dD]ominee|[wW]el[ck]om)";

	private Liturgy liturgy;

	public static final String ENCODING = "UTF-8";

	private List<String> liturgyView;
	private List<LiturgyPart.Type> litugyOverViewItems = new ArrayList<LiturgyPart.Type>();

	private enum CharType {
		number, character, dash, colon, comma
	}

	public LiturgyModel(Logger logger) {
		this.logger = logger;

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

		String regex_extended_scripture = "bijbeltekst vervolg";
		String regex_empty_with_logo = "leeg met logo";
		String regex_end_of_morning_service = "(([eE]inde)?.*[mM]orgendienst)";
		String regex_end_of_afternoon_service = "(([eE]inde)?.*[mM]iddagdienst)";
		String regex_amen = "(([gG]ezongen)?.*[aA]men)";
		String regex_votum = "([vV]otum)";
		String regex_gebed = "(([gG]ebed)|([bB]idden)|([dD]anken))";
		String regex_collecte = "(([cC]|[kK])olle[ck]te)";
		String regex_law = "([wW]et)";
		String regex_lecture = "([pP]reek)";
		String regex_agenda = "([aA]genda)";
		String regex = String.format("^[ ]*(%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s).*", regex_extended_scripture, regex_empty_with_logo, regex_agenda,
				regex_end_of_morning_service, regex_end_of_afternoon_service, regex_amen, regex_votum, regex_psalm, regex_gezang, regex_lied, regex_opwekking, regex_gebed,
				regex_collecte, regex_voorganger, regex_law, regex_lecture);

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
		} else if (m.group(1).matches(regex_extended_scripture)) {
			return LiturgyPart.Type.extendedScripture;
		} else if (m.group(1).matches(regex_empty_with_logo)) {
			return LiturgyPart.Type.emptyWithLogo;
		}

		return null;
	}

	/**
	 * 
	 * @param inputString
	 * @throws BibleException
	 * @throws SongBookException
	 * @throws IOException
	 */
	public LiturgyParseResult parseLiturgyScript(String liturgyText) throws BibleException, SongBookException {

		LiturgyParseResult parseResult = new LiturgyParseResult();
		liturgy = new Liturgy();
		liturgyView = new ArrayList<String>();

		if (StringUtils.isEmpty(liturgyText)) {
			parseResult.addError("Liturgie is leeg, niets te doen...");
		}

		StringTokenizer st = new StringTokenizer(liturgyText, System.getProperty("line.separator"));

		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (!line.trim().startsWith("#") && !StringUtils.isEmpty(line.trim())) {
				parseLiturgyScriptLine(line);
			}
		}

		return parseResult;

	}

	/**
	 * 
	 * @param line
	 * @throws BibleException
	 * @throws SongBookException
	 */
	private String parseLiturgyScriptLine(String line) throws BibleException, SongBookException {

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
					List<SongLine> songText = SongBook.getSongText(scType, SongBook.getSongNumber(line), currentVerse);

					if (songText == null) {
						throw new SongBookException(String.format("Vers %s van %s %s niet gevonden", currentVerse, scType, SongBook.getSongNumber(line)));
					}

					Song song = new Song(line, songText);
					song.setVerseNumber(currentVerse);
					lp.addSlide(song);
				}

			}

		} else if (type == LiturgyPart.Type.prair) {
			// nothing to do
		} else if (type == LiturgyPart.Type.extendedScripture) {
			// nothing to do
		} else if (type == LiturgyPart.Type.emptyWithLogo) {
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

			List<BiblePartFragment> biblePart = Bible.getBiblePartFromText(translation, Bible.getBibleBookFromLine(line), chapter, fromVerse, toVerse);

			line = format(line, LiturgyPart.Type.scripture);

			lp.addSlide(new Scripture(biblePart, bibleBook, translation, chapter, fromVerse, toVerse));
			lp.addSlide(new Scripture(null, bibleBook, translation, chapter, fromVerse, toVerse));
			
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

		boolean justCopy = false;

		CharType prevCharType = null;

		for (Character c : line.toCharArray()) {

			if (justCopy) {
				sb.append(c);
				if (String.valueOf(c).equals(")")) {
					justCopy = false;
				}
				continue;
			}

			// handle first round
			if (prevCharType == null) {
				sb.append(Character.toUpperCase(c));
				prevCharType = getCharType(c);
				continue;
			}

			// handle braces
			if (String.valueOf(c).equals("(")) {
				sb.append(" ");
				justCopy = true;
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
		
		// URLencode ampersand
		line = line.replace("&", "&amp;");

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

	public String getTimeFromLine(String line) {

		String data = StringUtils.substringAfter(line, ":");

		return StringUtils.substringBefore(data, ",").trim();
	}

	public String getNextVicarFromLine(String line) {

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

}
