/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import static org.junit.Assert.*;
import lu.lippmann.cdb.util.FakeCGraphBuilder;
import org.junit.Test;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class SimpleGraphDslTest 
{
	@Test
	public void testGlobal()
	{
		try
		{
			final SimpleGraphDsl sgd=new SimpleGraphDsl();
			final String s=sgd.getDslString(FakeCGraphBuilder.buildGraphWithOperations());
			assertNotNull(s);
			assertTrue(s.length()>0);
			System.out.println(s);
			
			final GraphDslParsingResult gdpr=sgd.getGraphDslParsingResult(s);
			assertNotNull(gdpr);
			assertNotNull(gdpr.getGraph());
			assertTrue(gdpr.getLinesWithError().isEmpty());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}			
		
	}
}
