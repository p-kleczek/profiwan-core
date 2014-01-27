package pkleczek.profiwan.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pkleczek.profiwan.utils.DBUtils;
import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.DatabaseHelperImpl;

public class PhraseEntryTest {

	DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();
	
	@Before
	public void recreateDB() throws SQLException {
		((DatabaseHelperImpl) dbHelper).recreateTables();
	}
	
	@Test
	public void testIsReviseNowNotInRevision() {
		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(false);
		assertFalse(pe.isReviseNow());
	}	
	
	@Test
	public void testIsReviseNowAfterNextInterval() {
		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(true);
		RevisionEntry re = null;

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_REVISION_INTERVAL + 1));
		re.setMistakes(1);
		pe.getRevisions().add(re);

		assertTrue(pe.isReviseNow());
	}

	@Test
	public void testIsReviseNowBeforeNextInterval() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now());
		re.setMistakes(1);
		pe.getRevisions().add(re);

		assertFalse(pe.isReviseNow());
	}

	@Test
	public void testGetRevisionsFrequencyNoStreak() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		assertEquals(pe.getRevisionFrequency(),
				PhraseEntry.MIN_REVISION_INTERVAL);
	}

	@Test
	public void testGetRevisionsFrequencyStreak() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK + 1; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL
				+ PhraseEntry.FREQUENCY_DECAY, pe.getRevisionFrequency());
	}

	@Test
	public void testGetRevisionsFrequencyNoStreakError() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK - 2; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK));
		re.setMistakes(1);
		pe.getRevisions().add(re);

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL,
				pe.getRevisionFrequency());
	}

	@Test
	public void testGetRevisionsFrequencyStreakError() {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < PhraseEntry.MIN_CORRECT_STREAK + 2; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(PhraseEntry.MIN_CORRECT_STREAK));
		re.setMistakes(1);
		pe.getRevisions().add(re);

		assertEquals(PhraseEntry.MIN_REVISION_INTERVAL
				+ PhraseEntry.FREQUENCY_DECAY, pe.getRevisionFrequency());
	}
	
	@Test
	public void testInsertIntoDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		DateTime dt = DateTime.now();
		
		pe.setLangAText("pl");
		pe.setLangBText("rus");
		pe.setCreatedAt(dt);
		pe.setLabel("lab");
		
		dbHelper.createPhrase(pe);
		
		List<PhraseEntry> dictionary = dbHelper.getDictionary();
		boolean found = false;
		
		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;				
			}
			
			found = true;
			
			assertEquals(pe.getLangAText(), ipe.getLangAText());
			assertEquals(pe.getLangBText(), ipe.getLangBText());
			
			int d1 = DBUtils.getIntFromDateTime(pe.getCreatedAt());
			int d2 = DBUtils.getIntFromDateTime(ipe.getCreatedAt());
			assertEquals(d1, d2);

			assertEquals(pe.getLabel(), ipe.getLabel());
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testUpdateInDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		DateTime dt = DateTime.now();
		
		pe.setLangAText("pl");
		pe.setLangBText("rus");
		pe.setCreatedAt(dt);
		pe.setLabel("lab");
		
		dbHelper.createPhrase(pe);
		
		pe.setLangAText("plX");
		pe.setLangBText("rusX");
		pe.setLabel("labX");

		dbHelper.updatePhrase(pe);
		
		List<PhraseEntry> dictionary = dbHelper.getDictionary();
		boolean found = false;
		
		for (PhraseEntry ipe : dictionary) {
			if (ipe.getId() != pe.getId()) {
				continue;				
			}
			
			found = true;
			
			assertEquals(pe.getLangAText(), ipe.getLangAText());
			assertEquals(pe.getLangBText(), ipe.getLangBText());
			
			int d1 = DBUtils.getIntFromDateTime(dt);
			int d2 = DBUtils.getIntFromDateTime(ipe.getCreatedAt());
			assertEquals(d1, d2);

			assertEquals(pe.getLabel(), ipe.getLabel());
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testDeleteFromDB() throws SQLException {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreatedAt(DateTime.now());
		
		dbHelper.createPhrase(pe);
		dbHelper.deletePhrase(pe.getId());
		
		List<PhraseEntry> dictionary = dbHelper.getDictionary();
		for (PhraseEntry ipe : dictionary) {
			assertThat(ipe.getId(), is(not(pe.getId())));
		}
	}	
}
