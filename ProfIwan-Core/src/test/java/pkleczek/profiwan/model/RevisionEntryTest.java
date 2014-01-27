package pkleczek.profiwan.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.DatabaseHelperImpl;

public class RevisionEntryTest {

	DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();

	@Before
	public void recreateDB() throws SQLException {
		((DatabaseHelperImpl) dbHelper).recreateTables();
	}

	@Test
	public void testIsToContinueTodayNoMistakes() {
		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(DateTime.now());
		re.setMistakes(0);

		assertFalse(re.isToContinue());
	}

	@Test
	public void testIsToContinueTodayCompletedWithMistakes() {
		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(DateTime.now());
		re.setMistakes(1);

		assertFalse(re.isToContinue());
	}

	@Test
	public void testIsToContinueTodayUncompletedWithMistakes() {
		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(DateTime.now());
		re.setMistakes(-1);

		assertTrue(re.isToContinue());
	}

	@Test
	public void testIsToContinuePast() {
		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(1));
		re.setMistakes(-1);

		assertFalse(re.isToContinue());
	}

	@Test
	public void testInsertIntoDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreatedAt(DateTime.now());
		
		dbHelper.createPhrase(pe);

		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(new DateTime(1000L));
		re.setMistakes(1);
		
		dbHelper.createRevision(re, pe.getId());

		boolean found = false;
		List<PhraseEntry> dictionary = dbHelper.getDictionary();

		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;
			}

			found = true;

			List<RevisionEntry> revs = ipe.getRevisions();
			assertFalse(revs.isEmpty());

			RevisionEntry ire = revs.get(0);
			assertEquals(re.getCreatedAt().getMillis(), ire.getCreatedAt()
					.getMillis());
			assertEquals(re.getMistakes(), ire.getMistakes());
		}

		assertTrue(found);
	}

	@Test
	public void testUpdateInDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreatedAt(DateTime.now());
		dbHelper.createPhrase(pe);

		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(new DateTime(1000L));
		re.setMistakes(1);
		dbHelper.createRevision(re, pe.getId());

		re.setMistakes(3);
		dbHelper.updateRevision(re);

		boolean found = false;
		List<PhraseEntry> dictionary = dbHelper.getDictionary();

		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;
			}

			found = true;

			List<RevisionEntry> revs = ipe.getRevisions();
			assertFalse(revs.isEmpty());

			RevisionEntry ire = revs.get(0);
			assertEquals(re.getCreatedAt().getMillis(), ire.getCreatedAt().getMillis());
			assertEquals(re.getMistakes(), ire.getMistakes());
		}

		assertTrue(found);
	}

}
