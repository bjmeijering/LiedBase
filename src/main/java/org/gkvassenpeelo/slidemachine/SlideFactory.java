package org.gkvassenpeelo.slidemachine;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.gkvassenpeelo.slidemachine.model.Agenda;
import org.gkvassenpeelo.slidemachine.model.Amen;
import org.gkvassenpeelo.slidemachine.model.Blank;
import org.gkvassenpeelo.slidemachine.model.EndAfternoonService;
import org.gkvassenpeelo.slidemachine.model.EndMorningService;
import org.gkvassenpeelo.slidemachine.model.Gathering;
import org.gkvassenpeelo.slidemachine.model.GenericSlideContent;
import org.gkvassenpeelo.slidemachine.model.Law;
import org.gkvassenpeelo.slidemachine.model.Lecture;
import org.gkvassenpeelo.slidemachine.model.Prair;
import org.gkvassenpeelo.slidemachine.model.Scripture;
import org.gkvassenpeelo.slidemachine.model.Song;
import org.gkvassenpeelo.slidemachine.model.Votum;
import org.gkvassenpeelo.slidemachine.model.Welcome;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.pptx4j.pml.Shape;

/**
 * This SlideFactory needs an original presentation containing the correct template.
 * 
 * @author m07b836
 * 
 */
public class SlideFactory {

    private Map<Class<? extends GenericSlideContent>, String> slideLayoutMap = new HashMap<Class<? extends GenericSlideContent>, String>();

    private VelocityEngine velocityEngine;

    private static final String ENCODING = "UTF-8";

    /**
     * 
     */
    public SlideFactory() {
        // fill the slide layout map
        slideLayoutMap.put(Song.class, "/ppt/slideLayouts/slideLayout4.xml");
        slideLayoutMap.put(Scripture.class, "/ppt/slideLayouts/slideLayout4.xml");
        slideLayoutMap.put(Blank.class, "/ppt/slideLayouts/slideLayout19.xml");
        slideLayoutMap.put(Gathering.class, "/ppt/slideLayouts/slideLayout6.xml");
        slideLayoutMap.put(Prair.class, "/ppt/slideLayouts/slideLayout10.xml");
        slideLayoutMap.put(Welcome.class, "/ppt/slideLayouts/slideLayout1.xml");
        slideLayoutMap.put(Law.class, "/ppt/slideLayouts/slideLayout12.xml");
        slideLayoutMap.put(Votum.class, "/ppt/slideLayouts/slideLayout3.xml");
        slideLayoutMap.put(Amen.class, "/ppt/slideLayouts/slideLayout9.xml");
        slideLayoutMap.put(EndMorningService.class, "/ppt/slideLayouts/slideLayout7.xml");
        slideLayoutMap.put(EndAfternoonService.class, "/ppt/slideLayouts/slideLayout8.xml");
        slideLayoutMap.put(Lecture.class, "/ppt/slideLayouts/slideLayout14.xml");
        slideLayoutMap.put(Agenda.class, "/ppt/slideLayouts/slideLayout2.xml");

        // init velocity with defaults
        Velocity.init();
    }

    /**
     * 
     * @param presentationMLPackage
     * @param slidePart
     * @param content
     * @throws JAXBException
     * @throws Docx4JException
     * @throws SlideFactoryException
     */
    public void addSlideContents(PresentationMLPackage presentationMLPackage, SlidePart slidePart, GenericSlideContent content) throws JAXBException, Docx4JException {
        SlideLayoutPart layoutPart = (SlideLayoutPart) presentationMLPackage.getParts().getParts().get(new PartName(slideLayoutMap.get(content.getClass())));

        // add Slide layout part
        slidePart.addTargetPart(layoutPart);

        // Add contents to layout if neccesary
        if (content instanceof Song) {
            // Create and add header
            if (!StringUtils.isEmpty(content.getHeader())) {
                Shape header = createSongHeaderShape((Song) content);
                slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(header);
            }

            // create and add body
            if (!StringUtils.isEmpty(content.getBody())) {
                Shape body = createSongBody(content.getBody());
                slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(body);
            }
        }
        if (content instanceof Gathering) {
            Shape benificiary = createGatheringShape(((Gathering) content).getFirstBenificiary(), true);
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(benificiary);
            benificiary = createGatheringShape(((Gathering) content).getSecondBenificiary(), false);
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(benificiary);
        }
        if (content instanceof Welcome) {
            Shape vicar = createVicarShape(((Welcome) content).getVicarName());
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(vicar);
        }
        if (content instanceof EndMorningService) {

            Shape time = createTimeShape(((EndMorningService) content).getTime());
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(time);

            Shape vicar = createNextVicarShape(((EndMorningService) content).getVicarName());
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(vicar);
        }
        if (content instanceof Scripture) {

            Shape scriptureHeader = createScriptureHeaderShape(content);
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(scriptureHeader);

            Shape scriptureBody = createScriptureBodyShape(content);
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(scriptureBody);

        }
        if (content instanceof Agenda) {
            
            CTGraphicalObjectFrame agenda = createAgendaShape();
            slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(agenda);
            
        }
    }

