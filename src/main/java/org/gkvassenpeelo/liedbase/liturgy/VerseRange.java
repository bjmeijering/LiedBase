package org.gkvassenpeelo.liedbase.liturgy;

public class VerseRange {

	private int startVerse;
	private int endVerse;

	public VerseRange(int start, int end) {
		startVerse = start;
		endVerse = end;
	}

	public int getStartVerse() {
		return startVerse;
	}

	public int getEndVerse() {
		return endVerse;
	}

}
