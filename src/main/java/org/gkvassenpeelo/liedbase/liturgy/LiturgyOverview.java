package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class LiturgyOverview extends SlideContents {

	private List<String> futureLiturgyLines = new ArrayList<String>();

	private List<String> pastLiturgyLines = new ArrayList<String>();

	public void addLiturgyLinePast(String line) {
		pastLiturgyLines.add(line);
	}

	public void addLiturgyLinesFuture(String line) {
		futureLiturgyLines.add(line);
	}

	public List<String> getLiturgyLinesPast() {
		return pastLiturgyLines;
	}

	public List<String> getLiturgyLinesFuture() {
		return futureLiturgyLines;
	}

}
