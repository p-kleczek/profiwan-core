package pkleczek.profiwan.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import pkleczek.profiwan.utils.DatabaseHelper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class RevisionsSession {

	@SuppressWarnings("unused")
	private static final String LOG_TAG = RevisionsSession.class.getName();

	/**
	 * List of phrases pending for revision.
	 */
	private LinkedList<PhraseEntry> pendingPhrases = new LinkedList<PhraseEntry>();

	/**
	 * Maps phrase's ID on a corresponding revision entry.
	 */
	private Map<Long, RevisionEntry> revisionEntries = new HashMap<Long, RevisionEntry>();

	/**
	 * Cyclic iterator over pending phrases.
	 */
	private Iterator<PhraseEntry> pendingRevisionsIterator;

	/**
	 * Currently revised phrase.
	 */
	private PhraseEntry currentPhrase = null;

	/**
	 * Total number of revised phrases.
	 */
	private int wordsNumber = 0;

	/**
	 * Total number of correctly typed phrases so far.
	 */
	private int correctWordsNumber = 0;

	/**
	 * Total number of revisions in this session so far (including repetitions).
	 */
	private int revisionsNumber = 0;

	private DatabaseHelper dbHelper;

	public RevisionsSession(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;

		initialize();
	}

	private void initialize() {
		buildUpListOfPendingPhrases();

		if (hasRevisions()) {
			nextRevision();
		}
	}

	private void buildUpListOfPendingPhrases() {
		List<PhraseEntry> dictionary = dbHelper.getDictionary();

		for (PhraseEntry pe : dictionary) {
			if (pe.isReviseNow()) {
				pendingPhrases.add(pe);
				revisionEntries.put(pe.getId(), prepareRevisionEntry(pe));
			}
		}

		Collections.shuffle(pendingPhrases);
		pendingRevisionsIterator = Iterators.cycle(pendingPhrases);

		wordsNumber = pendingPhrases.size();
	}

	private RevisionEntry prepareRevisionEntry(PhraseEntry phrase) {
		RevisionEntry newRevision = new RevisionEntry();
		newRevision.setCreatedAt(DateTime.now());

		RevisionEntry lastRevision = Iterables.getLast(phrase.getRevisions(),
				null);

		boolean isToBeContinued = lastRevision != null
				&& lastRevision.isToContinue();

		return isToBeContinued ? lastRevision : newRevision;
	}

	public boolean hasRevisions() {
		return !pendingPhrases.isEmpty();
	}

	public boolean isEnteredCorrectly(CharSequence input) {
		return currentPhrase.getLangBText().equals(input);
	}

	public boolean processTypedWord(String input) {
		boolean enteredCorrectly = isEnteredCorrectly(input);

		RevisionEntry re = revisionEntries.get(currentPhrase.getId());

		// Revisions for the first time today.
		if (re.getMistakes() == 0) {
			dbHelper.createRevision(re, currentPhrase.getId());
		}

		if (enteredCorrectly) {
			acceptRevision();
		} else {
			re.nextMistake();
		}

		dbHelper.updateRevision(re);

		return enteredCorrectly;
	}

	public PhraseEntry getCurrentPhrase() {
		return currentPhrase;
	}

	/**
	 * Marks current revision as correct (regardless of the actual value of the
	 * input).
	 */
	public void acceptRevision() {
		RevisionEntry re = revisionEntries.get(currentPhrase.getId());
		re.enteredCorrectly();

		revisionEntries.remove(currentPhrase.getId());
		pendingRevisionsIterator.remove();

		correctWordsNumber++;
	}

	/**
	 * Sets new text for revised language text.
	 * 
	 * @param newText
	 *            new text
	 */
	public void editPhrase(String newText) {
		currentPhrase.setLangBText(newText);
		dbHelper.updatePhrase(currentPhrase);
	}

	public int getWordsNumber() {
		return wordsNumber;
	}

	public int getCorrectWordsNumber() {
		return correctWordsNumber;
	}

	public int getRevisionsNumber() {
		return revisionsNumber;
	}

	public int getPendingRevisionsSize() {
		return pendingPhrases.size();
	}

	/**
	 * Proceeds to the next phrase.
	 */
	public void nextRevision() {
		if (!hasRevisions()) {
			throw new AssertionError();
		}

		currentPhrase = pendingRevisionsIterator.next();
		revisionsNumber++;
	}

}
