package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class LiturgyItem {

	public enum Type {
		welcome, blank, schoonmaak, song, gathering, prair, law, lecture, votum, amen, endOfMorningService, 
		endOfAfternoonService, scripture, agenda, liturgyOverview, extendedScripture, emptyWithLogo, video
	};

	private Type type;
	private String line;
	private String book;
	private String chapter;
	private List<String> verses = new ArrayList<String>();
	private VerseRange verseRange;

	public LiturgyItem(String line, Type type, String book, String chapter, List<String> verses, VerseRange verseRange) {
		this.line = line;
		this.type = type;
		this.book = book;
		this.chapter = chapter;
		this.verses = verses;
		this.verseRange = verseRange;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public List<String> getVerses() {
		return verses;
	}

	public void setVerses(List<String> verses) {
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

	private List<SlideContents> slides = new ArrayList<SlideContents>();

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

}
