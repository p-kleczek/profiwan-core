package pkleczek.profiwan.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.sqlite.SQLiteConfig;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.RevisionEntry;

public class DatabaseHelperImplMock implements DatabaseHelper {

	private static DatabaseHelper instance;

	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static Connection c = null;

	private static NamedParameterStatement insertPhraseEntry = null;
	private static NamedParameterStatement updatePhraseEntry = null;
	private static NamedParameterStatement deletePhraseEntry = null;
	private static NamedParameterStatement selectRevisionEntry = null;
	private static NamedParameterStatement insertRevisionEntry = null;
	private static NamedParameterStatement updateRevisionEntryId = null;

	public static final String prodDb = "jdbc:sqlite:profiwan.db";
	public static final String debugDb = "jdbc:sqlite:profiwan_debug.db";

	static {
		try {
			Class.forName("org.sqlite.JDBC");

			SQLiteConfig conf = new SQLiteConfig();
			conf.enforceForeignKeys(true);

			String modeURL = debugDb;

			c = DriverManager.getConnection(modeURL, conf.toProperties());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		String insertPhraseEntryQuery = "INSERT INTO " + TABLE_PHRASE + " ("
				+ KEY_ID + "," + KEY_PHRASE_LANG1 + "," + KEY_PHRASE_LANG2
				+ "," + KEY_PHRASE_LANG1_TEXT + "," + KEY_PHRASE_LANG2_TEXT
				+ "," + KEY_PHRASE_LABEL + "," + KEY_PHRASE_IN_REVISION + ","
				+ KEY_CREATED_AT + ") VALUES (NULL, :" + KEY_PHRASE_LANG1
				+ ",:" + KEY_PHRASE_LANG2 + ",:" + KEY_PHRASE_LANG1_TEXT + ",:"
				+ KEY_PHRASE_LANG2_TEXT + ",:" + KEY_PHRASE_LABEL + ",:"
				+ KEY_PHRASE_IN_REVISION + ",:" + KEY_CREATED_AT + ");";

		String updatePhraseEntryQuery = "UPDATE " + TABLE_PHRASE + " SET "
				+ KEY_PHRASE_LANG1_TEXT + "=:" + KEY_PHRASE_LANG1_TEXT + ","
				+ KEY_PHRASE_LANG2_TEXT + "=:" + KEY_PHRASE_LANG2_TEXT + ","
				+ KEY_PHRASE_IN_REVISION + "=:" + KEY_PHRASE_IN_REVISION + ","
				+ KEY_PHRASE_LABEL + "=:" + KEY_PHRASE_LABEL + ","
				+ KEY_PHRASE_LANG1 + "=:" + KEY_PHRASE_LANG1 + ","
				+ KEY_PHRASE_LANG2 + "=:" + KEY_PHRASE_LANG2 + " WHERE "
				+ KEY_ID + "=:" + KEY_ID + ";";

		String deletePhraseEntryQuery = "DELETE FROM " + TABLE_PHRASE
				+ " WHERE " + KEY_ID + "=:" + KEY_ID + ";";

		String selectRevisionEntryQuery = "SELECT * FROM " + TABLE_REVISION
				+ " WHERE " + KEY_REVISION_PHRASE_ID + "=:"
				+ KEY_REVISION_PHRASE_ID + " ORDER BY " + KEY_CREATED_AT + ";";

		String insertRevisionEntryQuery = "INSERT INTO " + TABLE_REVISION
				+ " (" + KEY_ID + "," + KEY_CREATED_AT + ","
				+ KEY_REVISION_MISTAKES + "," + KEY_REVISION_PHRASE_ID + ") "
				+ "VALUES (NULL, :" + KEY_CREATED_AT + ",:"
				+ KEY_REVISION_MISTAKES + ",:" + KEY_REVISION_PHRASE_ID + ") ;";

		String updateRevisionEntryIdQuery = "UPDATE " + TABLE_REVISION
				+ " SET " + KEY_REVISION_MISTAKES + "=:"
				+ KEY_REVISION_MISTAKES + " WHERE " + KEY_ID + "=:" + KEY_ID
				+ ";";

		try {
			insertPhraseEntry = new NamedParameterStatement(getConnection(),
					insertPhraseEntryQuery, Statement.RETURN_GENERATED_KEYS);

			updatePhraseEntry = new NamedParameterStatement(getConnection(),
					updatePhraseEntryQuery);

			deletePhraseEntry = new NamedParameterStatement(getConnection(),
					deletePhraseEntryQuery);

			selectRevisionEntry = new NamedParameterStatement(getConnection(),
					selectRevisionEntryQuery);

			insertRevisionEntry = new NamedParameterStatement(getConnection(),
					insertRevisionEntryQuery, Statement.RETURN_GENERATED_KEYS);

			updateRevisionEntryId = new NamedParameterStatement(
					getConnection(), updateRevisionEntryIdQuery);
		} catch (SQLException e) {
			logger.severe(e.toString());
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public static DatabaseHelper getInstance() {
		if (instance == null) {
			instance = new DatabaseHelperImplMock();
		}
		return instance;
	}

	private DatabaseHelperImplMock() {
	}

	public static Connection getConnection() {
		return c;
	}

	public void recreateTables() throws SQLException {

		Connection conn = getConnection();
		Statement stmt = conn.createStatement();

		if (!conn.getMetaData().getURL().contains("debug")) {
			System.err.println("not a debug db!");
		} else {
			try {
				stmt.executeUpdate("DROP TABLE IF EXISTS " + TABLE_PHRASE);
				stmt.executeUpdate(CREATE_TABLE_PHRASE);

				stmt.executeUpdate("DROP TABLE IF EXISTS " + TABLE_REVISION);
				stmt.executeUpdate(CREATE_TABLE_REVISION);
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public long createPhrase(PhraseEntry phrase) {

		try {
			NamedParameterStatement stmt = insertPhraseEntry;

			int ir = phrase.isInRevisions() ? 1 : 0;

			stmt.setString(KEY_PHRASE_LANG1_TEXT, phrase.getLangAText());
			stmt.setString(KEY_PHRASE_LANG2_TEXT, phrase.getLangBText());
			stmt.setInt(KEY_PHRASE_IN_REVISION, ir);
			stmt.setInt(KEY_CREATED_AT, (int) (phrase.getCreatedAt()
					.getMillis() / 1000));
			stmt.setString(KEY_PHRASE_LABEL, phrase.getLabel());
			stmt.setString(KEY_PHRASE_LANG1, phrase.getLangA());
			stmt.setString(KEY_PHRASE_LANG2, phrase.getLangB());

			stmt.executeUpdate();

			ResultSet generatedKeys = null;
			try {
				generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					phrase.setId(generatedKeys.getInt(1));
				} else {
					throw new SQLException(
							"PhraseEntry: no generated key obtained."); //$NON-NLS-1$
				}
			} finally {
				if (generatedKeys != null)
					try {
						generatedKeys.close();
					} catch (SQLException logOrIgnore) {
					}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return phrase.getId();
	}

	public int updatePhrase(PhraseEntry phrase) {
		int rv = 0;

		try {
			NamedParameterStatement stmt = updatePhraseEntry;

			int ir = phrase.isInRevisions() ? 1 : 0;

			stmt.setString(KEY_PHRASE_LANG1_TEXT, phrase.getLangAText());
			stmt.setString(KEY_PHRASE_LANG2_TEXT, phrase.getLangBText());
			stmt.setInt(KEY_PHRASE_IN_REVISION, ir);
			stmt.setString(KEY_PHRASE_LABEL, phrase.getLabel());
			stmt.setString(KEY_PHRASE_LANG1, phrase.getLangA());
			stmt.setString(KEY_PHRASE_LANG2, phrase.getLangB());
			stmt.setInt(KEY_ID, (int) phrase.getId());

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return rv;
	}

	public void deletePhrase(long phrase_id) {
		try {
			NamedParameterStatement stmt = deletePhraseEntry;

			stmt.setInt(KEY_ID, (int) phrase_id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public long createRevision(RevisionEntry revision, long phrase_id) {
		try {
			NamedParameterStatement stmt = insertRevisionEntry;

			stmt.setInt(KEY_CREATED_AT, (int) (revision.getCreatedAt()
					.getMillis() / 1000));
			stmt.setInt(KEY_REVISION_MISTAKES, revision.getMistakes());
			stmt.setInt(KEY_REVISION_PHRASE_ID, (int) phrase_id);
			stmt.executeUpdate();

			ResultSet generatedKeys = null;
			try {
				generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					revision.setId(generatedKeys.getInt(1));
				} else {
					throw new SQLException(
							"PhraseEntry: no generated key obtained."); //$NON-NLS-1$
				}
			} finally {
				if (generatedKeys != null)
					try {
						generatedKeys.close();
					} catch (SQLException logOrIgnore) {
					}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return revision.getId();
	}

	public int updateRevision(RevisionEntry revision) {
		int rv = 0;

		try {
			NamedParameterStatement stmt = updateRevisionEntryId;

			stmt.setInt(KEY_REVISION_MISTAKES, revision.getMistakes());
			stmt.setInt(KEY_ID, (int) revision.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return rv;
	}

	public List<PhraseEntry> getDictionary() {
		List<PhraseEntry> dict = new ArrayList<PhraseEntry>();

		String selectPhraseQuery = "SELECT * FROM Phrase;";

		Connection conn = getConnection();
		Statement stmt = null;
		ResultSet rs = null;

		try {

			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectPhraseQuery);
			while (rs.next()) {
				int id = rs.getInt(KEY_ID);
				String lang1 = rs.getString(KEY_PHRASE_LANG1);
				String lang2 = rs.getString(KEY_PHRASE_LANG2);
				String lang1Text = rs.getString(KEY_PHRASE_LANG1_TEXT);
				String lang2Text = rs.getString(KEY_PHRASE_LANG2_TEXT);
				boolean inRevision = rs.getBoolean(KEY_PHRASE_IN_REVISION);
				DateTime date = new DateTime(
						(long) rs.getInt(KEY_CREATED_AT) * 1000L);
				String label = rs.getString(KEY_PHRASE_LABEL);

				PhraseEntry entry = new PhraseEntry();
				entry.setId(id);
				entry.setLangA(lang1);
				entry.setLangB(lang2);
				entry.setLangAText(lang1Text);
				entry.setLangBText(lang2Text);
				entry.setInRevisions(inRevision);
				entry.setCreatedAt(date);
				entry.setLabel(label);

				dict.add(entry);
			}

			NamedParameterStatement pstmt = selectRevisionEntry;
			for (PhraseEntry entry : dict) {
				pstmt.setInt(KEY_REVISION_PHRASE_ID, (int) entry.getId());
				rs = pstmt.executeQuery();

				while (rs.next()) {
					RevisionEntry re = new RevisionEntry();
					re.setCreatedAt(new DateTime((long) rs
							.getInt(KEY_CREATED_AT) * 1000L));
					re.setMistakes(rs.getInt(KEY_REVISION_MISTAKES));

					entry.getRevisions().add(re);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return dict;
	}

}
