package pkleczek.profiwan.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import pkleczek.profiwan.utils.DBUtils;
import pkleczek.profiwan.utils.DatabaseHelper;

/**
 * The <code>PhraseEntry</code> class stores all the information about a phrase
 * (ie. translations, revisions).
 * 
 * @author Pawel
 * 
 */
public class PhraseEntry implements Comparable<PhraseEntry> {

	/**
	 * Datetime when the phrase entry was created.
	 */
	private DateTime createdAt = null;

	/**
	 * ID of the entry as in database.
	 */
	private long id;

	/**
	 * <code>true</code> if the phrase is currently revised
	 */
	private boolean inRevisions = false;

	/**
	 * A string used to group phrases by the user.
	 */
	private String label = ""; //$NON-NLS-1$

	/**
	 * ISO code of the first language.
	 */
	private String langA = "";

	/**
	 * Phrase in the first language.
	 */
	private String langAText = ""; //$NON-NLS-1$

	/**
	 * ISO code of the second language.
	 */
	private String langB = "";

	/**
	 * Phrase in the second language (its translation).
	 */
	private String langBText = ""; //$NON-NLS-1$

	/**
	 * List of all past revisions of this phrase.
	 */
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

	public List<RevisionEntry> getRevisions() {
		return revisions;
	}

	public boolean isInRevisions() {
		return inRevisions;
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

	public String[] toCSVTokens(String[] attributes) {
		List<String> tokens = new ArrayList<String>();

		for (String attr : attributes) {
			if (attr.equals(DatabaseHelper.KEY_ID)) {
				tokens.add(String.valueOf(getId()));
			}
			if (attr.equals(DatabaseHelper.KEY_CREATED_AT)) {
				Integer time = DBUtils.getIntFromDateTime(getCreatedAt());
				tokens.add(time.toString());
			}
			if (attr.equals(DatabaseHelper.KEY_PHRASE_LANG1)) {
				tokens.add(getLangA());
			}
			if (attr.equals(DatabaseHelper.KEY_PHRASE_LANG2)) {
				tokens.add(getLangB());
			}
			if (attr.equals(DatabaseHelper.KEY_PHRASE_LANG1_TEXT)) {
				tokens.add(getLangAText());
			}
			if (attr.equals(DatabaseHelper.KEY_PHRASE_LANG2_TEXT)) {
				tokens.add(getLangBText());
			}
			if (attr.equals(DatabaseHelper.KEY_PHRASE_LABEL)) {
				tokens.add(getLabel());
			}
			if (attr.equals(DatabaseHelper.KEY_PHRASE_IN_REVISION)) {
				tokens.add(String.valueOf(isInRevisions()));
			}
		}

		return tokens.toArray(new String[0]);
	}

	/**
	 * 
	 * @param tokens
	 *            maps attribute to its token data
	 */
	public void fromCSVTokens(Map<String, String> data) {

		if (data.containsKey(DatabaseHelper.KEY_ID)) {
			setId(Long.valueOf(data.get(DatabaseHelper.KEY_ID)));
		}

		if (data.containsKey(DatabaseHelper.KEY_CREATED_AT)) {
			Integer intTime = Integer.valueOf(data
					.get(DatabaseHelper.KEY_CREATED_AT));
			setCreatedAt(DBUtils.getDateTimeFromInt(intTime));
		}

		if (data.containsKey(DatabaseHelper.KEY_PHRASE_LANG1)) {
			setLangA(data.get(DatabaseHelper.KEY_PHRASE_LANG1));
		}
		if (data.containsKey(DatabaseHelper.KEY_PHRASE_LANG2)) {
			setLangB(data.get(DatabaseHelper.KEY_PHRASE_LANG2));
		}
		if (data.containsKey(DatabaseHelper.KEY_PHRASE_LANG1_TEXT)) {
			setLangAText(data.get(DatabaseHelper.KEY_PHRASE_LANG1_TEXT));
		}
		if (data.containsKey(DatabaseHelper.KEY_PHRASE_LANG2_TEXT)) {
			setLangBText(data.get(DatabaseHelper.KEY_PHRASE_LANG2_TEXT));
		}
		if (data.containsKey(DatabaseHelper.KEY_PHRASE_LABEL)) {
			setLabel(data.get(DatabaseHelper.KEY_PHRASE_LABEL));
		}

		if (data.containsKey(DatabaseHelper.KEY_PHRASE_IN_REVISION)) {
			Boolean inRevisions = Boolean.valueOf(data
					.get(DatabaseHelper.KEY_PHRASE_IN_REVISION));
			setInRevisions(inRevisions);
		}
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

	@Override
	public int compareTo(PhraseEntry o) {
		return langBText.compareTo(o.langBText);
	}

	/**
	 * A collision exists if two phrases have same languages and at least one
	 * text is same for both phrases.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static boolean isCollision(PhraseEntry p1, PhraseEntry p2) {
		if (!p1.getLangA().equals(p2.getLangA())
				|| !p1.getLangB().equals(p2.getLangB())) {
			return false;
		}

		if (p1.getLangAText().equals(p2.getLangAText())
				|| p1.getLangBText().equals(p2.getLangBText())) {
			return true;
		}

		return false;
	}

	/**
	 * Copies all data that may vary (used to update an entry).
	 * 
	 * @param pe
	 */
	public void copyData(PhraseEntry pe) {
		setLangA(pe.getLangA());
		setLangB(pe.getLangB());
		setLangAText(pe.getLangAText());
		setLangBText(pe.getLangBText());
		setLabel(pe.getLabel());
	}
}
