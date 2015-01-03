package pkleczek.profiwan.model;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class Statistics {

	class SessionEntry {
		/**
		 * Total number of entered revisions in a session.
		 */
		int nRevisions;

		/**
		 * Total number of entered words in a session.
		 */
		int nWords;
	}

	/**
	 * 
	 * @param from
	 *            null - from the very beginning
	 * @param to
	 *            null - to the very end
	 * @return
	 */
	public Map<DateTime, SessionEntry> getSessionsHistory(DateTime from,
			DateTime to) {
		Map<DateTime, SessionEntry> stats = new HashMap<DateTime, Statistics.SessionEntry>();
		
		// TODO: obliczenia zrobić na poziomie bazy danych?
		
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