    private CTGraphicalObjectFrame createAgendaShape() throws JAXBException {
        VelocityContext vc = new VelocityContext();

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_agenda_table.vc", ENCODING).merge(vc, ow);
        
        return (CTGraphicalObjectFrame) XmlUtils.unmarshalString(ow.toString(), Context.jcPML, CTGraphicalObjectFrame.class);
    }

    private Shape createScriptureHeaderShape(GenericSlideContent content) throws JAXBException {
        VelocityContext vc = new VelocityContext();
        vc.put("bibleBook", ((Scripture) content).getBibleBook());
        vc.put("chapter", ((Scripture) content).getChapter());
        vc.put("verseStart", ((Scripture) content).getFromVerse());
        vc.put("verseEnd", ((Scripture) content).getToVerse());

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_scripture_header.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    private Shape createScriptureBodyShape(GenericSlideContent content) throws JAXBException {
        VelocityContext vc = new VelocityContext();
        vc.put("biblePartFragments", ((Scripture) content).getBiblePart());

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_scripture_body.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    private Shape createVicarShape(Object vicarName) throws JAXBException {
        VelocityContext vc = new VelocityContext();
        vc.put("vicarName", vicarName);

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_welcome.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    private Shape createTimeShape(String time) throws JAXBException {
        VelocityContext vc = new VelocityContext();
        vc.put("time", time);

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_end_morning_service_time.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    private Shape createNextVicarShape(String vicarName) throws JAXBException {
        VelocityContext vc = new VelocityContext();
        vc.put("vicarName", vicarName);

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_end_morning_service_vicar.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    /**
     * 
     * @param textContent
     * @return
     * @throws JAXBException
     */
    private Shape createSongHeaderShape(Song song) throws JAXBException {

        // separate the header for easier handling below
        String songNumber = StringUtils.substringBefore(song.getHeader(), ":");
        String verses = StringUtils.substringAfter(song.getHeader(), ":");

        VelocityContext vc = new VelocityContext();
        vc.put("title_text_before", songNumber + (StringUtils.isEmpty(song.getCurrentVerse()) ? "" : ":") + StringUtils.substringBefore(verses, song.getCurrentVerse()));
        vc.put("title_text_bold", song.getCurrentVerse());
        vc.put("title_text_after", StringUtils.substringAfter(verses, song.getCurrentVerse()));

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_song_header.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    /**
     * 
     * @param lines
     * @return
     * @throws JAXBException
     */
    private Shape createSongBody(String lines) throws JAXBException {
        VelocityContext vc = new VelocityContext();

        // transform lines String into array of lines
        List<String> lineList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(lines, System.getProperty("line.separator"));
        while (st.hasMoreTokens()) {
            lineList.add(st.nextToken());
        }

        vc.put("lines", lineList);

        StringWriter ow = new StringWriter();

        getVelocityEngine().getTemplate("/templates/shape_song_body.vc", ENCODING).merge(vc, ow);

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    private Shape createGatheringShape(String benificiary, Boolean firstBenificiary) throws JAXBException {
        VelocityContext vc = new VelocityContext();
        vc.put("benificiary", benificiary);

        StringWriter ow = new StringWriter();

        if (firstBenificiary) {
            getVelocityEngine().getTemplate("/templates/shape_gathering_firstbenificiary.vc", ENCODING).merge(vc, ow);
        } else {
            getVelocityEngine().getTemplate("/templates/shape_gathering_secondbenificiary.vc", ENCODING).merge(vc, ow);
        }

        Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
        return shape;
    }

    /**
     * 
     * @param targetPosition
     * @return
     * @throws InvalidFormatException
     * @throws JAXBException
     */
    public SlidePart createSlide(int targetPosition) throws InvalidFormatException, JAXBException {
        // OK, now we can create a slide
        SlidePart slidePart = new SlidePart(new PartName(String.format("/ppt/slides/slide%s.xml", targetPosition)));
        slidePart.setContents(SlidePart.createSld());
        return slidePart;
    }

    /**
     * 
     * @param presentationMLPackage
     * @return
     */
    public MainPresentationPart getMainPresentationPart(PresentationMLPackage presentationMLPackage) {
        try {
            return (MainPresentationPart) presentationMLPackage.getParts().getParts().get(new PartName("/ppt/presentation.xml"));
        } catch (InvalidFormatException e) {
            // TODO print human fiendly message
            System.err.println(e.getMessage());
        }
        return null;
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

}
