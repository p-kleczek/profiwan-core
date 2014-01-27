package pkleczek.profiwan.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * The <code>PhraseEntry</code> class stores all the information about a phrase
 * (ie. translations, revisions).
 * 
 * @author Pawel
 * 
 */
public class PhraseEntry {

	/**
	 * Makes revision frequency vary +/- n% from its initial value to prevent
	 * the stacking effect of revisions made on the same day.
	 */
	public static double COUNTER_STACKING_FACTOR = 0.1;

	/**
	 * Change in revisions' frequency with each correct revision.
	 */
	public static int FREQUENCY_DECAY = 2;

	/**
	 * Maximum number of days between two consecutive revisions.
	 */
	public static int MAX_REVISION_INTERVAL = 30;

	/**
	 * Numbers of initial consecutive correct revisions before its frequency
	 * starts to fall.
	 */
	public static int MIN_CORRECT_STREAK = 3;

	/**
	 * Minimum number of days between two consecutive revisions.
	 */
	public static int MIN_REVISION_INTERVAL = 1;

	/**
	 * blad => reset postepow we FREQ do podanego ulamka
	 */
	public static double MISTAKE_MULTIPLIER = 0.5;

	private DateTime createdAt = null;

	/**
	 * ID as in database.
	 */
	private long id;

	/**
	 * <code>true</code> if the phrase is currently revised
	 */
	private boolean inRevisions = false;

	private String label = ""; //$NON-NLS-1$
	/**
	 * ISO code of the first language.
	 */
	private String langA = "";
	private String langAText = ""; //$NON-NLS-1$
	/**
	 * ISO code of the second language.
	 */
	private String langB = "";

	private String langBText = ""; //$NON-NLS-1$

	private List<RevisionEntry> revisions = new ArrayList<RevisionEntry>();

	public DateTime getCreatedAt() {
		return createdAt;
	}

	public long getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getLangA() {
		return langA;
	}

	public String getLangAText() {
		return langAText;
	}

	public String getLangB() {
		return langB;
	}

	public String getLangBText() {
		return langBText;
	}

	public int getRevisionFrequency() {
		int freq = MIN_REVISION_INTERVAL;
		int correctStreak = 0;
		boolean isInitialStreak = false;

		for (int i = 0; i < revisions.size(); i++) {
			RevisionEntry e = revisions.get(i);

			if (e.getMistakes() == 0) {
				if (isInitialStreak) {
					freq += FREQUENCY_DECAY;
				}

				correctStreak++;

				if (!isInitialStreak && correctStreak == MIN_CORRECT_STREAK) {
					isInitialStreak = true;
					correctStreak = 0;
				}
			} else {
				if (isInitialStreak) {
					freq -= correctStreak * FREQUENCY_DECAY
							* MISTAKE_MULTIPLIER;

					// clamp
					freq = Math.min(freq, MAX_REVISION_INTERVAL);
					freq = Math.max(freq, MIN_REVISION_INTERVAL);
				}
				correctStreak = 0;
			}
		}

		return freq;
	}

	public List<RevisionEntry> getRevisions() {
		return revisions;
	}

	public boolean isInRevisions() {
		return inRevisions;
	}

	public boolean isReviseNow() {

		if (!isInRevisions()) {
			return false;
		}

		if (revisions.isEmpty()) {
			return true;
		}

		RevisionEntry lastRevision = revisions.get(revisions.size() - 1);
		if (lastRevision.isToContinue()) {
			return true;
		}

		int freq = getRevisionFrequency();

		// Modify frequency to prevent stacking of revisions made on the same
		// day.
		freq *= (1.0 - COUNTER_STACKING_FACTOR) + Math.random()
				* (COUNTER_STACKING_FACTOR / 2.0);
		freq = Math.max(freq, MIN_REVISION_INTERVAL);
		freq = Math.min(freq, MAX_REVISION_INTERVAL);

		DateTime nextRevisionDate = lastRevision.getCreatedAt().plusDays(freq)
				.withTimeAtStartOfDay();
		DateTime todayMidnight = DateTime.now().withTimeAtStartOfDay();

		return !nextRevisionDate.isAfter(todayMidnight);
	}

	public void setCreatedAt(DateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setInRevisions(boolean inRevisions) {
		this.inRevisions = inRevisions;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLangA(String langA) {
		this.langA = langA;
	}

	public void setLangAText(String text) {
		this.langAText = text;
	}

	public void setLangB(String langB) {
		this.langB = langB;
	}

	public void setLangBText(String text) {
		this.langBText = text;
	}

	public void setRevisions(List<RevisionEntry> revisions) {
		this.revisions = revisions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("id=%d %s=\'%s\' %s=\'%s\' [%s]\n", getId(), //$NON-NLS-1$
				getLangA(), getLangAText(), getLangB(), getLangBText(),
				getCreatedAt()));

		for (RevisionEntry re : revisions) {
			sb.append("   " + re.toString() + "\n");
		}

		return sb.toString();
	}

}
