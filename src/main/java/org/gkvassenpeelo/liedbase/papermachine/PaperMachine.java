package org.gkvassenpeelo.liedbase.papermachine;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.gkvassenpeelo.liedbase.bible.BiblePartFragment;
import org.gkvassenpeelo.liedbase.liturgy.Liturgy;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyItem;
import org.gkvassenpeelo.liedbase.liturgy.Scripture;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.liedbase.songbook.SongLine;

public class PaperMachine {

	private Liturgy liturgy;

	private MainDocumentPart mainDocumentPart;

	private WordprocessingMLPackage wordMLPackage;

	private File targetFile = new File("LiturgieBoekje.docx");

	private VelocityEngine velocityEngine;

	private static final String ENCODING = "UTF-8";

	ObjectFactory factory = Context.getWmlObjectFactory();

	private List<LiturgyItem.Type> liturgyPartsToPrint = new ArrayList<LiturgyItem.Type>();

	public PaperMachine(Liturgy liturgy) throws PaperMachineException {

		this.liturgy = liturgy;

		liturgyPartsToPrint.add(LiturgyItem.Type.amen);
		liturgyPartsToPrint.add(LiturgyItem.Type.gathering);
		liturgyPartsToPrint.add(LiturgyItem.Type.law);
		liturgyPartsToPrint.add(LiturgyItem.Type.lecture);
		liturgyPartsToPrint.add(LiturgyItem.Type.prair);
		liturgyPartsToPrint.add(LiturgyItem.Type.scripture);
		liturgyPartsToPrint.add(LiturgyItem.Type.song);
		liturgyPartsToPrint.add(LiturgyItem.Type.votum);

	}

	public void createDocument() throws PaperMachineException {
		try {
			wordMLPackage = WordprocessingMLPackage.load(this.getClass().getClassLoader().getResourceAsStream("template.docx"));
			mainDocumentPart = wordMLPackage.getMainDocumentPart();

			for (LiturgyItem lp : liturgy.getLiturgyParts()) {
				try {
					addLiturgyPartToDocument(lp);
				} catch (JAXBException e) {
					throw new PaperMachineException(e.getMessage(), e);
				} catch (Exception e) {
					throw new PaperMachineException(e.getMessage(), e);
				}
			}

		} catch (InvalidFormatException e) {
			throw new PaperMachineException(String.format("Error while creating paper machine: %s", e.getMessage()), e);
		} catch (Docx4JException e) {
			throw new PaperMachineException(String.format("Error while loading paper template: %s", e.getMessage()), e);
		}
	}

