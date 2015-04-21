package org.gkvassenpeelo.liedbase.liturgy;

import java.util.List;

import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;

public class Scripture extends SlideContents {

	private List<BiblePartFragment> biblePart;

	private String bibleBook;

	private String translation;

	private int chapter;

	private int fromVerse;

	private int toVerse;

	public Scripture(List<BiblePartFragment> biblePart, String bibleBook, String translation, int chapter, int fromVerse, int toVerse) {
		this.biblePart = biblePart;
		this.bibleBook = bibleBook;
		this.chapter = chapter;
		this.fromVerse = fromVerse;
		this.toVerse = toVerse;
		this.translation = translation;
	}

	public List<BiblePartFragment> getBiblePart() {
		return this.biblePart;
	}

	public String getBibleBook() {
		return bibleBook;
	}

	public int getChapter() {
		return chapter;
	}

	public int getFromVerse() {
		return fromVerse;
	}

	public int getToVerse() {
		return toVerse;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}
}
