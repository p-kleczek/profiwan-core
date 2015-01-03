package pkleczek.profiwan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.TextUtils;

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

	/**
	 * blad => reset postepow we FREQ do podanego ulamka
	 */
	public static double MISTAKE_MULTIPLIER = 0.5;

	/**
	 * Minimum number of days between two consecutive revisions.
	 */
	public static int MIN_REVISION_INTERVAL = 2;

	/**
	 * Numbers of initial consecutive correct revisions before its frequency
	 * starts to fall.
	 */
	public static int MIN_CORRECT_STREAK = 3;

	/**
	 * Maximum number of days between two consecutive revisions.
	 */
	public static int MAX_REVISION_INTERVAL = 30;

	/**
	 * Change in revisions' frequency with each correct revision.
	 */
	public static int FREQUENCY_DECAY = 2;

	/**
	 * Makes revision frequency vary +/- n% from its initial value to prevent
	 * the stacking effect of revisions made on the same day.
	 */
	public static double COUNTER_STACKING_FACTOR = 0.1;

	public RevisionsSession(DatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;

		initialize();
	}

	private void initialize() {
		DateTime todayMidnight = DateTime.now().withTimeAtStartOfDay();
		pendingPhrases.addAll(getListOfPendingPhrases(dbHelper, todayMidnight));

		setupEnvironmentVariables();

		if (hasRevisions()) {
			nextRevision();
		}
	}

	public static List<PhraseEntry> getListOfPendingPhrases(
			DatabaseHelper dbHelper, DateTime dueDate) {
		List<PhraseEntry> dictionary = dbHelper.getDictionary();
		List<PhraseEntry> pending = new ArrayList<PhraseEntry>();

		for (PhraseEntry pe : dictionary) {
			if (isReviseNow(pe, dueDate)) {
				pending.add(pe);
			}
		}

		return pending;
	}

	private void setupEnvironmentVariables() {

		for (PhraseEntry pe : pendingPhrases) {
			revisionEntries.put(pe.getId(), prepareRevisionEntry(pe));
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
		String model = TextUtils.getUnaccentedString(currentPhrase
				.getLangBText());
		return model.equals(input);
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
			dbHelper.updateRevision(re);
		}

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
		dbHelper.updateRevision(re);

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

	private static int getRevisionFrequency(PhraseEntry pe) {
		int freq = MIN_REVISION_INTERVAL;
		int correctStreak = 0;
		boolean isInitialStreak = false;

		// Frequency when the streak began.
		int freqStreakBegin = -1;

		for (int i = 0; i < pe.getRevisions().size(); i++) {
			RevisionEntry e = pe.getRevisions().get(i);

			if (e.getMistakes() == 0) {
				if (correctStreak == 0) {
					freqStreakBegin = freq;
				}

				if (isInitialStreak) {
					freq += FREQUENCY_DECAY + correctStreak;
				}

				correctStreak++;

				if (!isInitialStreak && correctStreak == MIN_CORRECT_STREAK) {
					isInitialStreak = true;
					// correctStreak = 0; // FIXME: bez sensu tu zerowac, skoro
					// potem mnozymy to i odejmujemy od freq!
				}
			} else {
				if (isInitialStreak) {
					freq -= (freq - freqStreakBegin) * MISTAKE_MULTIPLIER;

					// clamp
					freq = Math.min(freq, MAX_REVISION_INTERVAL);
					freq = Math.max(freq, MIN_REVISION_INTERVAL);
				}
				correctStreak = 0;
			}
		}

		return freq;
	}

	private static boolean isReviseNow(PhraseEntry pe, DateTime dueDate) {

		if (!pe.isInRevisions()) {
			return false;
		}

		if (pe.getRevisions().isEmpty()) {
			return true;
		}

		RevisionEntry lastRevision = pe.getRevisions().get(
				pe.getRevisions().size() - 1);
		if (lastRevision.isToContinue()) {
			return true;
		}

		int freq = getRevisionFrequency(pe);

		// Modify frequency to prevent stacking of revisions made on the same
		// day.
		freq *= (1.0 - RevisionsSession.COUNTER_STACKING_FACTOR)
				+ Math.random()
				* (RevisionsSession.COUNTER_STACKING_FACTOR / 2.0);
		freq = Math.max(freq, RevisionsSession.MIN_REVISION_INTERVAL);
		freq = Math.min(freq, RevisionsSession.MAX_REVISION_INTERVAL);

		DateTime nextRevisionDate = lastRevision.getCreatedAt().plusDays(freq)
				.withTimeAtStartOfDay();

		return !nextRevisionDate.isAfter(dueDate);
	}
}