	@SuppressWarnings("deprecation")
	private void addLiturgyPartToDocument(LiturgyItem lp) throws JAXBException {
		if (!liturgyPartsToPrint.contains(lp.getType())) {
			return;
		}

		// Get the body of the document
		Body body = mainDocumentPart.getJaxbElement().getBody();

		if (lp.getType() != LiturgyItem.Type.votum) {
			// an empty line
			body.getEGBlockLevelElts().add(getEmptyParagraphShape());
		}

		if (lp.getType() == LiturgyItem.Type.scripture) {

			for (SlideContents sc : lp.getSlides()) {

				// Only add when there is actually text to display
				if (((Scripture) sc).getBiblePart() != null) {
					// Create the heading text
					org.docx4j.wml.P title = getTitleTextShape("Schriftlezing: " + lp.getLine());
					body.getEGBlockLevelElts().add(title);

					// Create the paragraph
					org.docx4j.wml.P para = getScriptureShape(((Scripture) sc).getBiblePart());

					// Now add our paragraph to the document body
					body.getEGBlockLevelElts().add(para);
				}

			}

			return;

		} else if (lp.getType() == LiturgyItem.Type.song) {

			// Create the heading text
			org.docx4j.wml.P title = getTitleTextShape("Zingen: " + lp.getSlides().get(0).getHeader());
			body.getEGBlockLevelElts().add(title);

			for (SlideContents sc : lp.getSlides()) {

				String songNumber = ((Song) sc).getVerseNumber();

				for (SongLine line : ((Song) sc).getSongText()) {

					if (songNumber != null) {
						// an empty line
						body.getEGBlockLevelElts().add(getEmptyParagraphShape());
					}

					// Create the paragraph
					org.docx4j.wml.P para = getSongShape(songNumber, line);

					// add the songnumber only to the first line
					songNumber = null;

					body.getEGBlockLevelElts().add(para);

				}

			}

			return;

		} else if (lp.getType() == LiturgyItem.Type.votum) {

			// Create the paragraph
			org.docx4j.wml.P para = getTitleTextShape("Votum/zegengroet");

			// Now add our paragraph to the document body
			body.getEGBlockLevelElts().add(para);

		} else if (lp.getType() == LiturgyItem.Type.prair) {

			// Create the paragraph
			org.docx4j.wml.P para = getTitleTextShape("Gebed");

			// Now add our paragraph to the document body
			body.getEGBlockLevelElts().add(para);

		} else if (lp.getType() == LiturgyItem.Type.law) {

			// Create the paragraph
			org.docx4j.wml.P para = getTitleTextShape("Wet");

			// Now add our paragraph to the document body
			body.getEGBlockLevelElts().add(para);

		} else if (lp.getType() == LiturgyItem.Type.lecture) {

			// Create the paragraph
			org.docx4j.wml.P para = getTitleTextShape("Preek");

			// Now add our paragraph to the document body
			body.getEGBlockLevelElts().add(para);

		} else if (lp.getType() == LiturgyItem.Type.gathering) {

			// Create the paragraph
			org.docx4j.wml.P para = getTitleTextShape("Collecte");

			// Now add our paragraph to the document body
			body.getEGBlockLevelElts().add(para);

		} else if (lp.getType() == LiturgyItem.Type.amen) {

			// Create the paragraph
			org.docx4j.wml.P para = getTitleTextShape("Zegen");

			// Now add our paragraph to the document body
			body.getEGBlockLevelElts().add(para);

		}
	}

	private Object getEmptyParagraphShape() throws JAXBException {
		VelocityContext vc = new VelocityContext();

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/docx/shape_empty_paragraph.vc", ENCODING).merge(vc, ow);

		org.docx4j.wml.P shape = (P) XmlUtils.unmarshalString(ow.toString());
		return shape;
	}

	private P getTitleTextShape(String titleText) throws JAXBException {

		VelocityContext vc = new VelocityContext();
		vc.put("titleText", titleText);

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/docx/shape_title.vc", ENCODING).merge(vc, ow);

		org.docx4j.wml.P shape = (P) XmlUtils.unmarshalString(ow.toString());
		return shape;

	}

	private P getScriptureShape(List<BiblePartFragment> list) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		if (list != null) {
			vc.put("fragments", list);
			vc.put("nol", list.size());
		}

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/docx/shape_bible_paragraph.vc", ENCODING).merge(vc, ow);

		org.docx4j.wml.P shape = (P) XmlUtils.unmarshalString(ow.toString());
		return shape;
	}

	private P getSongShape(String songNumber, SongLine line) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("songNumber", songNumber);
		vc.put("line", line.getContent());

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/docx/shape_song.vc", ENCODING).merge(vc, ow);

		org.docx4j.wml.P shape = (P) XmlUtils.unmarshalString(ow.toString());
		return shape;
	}

	/**
	 * 
	 * @return
	 */
	private VelocityEngine getVelocityEngine() {
		if (velocityEngine == null) {
			velocityEngine = new VelocityEngine();
			velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,classpath");
			velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		}
		return velocityEngine;
	}

	public void save() throws PaperMachineException {
		try {
			wordMLPackage.save(targetFile);
		} catch (Docx4JException e) {
			throw new PaperMachineException(String.format("Error while saving paper machine: %s", e.getMessage()), e);
		}
	}

	public void setTargetFile(File file) {
		this.targetFile = file;
	}

}
