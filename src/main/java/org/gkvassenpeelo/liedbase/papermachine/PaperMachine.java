package org.gkvassenpeelo.liedbase.papermachine;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.gkvassenpeelo.liedbase.liturgy.Liturgy;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;

public class PaperMachine {

	private Liturgy liturgy;

	private MainDocumentPart mainDocumentPart;

	private WordprocessingMLPackage wordMLPackage;
	
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
				addLiturgyPartToDocument(lp);
			}

		} catch (InvalidFormatException e) {
			throw new PaperMachineException(String.format("Error while creating paper machine: %s", e.getMessage()), e);
		}
	}

	private void addLiturgyPartToDocument(LiturgyPart lp) {
		if(!liturgyPartsToPrint.contains(lp.getType())) {
			return;
		}
		
		mainDocumentPart.addStyledParagraphOfText("", lp.getLine());
		for (SlideContents sc : lp.getSlides()) {
			mainDocumentPart.addParagraphOfText(sc.getBody());
		}
	}

	public void save(String location) throws PaperMachineException {
		try {
			wordMLPackage.save(new java.io.File(location));
		} catch (Docx4JException e) {
			throw new PaperMachineException(String.format("Error while saving paper machine: %s", e.getMessage()), e);
		}
	}

}
