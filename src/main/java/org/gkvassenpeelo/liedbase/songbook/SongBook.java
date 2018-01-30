package org.gkvassenpeelo.liedbase.songbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.liturgy.LiturgyModel;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;

public class SongBook {

	static final Logger logger = Logger.getLogger(SongBook.class);

	/**
	 * 
	 * @param type
	 * @param songNumber
	 * @param verse
	 * @return
	 * @throws SongBookException
	 */
	public static List<SongLine> getSongText(SlideContents.Type type, String songNumber, String verse)
			throws SongBookException {

		List<SongLine> songText = new ArrayList<SongLine>();

		String songBookName = "";
		String songIdentifier = "";

		if (type == SlideContents.Type.psalm) {
			songBookName = "psalmen.txt";
			songIdentifier = "psalm";
		}

		if (type == SlideContents.Type.gezang) {
			songBookName = "gezangen.txt";
			songIdentifier = "gereformeerd kerkboek";
		}

		if (type == SlideContents.Type.lied) {
			songBookName = "liedboek.txt";
			songIdentifier = "lied";
		}

		if (type == SlideContents.Type.levenslied) {
			songBookName = "levensliederen.txt";
			songIdentifier = "levenslied";
		}

		Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiturgyModel.ENCODING);

		int songInteger = 0;
		int verseInteger = 0;
		String songNumberPostfix = "";

		try {
			verseInteger = Integer.parseInt(verse);
		} catch (NumberFormatException e) {
			s.close();
			throw new SongBookException(String.format("'%s' is geen geldig nummer", verse));
		}

		if (songNumber.matches("^[0-9]+$")) {
			songInteger = Integer.parseInt(songNumber);
		} else if (songNumber.matches("^[0-9]+[a-z]{1}$")) {
			songInteger = Integer.parseInt(songNumber.substring(0, songNumber.length() - 1));
			songNumberPostfix = songNumber.substring(songNumber.length() - 1, songNumber.length());
		}

