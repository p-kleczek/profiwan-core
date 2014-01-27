package pkleczek.profiwan.utils;

import java.util.List;

import pkleczek.profiwan.model.PhraseEntry;
import pkleczek.profiwan.model.RevisionEntry;
import pkleczek.profiwan.model.RevisionsSession;

public interface DatabaseHelper {

	String TAG = RevisionsSession.class.getName();

	String TABLE_COMMENTS = "comments";
	String COLUMN_ID = "_id";
	String COLUMN_COMMENT = "comment";

	String DATABASE_NAME = "profiwan";

	// Table Names
	String TABLE_PHRASE = "Phrase";
	String TABLE_REVISION = "Revision";

	// Common column names
	String KEY_ID = "_id";
	String KEY_CREATED_AT = "created_at";

	// PHRASE Table - column names
	String KEY_PHRASE_LANG1 = "lang1";
	String KEY_PHRASE_LANG2 = "lang2";
	String KEY_PHRASE_LANG1_TEXT = "lang1_text";
	String KEY_PHRASE_LANG2_TEXT = "lang2_text";
	String KEY_PHRASE_LABEL = "label";
	String KEY_PHRASE_IN_REVISION = "in_revision";

	// REVISION Table - column names
	String KEY_REVISION_MISTAKES = "mistakes";
	String KEY_REVISION_PHRASE_ID = "Phrase_idPhrase";

	// Table Create Statements
	// PHRASE Table - create statement
	String CREATE_TABLE_PHRASE = "CREATE TABLE " + TABLE_PHRASE + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_PHRASE_LANG1
			+ " TEXT NOT NULL," + KEY_PHRASE_LANG2 + " TEXT NOT NULL,"
			+ KEY_PHRASE_LANG1_TEXT + " TEXT NOT NULL," + KEY_PHRASE_LANG2_TEXT
			+ " TEXT NOT NULL," + KEY_PHRASE_LABEL + " TEXT NOT NULL,"
			+ KEY_CREATED_AT + " INTEGER NOT NULL," + KEY_PHRASE_IN_REVISION
			+ " INTEGER NOT NULL" + ");";

	String CREATE_TABLE_REVISION = "CREATE TABLE " + TABLE_REVISION + "("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CREATED_AT
			+ " INTEGER NOT NULL," + KEY_REVISION_MISTAKES
			+ " INTEGER NOT NULL," + KEY_REVISION_PHRASE_ID
			+ " INTEGER NOT NULL," + "FOREIGN KEY(" + KEY_REVISION_PHRASE_ID
			+ ") REFERENCES " + TABLE_PHRASE + "(" + KEY_ID
			+ ") ON DELETE CASCADE ON UPDATE CASCADE" + ");";

	long createPhrase(PhraseEntry phrase);

	int updatePhrase(PhraseEntry phrase);

	void deletePhrase(long phrase_id);

	long createRevision(RevisionEntry revision, long phrase_id);

	int updateRevision(RevisionEntry revision);

	List<PhraseEntry> getDictionary();
}
