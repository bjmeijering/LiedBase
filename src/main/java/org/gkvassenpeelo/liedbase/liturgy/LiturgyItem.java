package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.gkvassenpeelo.liedbase.bible.Bible;
import org.gkvassenpeelo.liedbase.bible.BibleException;
import org.gkvassenpeelo.liedbase.songbook.SongBook;
import org.gkvassenpeelo.liedbase.songbook.SongBookException;
import org.gkvassenpeelo.liedbase.songbook.SongLine;

public class LiturgyItem {

	public enum Type {
		welcome, blank, schoonmaak, song, gathering, prair, law, lecture, votum, amen, endOfMorningService, endOfAfternoonService, scripture, agenda, liturgyOverview, extendedScripture, emptyWithLogo, video
	};

	private Type type;
	private String line;
	private String translation;
	private String book;
	private int chapter;
	private int[] verses;
	private VerseRange verseRange;

	private List<SlideContents> slides = new ArrayList<SlideContents>();

	public LiturgyItem(String line, Type type, String translation, String book, int chapter, int[] verses, VerseRange verseRange) {
		this.line = line;
		this.type = type;
		this.book = book;
		this.chapter = chapter;
		this.verses = verses;
		this.verseRange = verseRange;
		this.translation = translation;
	}

	public String getTranslation() {
		return translation;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public int[] getVerses() {
		return verses;
	}

	public void setVerses(int[] verses) {
		this.verses = verses;
	}

	public VerseRange getVerseRange() {
		return verseRange;
	}

	public void setVerseRange(VerseRange verseRange) {
		this.verseRange = verseRange;
	}

	public void setSlides(List<SlideContents> slides) {
		this.slides = slides;
	}

	public LiturgyItem(Type type) {
		setType(type);
	}

	public void addSlide(SlideContents slide) {
		slides.add(slide);
	}

	public List<SlideContents> getSlides() {
		return slides;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	/**
	 * fetches the contents for the slide based on the type
	 */
	public LiturgyItem loadContent() {
		switch (this.type) {
		case song:
			try {
				getSongContent();
			} catch (SongBookException e) {
				e.printStackTrace();
			}
			break;

		case scripture:
			try {
				getScriptureContent();
			} catch (BibleException e) {
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
		return this;
	}

	private void getScriptureContent() throws BibleException {
		Bible.getBiblePart(translation, book, chapter, verseRange);
		ScriptureSlide scripture = new ScriptureSlide(Bible.getBiblePart(translation, book, chapter, verseRange), book, translation, chapter, verseRange);
		addSlide(scripture);
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

	private void getSongContent() throws SongBookException {
		SlideContents.Type scType = null;
		final String regex_psalm = "([pP]salm )";
		final String regex_gezang = "([gG]ezang(en)?)";
		final String regex_lied = "([lL]ied([bB]oek)?)";
		final String regex_opwekking = "([oO]pwekking?)";
		final String regex_levenslied = "([lL]evenslied?)";

		// determine songtype
		String regex = String.format("^[ ]*(%s|%s|%s|%s|%s).*", regex_psalm, regex_gezang, regex_lied, regex_levenslied, regex_opwekking);
		Pattern songPattern = Pattern.compile(regex);
		java.util.regex.Matcher m = songPattern.matcher(line);

		m.find();
		if (m.group(1).matches(regex_psalm)) {
			scType = SlideContents.Type.psalm;
		} else if (m.group(1).matches(regex_gezang)) {
			scType = SlideContents.Type.gezang;
		} else if (m.group(1).matches(regex_lied)) {
			scType = SlideContents.Type.lied;
		} else if (m.group(1).matches(regex_levenslied)) {
			scType = SlideContents.Type.levenslied;
		} else if (m.group(1).matches(regex_opwekking)) {
			scType = SlideContents.Type.opwekking;
		}

		if (scType == SlideContents.Type.opwekking) {

			for (List<SongLine> verse : SongBook.getOpwekkingSongTekst(getSongNumber(line))) {
				SongSlide song = new SongSlide(line, verse);
				addSlide(song);
			}

		} else {

			if (!line.contains(":")) {
				List<String> allVerses = SongBook.getVersesFromSong(scType, getSongNumber(line));
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
				List<SongLine> songText = SongBook.getSongText(scType, getSongNumber(line), currentVerse);

				if (songText == null) {
					throw new SongBookException(String.format("Vers %s van %s %s niet gevonden", currentVerse, scType, getSongNumber(line)));
				}

				SongSlide song = new SongSlide(line, songText);
				song.setVerseNumber(currentVerse);
				addSlide(song);
			}
		}
	}

	public boolean isSong() {
		return getType() == Type.song;
	}

	public boolean isScripture() {
		return getType() == Type.scripture;
	}

}
