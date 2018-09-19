package org.gkvassenpeelo.liedbase.liturgy;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.impl.Log4JLogger;
import org.gkvassenpeelo.liedbase.LiedBaseError;

public class Parser {

	private Log4JLogger logger = new Log4JLogger(this.getClass().toString());

	private static String DEFAULT_TRANSLATION = "BGT";

	private String liturgy;

	// re-used
	private static final String regex_psalm = "([pP]salm )";
	private static final String regex_gezang = "([gG]ezang(en)?)";
	private static final String regex_lied = "([lL]ied([bB]oek)?)";
	private static final String regex_opwekking = "([oO]pwekking?)";
	private static final String regex_levenslied = "([lL]evenslied?)";
	private static final String regex_voorganger = "([vV]oorganger|[dD]ominee|[wW]el[ck]om)";

	public static final String ENCODING = "UTF-8";

	private enum CharType {
		number, character, dash, colon, comma
	}

	public void setText(String liturgy) {
		this.liturgy = liturgy;
	}

	/**
	 * Use this method to get from natural text to standardized LiturgyPartType
	 * 
	 * @param line
	 * @return
	 * @throws LiedBaseError
	 */
	private LiturgyItem.Type getLiturgyPartTypeFromLine(String line) {

		String regex_video = "([Vv]ideo)";
		String regex_schoonmaak = "([Ss]choonmaak)";
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
		String regex = String.format("^[ ]*(%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s).*", regex_schoonmaak, regex_extended_scripture,
				regex_empty_with_logo, regex_agenda, regex_end_of_morning_service, regex_end_of_afternoon_service, regex_amen, regex_votum, regex_psalm,
				regex_gezang, regex_lied, regex_opwekking, regex_levenslied, regex_gebed, regex_collecte, regex_voorganger, regex_law, regex_lecture);

		// check liturgy part type
		Pattern liturgyPattern = Pattern.compile(regex);
		java.util.regex.Matcher m = liturgyPattern.matcher(line);

		if (!line.matches(regex)) {
			return LiturgyItem.Type.scripture;
		}

		m.find();

		if (m.group(1).matches(regex_psalm)) {
			return LiturgyItem.Type.song;
		} else if (m.group(1).matches(regex_video)) {
			return LiturgyItem.Type.video;
		} else if (m.group(1).matches(regex_schoonmaak)) {
			return LiturgyItem.Type.schoonmaak;
		} else if (m.group(1).matches(regex_gezang)) {
			return LiturgyItem.Type.song;
		} else if (m.group(1).matches(regex_lied)) {
			return LiturgyItem.Type.song;
		} else if (m.group(1).matches(regex_opwekking)) {
			return LiturgyItem.Type.song;
		} else if (m.group(1).matches(regex_levenslied)) {
			return LiturgyItem.Type.song;
		} else if (m.group(1).matches(regex_gebed)) {
			return LiturgyItem.Type.prair;
		} else if (m.group(1).matches(regex_collecte)) {
			return LiturgyItem.Type.gathering;
		} else if (m.group(1).matches(regex_agenda)) {
			return LiturgyItem.Type.agenda;
		} else if (m.group(1).matches(regex_voorganger)) {
			return LiturgyItem.Type.welcome;
		} else if (m.group(1).matches(regex_law)) {
			return LiturgyItem.Type.law;
		} else if (m.group(1).matches(regex_lecture)) {
			return LiturgyItem.Type.lecture;
		} else if (m.group(1).matches(regex_votum)) {
			return LiturgyItem.Type.votum;
		} else if (m.group(1).matches(regex_amen)) {
			return LiturgyItem.Type.amen;
		} else if (m.group(1).matches(regex_end_of_morning_service)) {
			return LiturgyItem.Type.endOfMorningService;
		} else if (m.group(1).matches(regex_end_of_afternoon_service)) {
			return LiturgyItem.Type.endOfAfternoonService;
		} else if (m.group(1).matches(regex_extended_scripture)) {
			return LiturgyItem.Type.extendedScripture;
		} else if (m.group(1).matches(regex_empty_with_logo)) {
			return LiturgyItem.Type.emptyWithLogo;
		}

		return null;
	}

