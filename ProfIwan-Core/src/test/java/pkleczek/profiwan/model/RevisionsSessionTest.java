package pkleczek.profiwan.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.DatabaseHelperImplMock;

public class RevisionsSessionTest {

	DatabaseHelper dbHelper = DatabaseHelperImplMock.getInstance();
	DateTime todayMidnight;

	@Before
	public void recreateDB() throws SQLException {
		((DatabaseHelperImplMock) dbHelper).recreateTables();
		todayMidnight = DateTime.now().withTimeAtStartOfDay();
	}

	private RevisionsSession rs = null;

	@SuppressWarnings("unchecked")
	@Test
	public void testPrepareRevisions() throws Exception {

		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(true);
		pe.setCreatedAt(DateTime.now());
		dbHelper.createPhrase(pe);

		RevisionEntry re = new RevisionEntry();
		re.setCreatedAt(new DateTime(0L));
		re.setMistakes(0);
		dbHelper.createRevision(re, pe.getId());

		rs = new RevisionsSession(dbHelper);

		assertTrue(rs.hasRevisions());
		assertEquals(1, rs.getPendingRevisionsSize());

		Field field = RevisionsSession.class
				.getDeclaredField("revisionEntries");
		field.setAccessible(true);

		Map<Integer, RevisionEntry> map = (Map<Integer, RevisionEntry>) field
				.get(rs);
		assertEquals(1, map.size());
	}

	@Test
	public void testPrepareRevisionEntryBrandNew() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreatedAt(DateTime.now());

		RevisionEntry re = new RevisionEntry();
		re.setId(1);
		re.setCreatedAt(new DateTime(1000L));
		re.setMistakes(-1);
		pe.getRevisions().add(re);

		rs = new RevisionsSession(dbHelper);

		Method method = RevisionsSession.class.getDeclaredMethod(
				"prepareRevisionEntry", PhraseEntry.class);
		method.setAccessible(true);
		RevisionEntry ret = (RevisionEntry) method.invoke(rs, pe);

