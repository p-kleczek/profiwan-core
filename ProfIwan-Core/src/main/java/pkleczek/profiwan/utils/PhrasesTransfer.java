package pkleczek.profiwan.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import pkleczek.profiwan.model.PhraseEntry;
import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;
import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

public final class PhrasesTransfer {

	private PhrasesTransfer() {
	}

	private static final CSV csv = CSV.separator(',').quote('\"').escape('|').skipLines(0)
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
	public static Collection<PhraseEntry> importPhrasesFromCSV(String filename,
			final Set<String> attributes, final DatabaseHelper dbHelper) {

		final List<PhraseEntry> phrasesRead = new ArrayList<PhraseEntry>();

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

					for (Iterator<String> it = workingSet.iterator(); it
							.hasNext();) {
						String s = it.next();
						if (!attributes.contains(s)) {
							it.remove();
						}
					}

					workingSet.retainAll(attributes);

					if (workingSet.isEmpty()) {
						throw new AssertionError(
								"Set of attributes cannot be empty!");
					}
				} else {

					for (int i = 0; i < fileAttributes.size(); i++) {
						if (!workingSet.contains(orderedAttribs[i])) {
							continue;
						}

						String value = TextUtils.getAccentedString(values[i]);

						phraseData.put(orderedAttribs[i], value);
					}

					// XXX: in the furute - make data processing like reducing accents here (not in the loop) 

					PhraseEntry p = new PhraseEntry();

					p.fromCSVTokens(phraseData);

					if (p.getCreatedAt() == null) {
						p.setCreatedAt(DateTime.now());
					}

					phrasesRead.add(p);
				}
			}
		});

		return phrasesRead;
	}

	public static Collection<PhraseEntry> importIntoDatabase(
			DatabaseHelper dbHelper, Collection<PhraseEntry> toImport) {

		List<PhraseEntry> dictionary = dbHelper.getDictionary();

		// Imported phrases checked for conflicts (OK).
		List<PhraseEntry> checked = new ArrayList<PhraseEntry>();

		for (Iterator<PhraseEntry> it = toImport.iterator(); it.hasNext();) {
			boolean isCollision = false;
			PhraseEntry p = it.next();

			for (PhraseEntry c : checked) {
				isCollision = PhraseEntry.isCollision(p, c);
				if (isCollision) {
					break;
				}
			}

			for (PhraseEntry d : dictionary) {
				isCollision = PhraseEntry.isCollision(p, d);
				if (isCollision) {
					break;
				}
			}

			if (!isCollision) {
				dbHelper.createPhrase(p);
				checked.add(p);
				it.remove();
			}
		}

		return toImport;
	}
}
