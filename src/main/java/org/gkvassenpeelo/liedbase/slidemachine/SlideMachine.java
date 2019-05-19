package org.gkvassenpeelo.liedbase.slidemachine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyItem;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyOverview;
import org.gkvassenpeelo.liedbase.liturgy.ScriptureSlide;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.SongSlide;
import org.gkvassenpeelo.liedbase.songbook.SongLine;

public class SlideMachine {

	private static final String SLIDE_BREAK = "\n\n\n\n";
	private static final String BLANK_LINE = "\n\n";
	private static final String SUPERSCRIPT_OPEN = "<sup>";
	private static final String SUPERSCRIPT_CLOSE = "</sup>";

	static final Logger logger = Logger.getLogger(SlideMachine.class);

	private List<LiturgyItem.Type> followedByLiturgyOverview = new ArrayList<LiturgyItem.Type>();

	private int currentLiturgyPartIndex = 0;

	private boolean showLiturgyOverview = true;

	private List<String> liturgyView = new ArrayList<String>();

	private String targetFilename;

	private List<LiturgyItem> items;

	public SlideMachine(List<LiturgyItem> items) throws SlideMachineException {

		this.items = items;

		// fill list containing slide types after which a liturgy overview slide
		// must be added
		followedByLiturgyOverview.add(LiturgyItem.Type.welcome);
		followedByLiturgyOverview.add(LiturgyItem.Type.law);
		followedByLiturgyOverview.add(LiturgyItem.Type.song);
		followedByLiturgyOverview.add(LiturgyItem.Type.lecture);
		followedByLiturgyOverview.add(LiturgyItem.Type.votum);
		followedByLiturgyOverview.add(LiturgyItem.Type.prair);
		followedByLiturgyOverview.add(LiturgyItem.Type.scripture);
		followedByLiturgyOverview.add(LiturgyItem.Type.gathering);
	}

	// Where will we save our new Markdown file?
	public void setTargetFilename(String filename) {
		targetFilename = filename;
	}

	public void createSlides() throws SlideMachineException {

		try {
			// Liturgy parsed and created, time to create Slides
			setTargetFilename(getTargetFilename());

			// also create Markdown file
			FileWriter md = new FileWriter(new File("/Users/hdoedens/coding/reveal.js/liturgy.md"));

			for (LiturgyItem item : items) {

				if (item.isSong()) {

					for (SlideContents slide : item.getSlides()) {

						SongSlide songSlide = (SongSlide) slide;

						md.append(songSlide.getHeader());
						md.append(BLANK_LINE);
						for (SongLine line : songSlide.getSongText()) {
							md.append(line.getContent() + "  \n");
						}
						md.append(SLIDE_BREAK);
					}
				} else if (item.isScripture()) {

					for (SlideContents slide : item.getSlides()) {

						ScriptureSlide scriptureSlide = (ScriptureSlide) slide;

						// print header
						md.append(scriptureSlide.getFormattedHeader());
						md.append(BLANK_LINE);

						// print main bible text
						for (BiblePartFragment fragment : scriptureSlide.getBiblePart()) {
							if (fragment.getDisplayType() == BiblePartFragment.DisplayType.superScript) {
								md.append(superscript(fragment.getContent()));
							} else {
								md.append(fragment.getContent());
							}
						}
						md.append(SLIDE_BREAK);
					}
				} else {
					md.append("item van type: " + item.getType().toString());
					md.append(SLIDE_BREAK);
				}

				addIntermediateSlide(item, md);

			}

			// close Markdown file
			md.close();

		} catch (IOException e) {
			throw new SlideMachineException(e.getMessage(), e);
		}
	}

	private String superscript(String content) {
		return SUPERSCRIPT_OPEN + content + SUPERSCRIPT_CLOSE;
	}

	private void addIntermediateSlide(LiturgyItem lp, FileWriter md) throws IOException {
		// after some liturgy parts, add an overview slide, except for
		// the last one!

		if (liturgyView.size() == 0) {
			createLiturgyView();
		}

		if (followedByLiturgyOverview.contains(lp.getType()) && showLiturgyOverview) {

			md.append("Liturgie:" + BLANK_LINE);

			LiturgyOverview lo = new LiturgyOverview();

			StringBuilder builder = new StringBuilder();
			int pos = liturgyView.indexOf(lp.getLine());
			int currentPosition = 0;
			for (String s : liturgyView) {

				if (pos == -1) {
					if (currentLiturgyPartIndex > currentPosition++) {
						lo.addLiturgyLinePast(s);
					} else {
						lo.addLiturgyLinesFuture(s);
					}
				} else {
					if (liturgyView.indexOf(s) <= pos) {
						lo.addLiturgyLinePast(s);
						currentLiturgyPartIndex = pos + 1;
					} else {
						lo.addLiturgyLinesFuture(s);
					}

				}

				md.append(s + "  \n");

			}
			lo.setBody(builder.toString());
			// addSlide(lo, LiturgyItem.Type.liturgyOverview);
		} else {
			if (lp.getType() != LiturgyItem.Type.endOfMorningService
					&& lp.getType() != LiturgyItem.Type.endOfAfternoonService) {
				// addSlide(null, LiturgyItem.Type.blank);
				md.append(SLIDE_BREAK);
			}
		}

		md.append(SLIDE_BREAK);
	}

	private void createLiturgyView() {
		for (LiturgyItem item : items) {
			liturgyView.add(item.getType().toString());
		}
	}

	private String getTargetFilename() {
		return targetFilename;
	}

}
