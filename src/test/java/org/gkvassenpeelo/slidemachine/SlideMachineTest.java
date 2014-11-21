package org.gkvassenpeelo.slidemachine;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.gkvassenpeelo.slidemachine.model.Song;
import org.junit.Before;
import org.junit.Test;
import org.pptx4j.Pptx4jException;

public class SlideMachineTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
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
    // use for debugging only!
    @SuppressWarnings("deprecation")
    public void printSlideContents() throws Docx4JException {

        PresentationMLPackage presentationMLPackage = (PresentationMLPackage) OpcPackage.load(new java.io.File("E:/Projects/Eclipse Workspace/LiedBase/target/presentatie.pptx"));
        
        // get specific slide
        SlidePart slidePart = (SlidePart) presentationMLPackage.getParts().get(new PartName("/ppt/slides/slide1.xml"));
        List<Object> shapeList = slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame();

        String indent = "";

        for (Object o : shapeList) {
            try {
                System.out.println(indent + XmlUtils.marshaltoString(o, true, org.pptx4j.jaxb.Context.jcPML));
            } catch (RuntimeException me) {
                System.out.println(indent + o.getClass().getName());
            }

//            if (o instanceof org.pptx4j.pml.Shape) {
//                CTTextBody txBody = ((org.pptx4j.pml.Shape) o).getTxBody();
//                if (txBody != null) {
//                    for (CTTextParagraph tp : txBody.getP()) {
//
//                        System.out.println(indent
//                                + XmlUtils.marshaltoString(tp, true, true, org.pptx4j.jaxb.Context.jcPML,
//                                        "http://schemas.openxmlformats.org/presentationml/2006/main", "txBody",
//                                        CTTextParagraph.class));
//
//                    }
//                }
//            }
        }
    }

}