		while (s.hasNextLine()) {

			String line = s.nextLine();

			if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
				// we have the line number on which the song starts
				// continue reading from that line again until we end up on
				// the right verse
				while (s.hasNextLine()) {

					line = s.nextLine();

					// reading next song, return
					if (readingNextSong(songInteger, songNumberPostfix, line)) {
						s.close();
						return cleanup(songText);
					}

					int index = 0;
					if (line.equals(verse)) {
						while (s.hasNextLine()) {
							line = s.nextLine();
							// reading next verse, return
							// check for next verse AND next song!
							if (line.equals("" + (verseInteger + 1))
									|| readingNextSong(songInteger, songNumberPostfix, line)) {
								s.close();
								return cleanup(songText);
							}

							if (index == 0) {
								songText.add(new SongLine(SongLine.DisplayType.first_line, line));
							} else {
								songText.add(new SongLine(SongLine.DisplayType.normal, line));
							}

							index++;
						}
						s.close();

						return cleanup(songText);
					}
				}
			}
		}

		s.close();

		// no text found
		return null;
	}

	static boolean readingNextSong(int songInteger, String songNumberPostfix, String line) {

		Pattern pattern = Pattern.compile("^[a-zA-Z ]+([0-9]+)([a-z]*):");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {

			int nextSong = Integer.parseInt(matcher.group(1));
			String nextSongPostfix = matcher.group(2);

			if (songInteger + 1 <= nextSong) {
				return true;
			}

			if (songInteger == nextSong && songNumberPostfix != null && nextSongPostfix != null
					&& songNumberPostfix.charAt(0) <= nextSongPostfix.charAt(0)) {
				return true;
			}
		}

		return false;
	}

	// private boolean readingNextSong(String line)

	private static List<SongLine> cleanup(List<SongLine> songText) {

		if (songText.size() > 0) {
			if (songText.get(songText.size() - 1).getContent().equals("")) {
				songText.remove(songText.size() - 1);
			}

			return songText;

		} else {
			return null;
		}

	}

	public static List<List<SongLine>> getOpwekkingSongTekst(String songNumber) throws SongBookException {
		List<List<SongLine>> verses = new ArrayList<List<SongLine>>();

		String songBookName = "opwekking.txt";
		String songIdentifier = "opwekking";

		Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiturgyModel.ENCODING);

		while (s.hasNextLine()) {

			String line = s.nextLine();

			if (line.matches(String.format("^%s %s$", songIdentifier, songNumber))) {
				// we have the line number on which the song starts
				// continue reading from that line again until we have all song
				// parts

				// do two extra readlines (into oblivion) so the songtitle and
				// the blank line thereafter are skipped
				s.nextLine();
				s.nextLine();

				while (s.hasNextLine()) {

					// if (StringUtils.isEmpty(songLine) ||
					// StringUtils.startsWith(songLine, "(c)") ||
					// "Ned. tekst arr.: Opwekking".equals(songLine)) {
					// continue;
					// }

					boolean nextSong = false;
					List<SongLine> verseText = new ArrayList<SongLine>();

					while (s.hasNextLine() && !nextSong) {

						// read next line
						String verseLine = s.nextLine();

						// check to see if we are not reading too far
						nextSong = verseLine
								.matches(String.format("^%s %s$", songIdentifier, Integer.parseInt(songNumber) + 1));

						if (StringUtils.isEmpty(verseLine)) {
							if (!StringUtils.isEmpty(verseText.toString())
									&& !StringUtils.startsWith(verseText.toString(), "Tekst  muziek:")
									&& !StringUtils.startsWith(verseText.toString(), "(c)")) {
								verses.add(verseText);
							}
							verseText = new ArrayList<SongLine>();
							continue;
						}
						verseText.add(new SongLine(SongLine.DisplayType.normal, verseLine));
					}

					// add the last verse/line, unless the line is the header of
					// the next song
					if (!nextSong) {
						verses.add(verseText);
					}
					s.close();
					return verses;
				}

				s.close();
				return verses;
			}
		}

		// song not found
		s.close();
		throw new SongBookException("Opwekking " + songNumber + " niet gevonden");
	}

	public static List<String> getVersesFromSong(SlideContents.Type type, String songNumber) {
		List<String> verses = new ArrayList<String>();

		String songBookName = "";
		String songIdentifier = "";

		if (type == SlideContents.Type.psalm) {
			songBookName = "psalmen.txt";
			songIdentifier = "psalm";
		}

		if (type == SlideContents.Type.gezang) {
			songBookName = "gezangen.txt";
			songIdentifier = "gereformeerd kerkboek";
		}

		if (type == SlideContents.Type.lied) {
			songBookName = "liedboek.txt";
			songIdentifier = "lied";
		}

		if (type == SlideContents.Type.levenslied) {
			songBookName = "levensliederen.txt";
			songIdentifier = "levenslied";
		}

		Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiturgyModel.ENCODING);

		while (s.hasNextLine()) {

			String line = s.nextLine();

			if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
				// we have the line number on which the song starts
				// continue reading from that line until we have all verses
				while (s.hasNextLine()) {
					String songLine = s.nextLine();

					// we are reading the next song, stop!
					String tempSongNumber = songNumber;
					if (tempSongNumber.matches("^[0-9]*[a-z]{1}$")) {
						tempSongNumber = songNumber.substring(0, songNumber.length() - 1);
					}
					// next song in integers
					if (songLine.matches(
							String.format("^%s %s[a-z]*:?.*$", songIdentifier, Integer.parseInt(tempSongNumber) + 1))) {
						s.close();
						return verses;
					}
					// next song alphabetically
					if (songLine.matches(
							String.format("^%s %s[a-z]*:.*$", songIdentifier, Integer.parseInt(tempSongNumber)))) {
						s.close();
						return verses;
					}

					try {
						Integer.parseInt(songLine);
						verses.add(songLine);
					} catch (Exception e) {
						// do nothing
					}
				}
			}
		}

		s.close();

		return verses;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	public static String getSongNumber(String line) {
		if (line.contains(":")) {
			return StringUtils.substringBetween(line, " ", ":").trim();
		} else {
			return StringUtils.substringAfter(line, " ");
		}
	}

}
