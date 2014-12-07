package org.gkvassenpeelo.slidemachine;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.gkvassenpeelo.slidemachine.model.Song;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pptx4j.Pptx4jException;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.CTGraphicalObjectFrame;

public class SlideMachineTest {

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    public void testSlideMachine() throws Docx4JException, Pptx4jException, JAXBException {
        SlideMachine sm = new SlideMachine();
        sm.init();
        sm.setTargetFile(new File("/target/presentation.pptx"));

        Song song = new Song();
        song.setHeader("Samen in de naam van Jezus");
        song.setBody("Samen in de naam van Jezus" + System.getProperty("line.separator") + "Hef ik hier mijn loflied aan");

        sm.addSlide(song);
        sm.save();
    }

    @Test
    public void prinlideContents() throws Docx4JException, JAXBException {
        
        PresentationMLPackage presentationMLPackage = (PresentationMLPackage) OpcPackage.load(new java.io.File("E:/Projects/Eclipse Workspace/LiedBase/target/Presentatie.pptx"));

        SlidePart slide = (SlidePart) presentationMLPackage.getParts().get(new PartName("/ppt/slides/slide1.xml"));

        CTGraphicalObjectFrame graphicFrame = (CTGraphicalObjectFrame) slide.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()
                .get(0);

        String foo = XmlUtils.marshaltoString(graphicFrame, true, true, Context.jcPML, "http://schemas.openxmlformats.org/presentationml/2006/main", "graphicFrame",
                CTGraphicalObjectFrame.class);

        System.out.println(foo);

//        org.pptx4j.pml.CTGraphicalObjectFrame graphicFrame2 = (org.pptx4j.pml.CTGraphicalObjectFrame) XmlUtils.unmarshalString(foo, Context.jcPML, CTGraphicalObjectFrame.class);

    }

}