	/**
	 * 
	 * @param inputString
	 * @throws ParseException
	 */
	public LiturgyParseResult parseLiturgyScript() throws ParseException {

		LiturgyParseResult parseResult = new LiturgyParseResult();

		if (StringUtils.isEmpty(liturgy)) {
			parseResult.addError("Liturgie is leeg, niets te doen...");
		}

		StringTokenizer st = new StringTokenizer(liturgy, System.getProperty("line.separator"));

		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			logger.info("=====================");
			logger.info("Start verwerken regel: " + line);
			if (!line.trim().startsWith("#") && !StringUtils.isEmpty(line.trim())) {
				// first: get the general type, then use that to decide what extractions are needed next
				LiturgyItem.Type type = getLiturgyPartTypeFromLine(line);
				logger.info("Type: " + type.toString());

				String translation = null;
				String book = null;
				int chapter = -1;
				int[] verses = null;
				VerseRange verseRange = null;
				// only if the the type requires further lookup (like a song or bible text) extract more details
				if (type == LiturgyItem.Type.song || type == LiturgyItem.Type.scripture) {
					translation = getTranslationFromLine(line);
					book = getBookFromLine(line);
					chapter = getChapterFromLine(line);
					verses = getVersesFromLine(line);
					if (type == LiturgyItem.Type.scripture) {
						verseRange = getVerseRangeFromLine(line);
					}
				}
				parseResult.addLiturgyItem(new LiturgyItem(line, type, translation, book, chapter, verses, verseRange));
			}
		}