		assertEquals(0, ret.getId());
		assertEquals(0, ret.getMistakes());
	}

	@Test
	public void testPrepareRevisionEntryNoContinue() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreatedAt(DateTime.now());

		RevisionEntry re = new RevisionEntry();
		re.setId(1);
		re.setCreatedAt(new DateTime(1000L));
		re.setMistakes(-1);
		pe.getRevisions().add(re);

		rs = new RevisionsSession(dbHelper);

		Method method = RevisionsSession.class.getDeclaredMethod(
				"prepareRevisionEntry", PhraseEntry.class);
		method.setAccessible(true);

		RevisionEntry ret = (RevisionEntry) method.invoke(rs, pe);
		assertTrue(ret.getId() != re.getId());

		// XXX: use hamcrest!
		// assertThat(ret, is(not(re)));
	}

	@Test
	public void testPrepareRevisionEntryContinue() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setCreatedAt(DateTime.now());

		RevisionEntry re = new RevisionEntry();
		re.setId(1);
		re.setCreatedAt(DateTime.now());
		re.setMistakes(-1);
		pe.getRevisions().add(re);

		rs = new RevisionsSession(dbHelper);

		Method method = RevisionsSession.class.getDeclaredMethod(
				"prepareRevisionEntry", PhraseEntry.class);
		method.setAccessible(true);

		RevisionEntry ret = (RevisionEntry) method.invoke(rs, pe);
		assertSame(ret, re);
	}

	@Test
	public void testIsReviseNowNotInRevision() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(false);

		Method method = RevisionsSession.class.getDeclaredMethod("isReviseNow",
				PhraseEntry.class, DateTime.class);
		method.setAccessible(true);

		boolean isReviseNowResult = (Boolean) method.invoke(rs, pe,
				todayMidnight);

		assertFalse(isReviseNowResult);
	}

	@Test
	public void testIsReviseNowAfterNextInterval() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		pe.setInRevisions(true);
		RevisionEntry re = null;

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(
				RevisionsSession.MIN_REVISION_INTERVAL + 1));
		re.setMistakes(1);
		pe.getRevisions().add(re);

		Method method = RevisionsSession.class.getDeclaredMethod("isReviseNow",
				PhraseEntry.class, DateTime.class);
		method.setAccessible(true);

		boolean isReviseNowResult = (Boolean) method.invoke(rs, pe,
				todayMidnight);

		assertTrue(isReviseNowResult);
	}

	@Test
	public void testIsReviseNowBeforeNextInterval() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now());
		re.setMistakes(1);
		pe.getRevisions().add(re);

		Method method = RevisionsSession.class.getDeclaredMethod("isReviseNow",
				PhraseEntry.class, DateTime.class);
		method.setAccessible(true);

		boolean isReviseNowResult = (Boolean) method.invoke(rs, pe,
				todayMidnight);

		assertFalse(isReviseNowResult);
	}

	@Test
	public void testGetRevisionsFrequencyNoStreak() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < RevisionsSession.MIN_CORRECT_STREAK; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(
					RevisionsSession.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		Method method = RevisionsSession.class.getDeclaredMethod(
				"getRevisionFrequency", PhraseEntry.class);
		method.setAccessible(true);

		int getRevisionFrequencyResult = (Integer) method.invoke(rs, pe);

		assertEquals(RevisionsSession.MIN_REVISION_INTERVAL,
				getRevisionFrequencyResult);
	}

	@Test
	public void testGetRevisionsFrequencyStreak() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < RevisionsSession.MIN_CORRECT_STREAK + 1; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(
					RevisionsSession.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		Method method = RevisionsSession.class.getDeclaredMethod(
				"getRevisionFrequency", PhraseEntry.class);
		method.setAccessible(true);

		int getRevisionFrequencyResult = (Integer) method.invoke(rs, pe);

		assertEquals(RevisionsSession.MIN_REVISION_INTERVAL
				+ RevisionsSession.FREQUENCY_DECAY, getRevisionFrequencyResult);
	}

	@Test
	public void testGetRevisionsFrequencyNoStreakError() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < RevisionsSession.MIN_CORRECT_STREAK - 2; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(
					RevisionsSession.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(
				RevisionsSession.MIN_CORRECT_STREAK));
		re.setMistakes(1);
		pe.getRevisions().add(re);

		Method method = RevisionsSession.class.getDeclaredMethod(
				"getRevisionFrequency", PhraseEntry.class);
		method.setAccessible(true);

		int getRevisionFrequencyResult = (Integer) method.invoke(rs, pe);

		assertEquals(RevisionsSession.MIN_REVISION_INTERVAL,
				getRevisionFrequencyResult);
	}

	@Test
	public void testGetRevisionsFrequencyStreakError() throws Exception {
		PhraseEntry pe = new PhraseEntry();
		RevisionEntry re = null;

		for (int i = 0; i < RevisionsSession.MIN_CORRECT_STREAK + 2; i++) {
			re = new RevisionEntry();
			re.setCreatedAt(DateTime.now().minusDays(
					RevisionsSession.MIN_CORRECT_STREAK));
			re.setMistakes(0);
			pe.getRevisions().add(re);
		}

		re = new RevisionEntry();
		re.setCreatedAt(DateTime.now().minusDays(
				RevisionsSession.MIN_CORRECT_STREAK));
		re.setMistakes(1);
		pe.getRevisions().add(re);

		Method method = RevisionsSession.class.getDeclaredMethod(
				"getRevisionFrequency", PhraseEntry.class);
		method.setAccessible(true);

		int getRevisionFrequencyResult = (Integer) method.invoke(rs, pe);

		assertEquals(RevisionsSession.MIN_REVISION_INTERVAL, getRevisionFrequencyResult);
	}
}
