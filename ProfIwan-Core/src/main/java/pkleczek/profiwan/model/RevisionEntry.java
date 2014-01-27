package pkleczek.profiwan.model;

import org.joda.time.DateTime;

/**
 * The <code>RevisionEntry</code> class stores all information about a revision
 * relevant for generation of further revisions.
 * 
 * @author Pawel
 * 
 */
public class RevisionEntry {

	private DateTime createdAt = null;

	private long id;

	/**
	 * How many times a mistake was made during the given revision.
	 */
	private int mistakes;

	public DateTime getCreatedAt() {
		return createdAt;
	}

	public long getId() {
		return id;
	}

	public int getMistakes() {
		return mistakes;
	}

	public boolean isToContinue() {
		return (createdAt.isAfter(DateTime.now().withTimeAtStartOfDay()) && mistakes < 0);
	}

	public void setCreatedAt(DateTime date) {
		this.createdAt = date;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMistakes(int mistakes) {
		this.mistakes = mistakes;
	}
	
	public void nextMistake() {
		mistakes--;
	}
	
	public void enteredCorrectly() {
		mistakes = Math.abs(mistakes);
	}

	@Override
	public String toString() {
		return String.format("%s [%d]\n", createdAt.toString(), //$NON-NLS-1$
				mistakes);
	}

}
