package org.gkvassenpeelo.liedbase.liturgy;

import java.util.List;

import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;

public class ScriptureContents extends SlideContents {

	private List<BiblePartFragment> biblePart;

	private String bibleBook;

	private String translation;

	private int chapter;

	private VerseRange verseRange;

	public ScriptureContents(List<BiblePartFragment> biblePart, String bibleBook, String translation, int chapter,
			VerseRange verseRange) {
		this.biblePart = biblePart;
		this.bibleBook = bibleBook;
		this.chapter = chapter;
		this.verseRange = verseRange;
		this.translation = translation;
	}

	public List<BiblePartFragment> getBiblePart() {
		return this.biblePart;
	}

	public String getBibleBook() {
		return bibleBook.substring(0, 1).toUpperCase() + bibleBook.substring(1);
	}

	public int getChapter() {
		return chapter;
	}

	public int getFromVerse() {
		return verseRange.getStartVerse();
	}

	public int getToVerse() {
		return verseRange.getEndVerse();
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public String getFormattedHeader() {
		return String.format("%s %s: %s - %s", getBibleBook(), getChapter(), getFromVerse(), getToVerse());
	}
}
