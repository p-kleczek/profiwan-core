package pkleczek.profiwan;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ pkleczek.profiwan.utils.DBUtilsTest.class,
		pkleczek.profiwan.model.PhraseEntryTest.class,
		pkleczek.profiwan.model.RevisionEntryTest.class,
		pkleczek.profiwan.model.RevisionsSessionTest.class})
public class AllTests {

}
