package org.gkvassenpeelo.liedbase.songbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gkvassenpeelo.liedbase.LiedBase;
import org.gkvassenpeelo.liedbase.liturgy.SlideContents;

public class SongBook {

	static final Logger logger = Logger.getLogger(SongBook.class);

	/**
	 * 
	 * @param type
	 * @param songNumber
	 * @param verse
	 * @return
	 */
	public static String getSongText(SlideContents.Type type, String songNumber, String verse) {

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

		Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiedBase.ENCODING);

		while (s.hasNextLine()) {

			String line = s.nextLine();

			if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
				// we have the line number on which the song starts
				// continue reading from that line again until we end up on
				// the right verse
				while (s.hasNextLine()) {
					String songLine = s.nextLine();

					if (songLine.equals(verse)) {
						StringBuilder verseText = new StringBuilder();
						while (s.hasNextLine()) {
							String verseLine = s.nextLine();
							if (StringUtils.isEmpty(verseLine)) {
								s.close();
								return verseText.toString();
							}
							verseText.append(verseLine);
							verseText.append(System.getProperty("line.separator"));
						}
						s.close();
						return verseText.toString();
					}
				}
			}
		}

		s.close();

		return String.format("Geen tekst gevonden voor %s %s: %s", type.toString(), songNumber, verse);
	}

	public static List<String> getOpwekkingSongTekst(String songNumber) {
		List<String> verses = new ArrayList<String>();

		String songBookName = "opwekking.txt";
		String songIdentifier = "opwekking";

		Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiedBase.ENCODING);

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
					// "Ned. tekst  arr.: Opwekking".equals(songLine)) {
					// continue;
					// }

					boolean nextSong = false;
					StringBuilder verseText = new StringBuilder();

					while (s.hasNextLine() && !nextSong) {

						// read next line
						String verseLine = s.nextLine();

						// check to see if we are not reading too far
						nextSong = verseLine.matches(String.format("^%s %s$", songIdentifier, Integer.parseInt(songNumber) + 1));

						if (StringUtils.isEmpty(verseLine)) {
							if (!StringUtils.isEmpty(verseText.toString()) && !StringUtils.startsWith(verseText.toString(), "Tekst  muziek:")
									&& !StringUtils.startsWith(verseText.toString(), "(c)")) {
								verses.add(verseText.toString());
							}
							verseText = new StringBuilder();
							continue;
						}
						verseText.append(verseLine);
						verseText.append(System.getProperty("line.separator"));
					}

					return verses;
				}

				s.close();
				return verses;
			}
		}

		s.close();
		return verses;
	}

	public static List<String> getVersesFromSong(org.gkvassenpeelo.liedbase.liturgy.SlideContents.Type type, String songNumber) {
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

		Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("songs/" + songBookName), LiedBase.ENCODING);

		while (s.hasNextLine()) {

			String line = s.nextLine();

			if (line.matches(String.format("^%s %s:.*$", songIdentifier, songNumber))) {
				// we have the line number on which the song starts
				// continue reading from that line until we have all verses
				while (s.hasNextLine()) {
					String songLine = s.nextLine();

					// we are reading the next song, stop it!
					if (songLine.matches(String.format("^%s %s:.*$", songIdentifier, Integer.parseInt(songNumber) + 1))) {
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
