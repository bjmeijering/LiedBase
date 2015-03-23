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
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
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

	private List<LiturgyPart.Type> liturgyPartsToPrint = new ArrayList<LiturgyPart.Type>();

	public PaperMachine(Liturgy liturgy) throws PaperMachineException {

		this.liturgy = liturgy;

		liturgyPartsToPrint.add(LiturgyPart.Type.amen);
		liturgyPartsToPrint.add(LiturgyPart.Type.gathering);
		liturgyPartsToPrint.add(LiturgyPart.Type.law);
		liturgyPartsToPrint.add(LiturgyPart.Type.lecture);
		liturgyPartsToPrint.add(LiturgyPart.Type.prair);
		liturgyPartsToPrint.add(LiturgyPart.Type.scripture);
		liturgyPartsToPrint.add(LiturgyPart.Type.song);
		liturgyPartsToPrint.add(LiturgyPart.Type.votum);

	}

	public void createDocument() throws PaperMachineException {
		try {
			wordMLPackage = WordprocessingMLPackage.createPackage();
			mainDocumentPart = wordMLPackage.getMainDocumentPart();

			for (LiturgyPart lp : liturgy.getLiturgyParts()) {
				try {
					addLiturgyPartToDocument(lp);
				} catch (JAXBException e) {
					throw new PaperMachineException(e.getMessage());
				}
			}

		} catch (InvalidFormatException e) {
			throw new PaperMachineException(String.format("Error while creating paper machine: %s", e.getMessage()), e);
		}
	}

	@SuppressWarnings("deprecation")
	private void addLiturgyPartToDocument(LiturgyPart lp) throws JAXBException {
		if (!liturgyPartsToPrint.contains(lp.getType())) {
			return;
		}

		if (lp.getType() == LiturgyPart.Type.scripture) {

			for (SlideContents sc : lp.getSlides()) {

				mainDocumentPart.addStyledParagraphOfText("", lp.getLine());

				// Create the paragraph
				org.docx4j.wml.P para = getScriptureShape(((Scripture) sc).getBiblePart());

				// Now add our paragraph to the document body
				Body body = mainDocumentPart.getJaxbElement().getBody();
				body.getEGBlockLevelElts().add(para);

			}
			return;
		}

		if (lp.getType() == LiturgyPart.Type.song) {

			for (SlideContents sc : lp.getSlides()) {

				mainDocumentPart.addParagraphOfText(lp.getLine());

				// Create the paragraph
				org.docx4j.wml.P para = getSongShape(((Song) sc).getSongText());

				// Now add our paragraph to the document body
				Body body = mainDocumentPart.getJaxbElement().getBody();
				body.getEGBlockLevelElts().add(para);

			}
			return;
		}
	}

	private P getScriptureShape(List<BiblePartFragment> list) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("fragments", list);
		vc.put("nol", list.size());

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/docx/shape_bible_paragraph.vc", ENCODING).merge(vc, ow);

		org.docx4j.wml.P shape = (P) XmlUtils.unmarshalString(ow.toString());
		return shape;
	}

	private P getSongShape(List<SongLine> list) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("lines", list);

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
