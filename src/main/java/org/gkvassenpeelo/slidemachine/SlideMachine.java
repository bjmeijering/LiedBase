package org.gkvassenpeelo.slidemachine;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.gkvassenpeelo.slidemachine.model.GenericSlideContent;
import org.pptx4j.Pptx4jException;

public class SlideMachine {

    private MainPresentationPart targetPresentationPart;
    private File targetFile;
    private PresentationMLPackage presentationMLPackage;
    SlideFactory slideFactory;

    public SlideMachine() {
    }

    public void init() throws Docx4JException, Pptx4jException {

        presentationMLPackage = (PresentationMLPackage) OpcPackage.load(ClassLoader.getSystemResourceAsStream("template.pptx"));

        slideFactory = new SlideFactory();

        // Need references to these parts to create a slide
        // Please note that these parts *already exist* - they are
        // created by createPackage() above. See that method
        // for instruction on how to create and add a part.
        targetPresentationPart = slideFactory.getMainPresentationPart(presentationMLPackage);

        // remove the first slide
        targetPresentationPart.removeSlide(0);
    }

    // Where will we save our new .pptx?
    public void setTargetFile(File filename) {
        targetFile = filename;
    }

    public void addSlide(GenericSlideContent content) throws JAXBException, Pptx4jException, Docx4JException {

        SlidePart slidePart = slideFactory.createSlide(targetPresentationPart.getSlideCount());
        targetPresentationPart.addSlide(slidePart);

        slideFactory.addSlideContents(presentationMLPackage, slidePart, content);

    }

    public void save() throws Docx4JException {
        // All done: save it
        presentationMLPackage.save(targetFile);
    }

}
