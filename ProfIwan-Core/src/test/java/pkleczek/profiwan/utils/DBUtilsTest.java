package pkleczek.profiwan.utils;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

public class DBUtilsTest {
	
	@Test
	public void testGetIntFromDateTime() {
		long val = (long) Integer.MAX_VALUE * 1000 + 999;
		DateTime dt = new DateTime(val);
		int i = DBUtils.getIntFromDateTime(dt);
		
		assertEquals(Integer.MAX_VALUE, i);
	}
	
	@Test
	public void testGetDateTimeFromInt() {
		DateTime dt = DBUtils.getDateTimeFromInt(Integer.MAX_VALUE);
		long val = (long) Integer.MAX_VALUE * 1000;
		
		assertEquals(dt.getMillis(), val);
	}	
	
	// INFO: test getDeictionary() done indirectly in other classes 
}
