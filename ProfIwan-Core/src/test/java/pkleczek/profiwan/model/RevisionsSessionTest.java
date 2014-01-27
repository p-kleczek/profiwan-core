package pkleczek.profiwan.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import pkleczek.profiwan.ProfIwan;
import pkleczek.profiwan.utils.DatabaseHelper;
import pkleczek.profiwan.utils.DatabaseHelperImpl;

public class RevisionsSessionTest {

	DatabaseHelper dbHelper = DatabaseHelperImpl.getInstance();

	@Before
	public void recreateDB() throws SQLException {
		((DatabaseHelperImpl) dbHelper).recreateTables();
	}

	private RevisionsSession rs = null;

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
}
