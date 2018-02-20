package org.gkvassenpeelo.liedbase.liturgy;

public class VerseRange {

	private String startVerse;
	private String endVerse;

	public VerseRange(String start, String end) {
		startVerse = start;
		endVerse = end;
	}

	public String getStartVerse() {
		return startVerse;
	}

	public String getEndVerse() {
		return endVerse;
	}

}