		return parseResult;

	}

	public static String getTranslationFromLine(String line) throws ParseException {
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
				throw new ParseException("Onbekende vertaling in regel: " + line, 0);
			}
		}
		return DEFAULT_TRANSLATION;
	}

	VerseRange getVerseRangeFromLine(String line) {

		int startVerse = 0;
		int endVerse = 999;

		if (line.contains("-")) {
			startVerse = Integer.parseInt(StringUtils.substringBetween(line, ":", "-").trim());
		} else {
			if (line.contains("(")) {
				startVerse = Integer.parseInt(StringUtils.substringBetween(line, ":", "(").trim());
			} else {
				startVerse = Integer.parseInt(StringUtils.substringAfterLast(line, ":").trim());
			}
		}

		if (line.contains("-")) {
			if (line.contains("(")) {
				endVerse = Integer.parseInt(StringUtils.substringBetween(line, "-", "(").trim());
			} else {
				endVerse = Integer.parseInt(StringUtils.substringAfterLast(line, "-").trim());
			}
		} else {
			if (line.contains("(")) {
				endVerse = Integer.parseInt(StringUtils.substringBetween(line, ":", "(").trim());
			} else {
				endVerse = Integer.parseInt(StringUtils.substringAfterLast(line, ":").trim());
			}
		}

		return new VerseRange(startVerse, endVerse);

	}

	int[] getVersesFromLine(String line) {
		if (line.contains(":")) {
			int startPos = line.indexOf(':') + 1;
			int endPos = line.indexOf("(") == -1 ? line.length() : line.indexOf("(");
			String[] verses = line.substring(startPos, endPos).split(",");
			int[] intVerses = new int[verses.length];
			for (int i = 0; i < verses.length; i++) {
				intVerses[i] = Integer.parseInt(verses[i].trim());
			}
			return intVerses;
		}
		return null;
	}

	// get the chapter. i.e. the word after the first space and before an optional :
	int getChapterFromLine(String line) {
		Pattern p = Pattern.compile("^[\\d]?[ ]?[a-zA-Z0-9ëü]*[ ]+([0-9a-z]+)");
		Matcher m = p.matcher(line);
		int chapter = -1;
		if (m.find()) {
			chapter = Integer.parseInt(m.group(1).trim());
			logger.info("Hoofdstuk: " + chapter);
		} else {
			logger.info("Geen hoofdstuk gevond in regel: " + line);
		}
		return chapter;
	}

	// get the book. i.e. the part before the first space
	private String getBookFromLine(String line) {
		Pattern p = Pattern.compile("^([123 ]{0,2}[a-zA-Z0-9ëü]*)[ ]*.*$");
		Matcher m = p.matcher(line);
		m.find();
		logger.info("Boek: " + m.group(1).trim());
		return m.group(1).trim();
	}

	// /**
	// *
	// * @param line
	// * @throws BibleException
	// * @throws SongBookException
	// */
	// private String parseLiturgyScriptLine(String line) throws BibleException, SongBookException {
	//
	// LiturgyItem.Type type = null;
	//
	// try {
	//
	// type = getLiturgyPartTypeFromLine(line);
	//
	// } catch (LiedBaseError e) {
	// logger.warn(e.getMessage());
	// return line;
	// }
	//
	// // apply the right formatting to 'line'
	// if (type == LiturgyItem.Type.song || type == LiturgyItem.Type.scripture) {
	// line = format(line, LiturgyItem.Type.song);
	// }
	//
	// // create a new liturgy part
	// LiturgyItem lp = new LiturgyItem(type);
	//
	// if (type == LiturgyItem.Type.song) {
	//
	// SlideContents.Type scType = null;
	//
	// // determine songtype
	// String regex = String.format("^[ ]*(%s|%s|%s|%s|%s).*", regex_psalm, regex_gezang, regex_lied, regex_levenslied, regex_opwekking);
	// Pattern songPattern = Pattern.compile(regex);
	// java.util.regex.Matcher m = songPattern.matcher(line);
	//
	// m.find();
	// if (m.group(1).matches(regex_psalm)) {
	// scType = SlideContents.Type.psalm;
	// } else if (m.group(1).matches(regex_gezang)) {
	// scType = SlideContents.Type.gezang;
	// } else if (m.group(1).matches(regex_lied)) {
	// scType = SlideContents.Type.lied;
	// } else if (m.group(1).matches(regex_levenslied)) {
	// scType = SlideContents.Type.levenslied;
	// } else if (m.group(1).matches(regex_opwekking)) {
	// scType = SlideContents.Type.opwekking;
	// }
	//
	// if (scType == SlideContents.Type.opwekking) {
	//
	// for (List<SongLine> verse : SongBook.getOpwekkingSongTekst(SongBook.getSongNumber(line))) {
	// Song song = new Song(line, verse);
	// lp.addSlide(song);
	// }
	//
	// } else {
	//
	// if (!line.contains(":")) {
	// List<String> allVerses = SongBook.getVersesFromSong(scType, SongBook.getSongNumber(line));
	// String displayLine = String.format("%s%s", line, ": ");
	// for (String verse : allVerses) {
	// displayLine += verse + ", ";
	// }
	// line = StringUtils.substringBeforeLast(displayLine, ",");
	// }
	// // for each verse, create a songSlideContents and add it to the
	// // liturgyPart
	// StringTokenizer st = new StringTokenizer(StringUtils.substringAfter(line, ":"), ",");
	// while (st.hasMoreTokens()) {
	// String currentVerse = st.nextToken().trim();
	// List<SongLine> songText = SongBook.getSongText(scType, SongBook.getSongNumber(line), currentVerse);
	//
	// if (songText == null) {
	// throw new SongBookException(String.format("Vers %s van %s %s niet gevonden", currentVerse, scType, SongBook.getSongNumber(line)));
	// }
	//
	// Song song = new Song(line, songText);
	// song.setVerseNumber(currentVerse);
	// lp.addSlide(song);
	// }
	//
	// }
	//
	// } else if (type == LiturgyItem.Type.prair) {
	// // nothing to do
	// } else if (type == LiturgyItem.Type.extendedScripture) {
	// // nothing to do
	// } else if (type == LiturgyItem.Type.emptyWithLogo) {
	// // nothing to do
	// } else if (type == LiturgyItem.Type.gathering) {
	// lp.addSlide(new Gathering(getGatheringBenificiaries(line)));
	// line = "Collecte";
	// } else if (type == LiturgyItem.Type.welcome) {
	// lp.addSlide(new Welcome(getVicarName(line)));
	// } else if (type == LiturgyItem.Type.endOfMorningService) {
	// EndOfMorningService ems = new EndOfMorningService();
	// ems.setTime(getTimeFromLine(line));
	// ems.setVicarName(getNextVicarFromLine(line));
	// lp.addSlide(ems);
	// } else if (type == LiturgyItem.Type.scripture) {
	// String bibleBook = Bible.getBibleBookFromLine(line);
	// int chapter = Bible.getChapterFromLine(line);
	// int fromVerse = Bible.getStartVerseFromLine(line);
	// int toVerse = Bible.getEndVerseFromLine(line);
	// String translation = Bible.getTranslationFromLine(line);
	//
	// List<BiblePartFragment> biblePart = Bible.getBiblePartFromText(translation, bibleBook, chapter, fromVerse, toVerse);
	//
	// line = Bible.formatLine(bibleBook, translation, chapter, fromVerse, toVerse);
	//
	// lp.addSlide(new Scripture(biblePart, bibleBook, translation, chapter, fromVerse, toVerse));
	// lp.addSlide(new Scripture(null, bibleBook, translation, chapter, fromVerse, toVerse));
	//
	// }
	//
	// lp.setLine(line);
	//
	// liturgy.addLiturgyPart(lp);
	//
	// if (litugyOverViewItems.contains(type)) {
	// liturgyView.add(line);
	// }
	//
	// return line;
	//
	// }

	public static String format(String line, LiturgyItem.Type type) {

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

			if (prevCharType == CharType.number && getCharType(c) == CharType.character && type == LiturgyItem.Type.scripture) {
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
		line = line.replace("ï¿½", "-");

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

}
