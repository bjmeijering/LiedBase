package org.gkvassenpeelo.liedbase.slidemachine;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.gkvassenpeelo.liedbase.liturgy.EndOfMorningService;
import org.gkvassenpeelo.liedbase.liturgy.Gathering;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyOverview;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyPart;
import org.gkvassenpeelo.liedbase.liturgy.Scripture;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;
import org.gkvassenpeelo.liedbase.liturgy.Song;
import org.gkvassenpeelo.liedbase.liturgy.Welcome;
import org.gkvassenpeelo.liedbase.songbook.SongLine;
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

	private Map<LiturgyPart.Type, String> slideLayoutMap = new HashMap<LiturgyPart.Type, String>();

	private VelocityEngine velocityEngine;

	private static final String ENCODING = "UTF-8";

	/**
     * 
     */
	public SlideFactory() {
		// fill the slide layout map
		slideLayoutMap.put(LiturgyPart.Type.song, "/ppt/slideLayouts/slideLayout4.xml");
		slideLayoutMap.put(LiturgyPart.Type.scripture, "/ppt/slideLayouts/slideLayout4.xml");
		slideLayoutMap.put(LiturgyPart.Type.blank, "/ppt/slideLayouts/slideLayout19.xml");
		slideLayoutMap.put(LiturgyPart.Type.gathering, "/ppt/slideLayouts/slideLayout6.xml");
		slideLayoutMap.put(LiturgyPart.Type.prair, "/ppt/slideLayouts/slideLayout10.xml");
		slideLayoutMap.put(LiturgyPart.Type.welcome, "/ppt/slideLayouts/slideLayout1.xml");
		slideLayoutMap.put(LiturgyPart.Type.law, "/ppt/slideLayouts/slideLayout12.xml");
		slideLayoutMap.put(LiturgyPart.Type.votum, "/ppt/slideLayouts/slideLayout3.xml");
		slideLayoutMap.put(LiturgyPart.Type.amen, "/ppt/slideLayouts/slideLayout9.xml");
		slideLayoutMap.put(LiturgyPart.Type.endOfMorningService, "/ppt/slideLayouts/slideLayout7.xml");
		slideLayoutMap.put(LiturgyPart.Type.endOfAfternoonService, "/ppt/slideLayouts/slideLayout8.xml");
		slideLayoutMap.put(LiturgyPart.Type.lecture, "/ppt/slideLayouts/slideLayout14.xml");
		slideLayoutMap.put(LiturgyPart.Type.agenda, "/ppt/slideLayouts/slideLayout2.xml");
		slideLayoutMap.put(LiturgyPart.Type.liturgyOverview, "/ppt/slideLayouts/slideLayout20.xml");
		slideLayoutMap.put(LiturgyPart.Type.extendedScripture, "/ppt/slideLayouts/slideLayout5.xml");
		slideLayoutMap.put(LiturgyPart.Type.emptyWithLogo, "/ppt/slideLayouts/slideLayout19.xml");
		slideLayoutMap.put(LiturgyPart.Type.video, "/ppt/slideLayouts/slideLayout21.xml");
		slideLayoutMap.put(LiturgyPart.Type.schoonmaak, "/ppt/slideLayouts/slideLayout22.xml");

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
	public void addSlideContents(PresentationMLPackage presentationMLPackage, SlidePart slidePart, SlideContents content, LiturgyPart.Type type) throws JAXBException,
			Docx4JException {
		SlideLayoutPart layoutPart = (SlideLayoutPart) presentationMLPackage.getParts().getParts().get(new PartName(slideLayoutMap.get(type)));

		// add Slide layout part
		slidePart.addTargetPart(layoutPart);

		// Add contents to layout if neccesary
		if (type == LiturgyPart.Type.song) {
			// Create and add header
			if (!StringUtils.isEmpty(content.getHeader())) {
				Shape header = createSongHeaderShape((Song) content);
				slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(header);
			}

			// create and add body
			if (((Song) content).getSongText().size() > 0) {
				Shape body = createSongBody(((Song) content).getSongText());
				slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(body);
			}
		}
		if (type == LiturgyPart.Type.gathering) {
			Shape benificiary = createGatheringShape(((Gathering) content).getFirstGatheringBenificiary(), true);
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(benificiary);
			benificiary = createGatheringShape(((Gathering) content).getSecondGatheringBenificiary(), false);
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(benificiary);
		}
		if (type == LiturgyPart.Type.welcome) {
			Shape vicar = createVicarShape(((Welcome) content).getVicarName());
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(vicar);
		}
		if (type == LiturgyPart.Type.endOfMorningService) {

			Shape time = createTimeShape(((EndOfMorningService) content).getTime());
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(time);

			Shape vicar = createNextVicarShape(((EndOfMorningService) content).getVicarName());
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(vicar);
		}
		if (type == LiturgyPart.Type.scripture) {

			Shape scriptureHeader = createScriptureHeaderShape(content);
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(scriptureHeader);

			Shape scriptureBody = createScriptureBodyShape(content);
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(scriptureBody);

		}
		if (type == LiturgyPart.Type.agenda) {
		    
		    slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(createAgendaShape());
		    
		}
		if (type == LiturgyPart.Type.schoonmaak) {

			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(createSchoonmaakShape());

		}
		if (type == LiturgyPart.Type.liturgyOverview) {
			slidePart.getContents().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(getLiturgyOverviewBody(content));
		}

	}

	private Shape getLiturgyOverviewBody(SlideContents content) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("pastParts", ((LiturgyOverview) content).getLiturgyLinesPast());
		vc.put("futureParts", ((LiturgyOverview) content).getLiturgyLinesFuture());

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_liturgy_overview_body.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}
	
	private CTGraphicalObjectFrame createAgendaShape() throws JAXBException {
	    VelocityContext vc = new VelocityContext();
	    
	    StringWriter ow = new StringWriter();
	    
	    getVelocityEngine().getTemplate("/templates/pptx/shape_agenda_table.vc", ENCODING).merge(vc, ow);
	    
	    return (CTGraphicalObjectFrame) XmlUtils.unmarshalString(ow.toString(), Context.jcPML, CTGraphicalObjectFrame.class);
	}

	private CTGraphicalObjectFrame createSchoonmaakShape() throws JAXBException {
		VelocityContext vc = new VelocityContext();

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_schoonmaak.vc", ENCODING).merge(vc, ow);

		return (CTGraphicalObjectFrame) XmlUtils.unmarshalString(ow.toString(), Context.jcPML, CTGraphicalObjectFrame.class);
	}

	private Shape createScriptureHeaderShape(SlideContents content) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("bibleBook", ((Scripture) content).getBibleBook());
		vc.put("chapter", ((Scripture) content).getChapter());
		vc.put("verseStart", ((Scripture) content).getFromVerse());
		vc.put("verseEnd", ((Scripture) content).getToVerse());
		if (!((Scripture) content).getTranslation().equals("NBV")) {
			vc.put("translation", "(" + ((Scripture) content).getTranslation() + ")");
		} else {
			vc.put("translation", "");
		}

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_scripture_header.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}

	private Shape createScriptureBodyShape(SlideContents content) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("biblePartFragments", ((Scripture) content).getBiblePart());

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_scripture_body.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}

	private Shape createVicarShape(Object vicarName) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("vicarName", vicarName);

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_welcome.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}

	private Shape createTimeShape(String time) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("time", time);

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_end_morning_service_time.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}

	private Shape createNextVicarShape(String vicarName) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("vicarName", vicarName);

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_end_morning_service_vicar.vc", ENCODING).merge(vc, ow);

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
		vc.put("title_text_before", songNumber + (StringUtils.isEmpty(song.getVerseNumber()) ? "" : ":") + StringUtils.substringBefore(verses, song.getVerseNumber()));
		vc.put("title_text_bold", song.getVerseNumber());
		vc.put("title_text_after", StringUtils.substringAfter(verses, song.getVerseNumber()));

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_song_header.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}

	/**
	 * 
	 * @param lines
	 * @return
	 * @throws JAXBException
	 */
	private Shape createSongBody(List<SongLine> lines) throws JAXBException {
		VelocityContext vc = new VelocityContext();

		vc.put("lines", lines);

		StringWriter ow = new StringWriter();

		getVelocityEngine().getTemplate("/templates/pptx/shape_song_body.vc", ENCODING).merge(vc, ow);

		Shape shape = ((Shape) XmlUtils.unmarshalString(ow.toString(), Context.jcPML));
		return shape;
	}

	private Shape createGatheringShape(String benificiary, Boolean firstBenificiary) throws JAXBException {
		VelocityContext vc = new VelocityContext();
		vc.put("benificiary", benificiary);

		StringWriter ow = new StringWriter();

		if (firstBenificiary) {
			getVelocityEngine().getTemplate("/templates/pptx/shape_gathering_firstbenificiary.vc", ENCODING).merge(vc, ow);
		} else {
			getVelocityEngine().getTemplate("/templates/pptx/shape_gathering_secondbenificiary.vc", ENCODING).merge(vc, ow);
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
