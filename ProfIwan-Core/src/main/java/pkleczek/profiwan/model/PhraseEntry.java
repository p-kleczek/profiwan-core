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
public class PhraseEntry implements Comparable<PhraseEntry>{

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

}
