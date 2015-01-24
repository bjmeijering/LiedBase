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
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.liedbase.liturgy.Welcome;
import org.gkvassenpeelo.liedbase.slidemachine.model.GenericSlideContent;
import org.gkvassenpeelo.liedbase.slidemachine.model.LiturgyOverview;
import org.pptx4j.Pptx4jException;

public class SlideMachine {

	static final Logger logger = Logger.getLogger(SlideMachine.class);

	private MainPresentationPart targetPresentationPart;

	private File targetFile = new File("presentatie.pptx");

	private PresentationMLPackage presentationMLPackage;

	private List<LiturgyPart.Type> followedByLiturgyOverview = new ArrayList<LiturgyPart.Type>();

	private int currentLiturgyPartIndex = 0;

	private boolean showLiturgyOverview = true;

	private List<String> liturgyView;

	SlideFactory slideFactory;

	private Liturgy liturgy;

	public SlideMachine(Liturgy liturgy, List<String> liturgyView) throws SlideMachineException {
		this.liturgy = liturgy;
		this.liturgyView = liturgyView;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Where will we save our new .pptx?
	public void setTargetFile(File filename) {
		targetFile = filename;
	}

	public void createSlides() {

		try {
			// Liturgy parsed and created, time to create Slides
			setTargetFile(getTargetFile());

			for (LiturgyPart lp : liturgy.getLiturgyParts()) {

				if (lp.getType() == LiturgyPart.Type.song) {

					for (SlideContents sc : lp.getSlides()) {

						GenericSlideContent gsc = new org.gkvassenpeelo.liedbase.slidemachine.model.Song();

						((org.gkvassenpeelo.liedbase.slidemachine.model.Song) gsc).setCurrentVerse(((Song) sc).getVerseNumber());
						gsc.setHeader(sc.getHeader());
						gsc.setBody(sc.getBody());

						addSlide(gsc);

					}
				} else if (lp.getType() == LiturgyPart.Type.gathering) {
					GenericSlideContent gsc = new org.gkvassenpeelo.liedbase.slidemachine.model.Gathering();
					((org.gkvassenpeelo.liedbase.slidemachine.model.Gathering) gsc).setFirstBenificiary(((org.gkvassenpeelo.liedbase.liturgy.Gathering) lp.getSlides().get(0))
							.getFirstGatheringBenificiary());
					((org.gkvassenpeelo.liedbase.slidemachine.model.Gathering) gsc).setSecondBenificiary(((org.gkvassenpeelo.liedbase.liturgy.Gathering) lp.getSlides().get(0))
							.getSecondGatheringBenificiary());
					addSlide(gsc);
				} else if (lp.getType() == LiturgyPart.Type.agenda) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Agenda());
				} else if (lp.getType() == LiturgyPart.Type.welcome) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Welcome(((Welcome) lp.getSlides().get(0)).getVicarName()));
				} else if (lp.getType() == LiturgyPart.Type.prair) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Prair());
				} else if (lp.getType() == LiturgyPart.Type.votum) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Votum());
				} else if (lp.getType() == LiturgyPart.Type.endOfMorningService) {

					GenericSlideContent gsc = new org.gkvassenpeelo.liedbase.slidemachine.model.EndMorningService();

					((org.gkvassenpeelo.liedbase.slidemachine.model.EndMorningService) gsc)
							.setTime(((org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService) lp.getSlides().get(0)).getTime());
					((org.gkvassenpeelo.liedbase.slidemachine.model.EndMorningService) gsc).setVicarName(((org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService) lp.getSlides()
							.get(0)).getVicarName());

					addSlide(gsc);
				} else if (lp.getType() == LiturgyPart.Type.endOfAfternoonService) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.EndAfternoonService());
				} else if (lp.getType() == LiturgyPart.Type.amen) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Amen());
				} else if (lp.getType() == LiturgyPart.Type.law) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Law());
				} else if (lp.getType() == LiturgyPart.Type.lecture) {
					addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Lecture());
				} else if (lp.getType() == LiturgyPart.Type.scripture) {
					GenericSlideContent gsc = new org.gkvassenpeelo.liedbase.slidemachine.model.Scripture(lp.getSlides().get(0));
					addSlide(gsc);
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
			LiturgyOverview lo = new org.gkvassenpeelo.liedbase.slidemachine.model.LiturgyOverview();

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
			addSlide(lo);
		} else {
			if (lp.getType() != LiturgyPart.Type.endOfMorningService && lp.getType() != LiturgyPart.Type.endOfAfternoonService) {
				addSlide(new org.gkvassenpeelo.liedbase.slidemachine.model.Blank());
			}
		}
	}

	public void addSlide(GenericSlideContent content) throws JAXBException, Pptx4jException, Docx4JException {

		SlidePart slidePart = slideFactory.createSlide(targetPresentationPart.getSlideCount());
		targetPresentationPart.addSlide(slidePart);

		slideFactory.addSlideContents(presentationMLPackage, slidePart, content);

	}

	private File getTargetFile() {
		return targetFile;
	}

	public void save() throws Docx4JException {
		presentationMLPackage.save(targetFile);
	}

}
