package org.gkvassenpeelo.liedbase.slidemachine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.gkvassenpeelo.liedbase.liturgy.Liturgy;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyOverview;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.pptx4j.Pptx4jException;

public class SlideMachine {

	static final Logger logger = Logger.getLogger(SlideMachine.class);

	private MainPresentationPart targetPresentationPart;

	private String targetFilename = "Presentatie.pptx";

	private PresentationMLPackage presentationMLPackage;

	private List<LiturgyPart.Type> followedByLiturgyOverview = new ArrayList<LiturgyPart.Type>();

	private int currentLiturgyPartIndex = 0;

	private boolean showLiturgyOverview = true;

	private List<String> liturgyView;

	SlideFactory slideFactory;

	private Liturgy liturgy;

	public SlideMachine(LiturgyModel model) throws SlideMachineException {
		this.liturgy = model.getLiturgy();
		this.liturgyView = model.getLiturgyView();

		try {
			presentationMLPackage = (PresentationMLPackage) OpcPackage.load(ClassLoader.getSystemResourceAsStream("template.pptx"));

			slideFactory = new SlideFactory();

			// Need references to these parts to create a slide
			// Please note that these parts *already exist* - they are
			// created by createPackage() above. See that method
			// for instruction on how to create and add a part.
			targetPresentationPart = slideFactory.getMainPresentationPart(presentationMLPackage);

			// remove the first slide
			targetPresentationPart.removeSlide(0);

			// fill list containing slide types after which a liturgy overview slide
			// must be added
			followedByLiturgyOverview.add(LiturgyPart.Type.welcome);
			followedByLiturgyOverview.add(LiturgyPart.Type.law);
			followedByLiturgyOverview.add(LiturgyPart.Type.song);
			followedByLiturgyOverview.add(LiturgyPart.Type.lecture);
			followedByLiturgyOverview.add(LiturgyPart.Type.votum);
			followedByLiturgyOverview.add(LiturgyPart.Type.prair);
			followedByLiturgyOverview.add(LiturgyPart.Type.scripture);
			followedByLiturgyOverview.add(LiturgyPart.Type.gathering);
		} catch (Docx4JException e) {
			throw new SlideMachineException(e.getMessage(), e);
		} catch (Pptx4jException e) {
			throw new SlideMachineException(e.getMessage(), e);
		}
	}

	// Where will we save our new .pptx?
	public void setTargetFilename(String filename) {
		targetFilename = filename;
	}

	public void createSlides() {

		try {
			// Liturgy parsed and created, time to create Slides
			setTargetFilename(getTargetFilename());

			for (LiturgyPart lp : liturgy.getLiturgyParts()) {

				if (lp.getSlides().size() == 0) {
					addSlide(null, lp.getType());
				} else {
					for (SlideContents sc : lp.getSlides()) {
						addSlide(sc, lp.getType());
					}
				}

				addIntermediateSlide(lp);

			}

		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			System.exit(1);
		} catch (Pptx4jException e) {
			logger.error(e.getMessage(), e);
			System.exit(1);
		} catch (Docx4JException e) {
			logger.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	private void addIntermediateSlide(LiturgyPart lp) throws JAXBException, Pptx4jException, Docx4JException {
		// after some liturgy parts, add an overview slide, except for
		// the last one!
		if (followedByLiturgyOverview.contains(lp.getType()) && showLiturgyOverview) {
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

			}

			lo.setHeader("Liturgie:");
			lo.setBody(builder.toString());
			addSlide(lo, LiturgyPart.Type.liturgyOverview);
		} else {
			if (lp.getType() != LiturgyPart.Type.endOfMorningService && lp.getType() != LiturgyPart.Type.endOfAfternoonService) {
				addSlide(null, LiturgyPart.Type.blank);
			}
		}
	}

	public void addSlide(SlideContents content, LiturgyPart.Type type) throws JAXBException, Pptx4jException, Docx4JException {

		SlidePart slidePart = slideFactory.createSlide(targetPresentationPart.getSlideCount());
		targetPresentationPart.addSlide(slidePart);

		slideFactory.addSlideContents(presentationMLPackage, slidePart, content, type);

	}

	private String getTargetFilename() {
		return targetFilename;
	}

	public void save() throws Docx4JException {
		presentationMLPackage.save(new File(targetFilename));
	}

}
