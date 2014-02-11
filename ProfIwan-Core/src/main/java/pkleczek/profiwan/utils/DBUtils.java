package pkleczek.profiwan.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import pkleczek.profiwan.model.PhraseEntry;
import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;
import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

public class DBUtils {

	private static final CSV csv = CSV.separator(';').quote('\'').skipLines(0)
			.charset("UTF-8").create();

	/**
	 * Exports given phrases to a CSV format. Apart from basic data (language
	 * codes and texts) also provided extra attributes are stored.
	 * 
	 * @param phrases
	 * @param attributes
	 * @return
	 */
	public static void exportPhrasesToCSV(String filename,
			final Collection<PhraseEntry> phrases, final String[] attributes) {

		csv.write(filename, new CSVWriteProc() {
			public void process(CSVWriter out) {
				out.writeNext(attributes);

				for (PhraseEntry p : phrases) {
					out.writeNext(p.toCSVTokens(attributes));
				}
			}
		});
	}

	/**
	 * 
	 * @param filename
	 * @param attributes
	 *            optional filtering attributes
	 */
	public static void importPhrasesFromCSV(String filename,
			final Set<String> attributes, final DatabaseHelper dbHelper) {
		
		csv.read(filename, new CSVReadProc() {
			String[] orderedAttribs;
			Set<String> fileAttributes = new HashSet<String>();

			// Attributes stored in the file AND wanted by the user.
			Set<String> workingSet = new HashSet<String>();

			Map<String, String> phraseData = new HashMap<String, String>();

			public void procRow(int rowIndex, String... values) {
				if (rowIndex == 0) {
					orderedAttribs = values;
					fileAttributes.addAll(Arrays.asList(values));
					workingSet.addAll(fileAttributes);
					workingSet.retainAll(attributes);
				} else {
					
					for (int i = 0; i < fileAttributes.size(); i++) {
						if (!workingSet.contains(orderedAttribs[i])) {
							continue;
						}
						
						phraseData.put(orderedAttribs[i], values[i]);
					}
					
					PhraseEntry p = new PhraseEntry();

					p.fromCSVTokens(phraseData);

					if (p.getCreatedAt() == null) {
						p.setCreatedAt(DateTime.now());
					}
					
					dbHelper.createPhrase(p);
				}
			}
		});

	}

	public static int getIntFromDateTime(DateTime dt) {
		return (int) (dt.getMillis() / 1000);
	}

	public static DateTime getDateTimeFromInt(int i) {
		return new DateTime((long) i * 1000L);
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
}
