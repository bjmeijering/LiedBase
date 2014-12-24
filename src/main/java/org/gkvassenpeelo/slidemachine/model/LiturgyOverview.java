package org.gkvassenpeelo.slidemachine.model;

import java.util.ArrayList;
import java.util.List;

public class LiturgyOverview extends GenericSlideContent {
    
	private List<String> liturgyLinesPast = new ArrayList<String>();
    private List<String> liturgyLinesFuture = new ArrayList<String>();

	public List<String> getLiturgyLinesPast() {
		return liturgyLinesPast;
	}

	public void addLiturgyLinePast(String liturgyLine) {
		this.liturgyLinesPast.add(liturgyLine);
	}

	public List<String> getLiturgyLinesFuture() {
		return liturgyLinesFuture;
	}

	public void addLiturgyLinesFuture(String liturgyLine) {
		this.liturgyLinesFuture.add(liturgyLine);
	}

}
