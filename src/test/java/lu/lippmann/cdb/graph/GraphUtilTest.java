/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import static org.junit.Assert.*;
import java.util.*;

import lu.lippmann.cdb.dsl.ASCIIGraphDsl;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.*;
import lu.lippmann.cdb.util.FakeCGraphBuilder;
import org.junit.Test;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class GraphUtilTest 
{
	@Test
	public void testsaveGraphWithOperation()
	{
		try 
		{
			final GraphWithOperations gwo=FakeCGraphBuilder.buildGraphWithOperations();
			final String s=GraphUtil.saveGraphWithOperation(gwo);
			assertNotNull(s);
			assertTrue(s.length()>0);
			System.out.println(s);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testgetCGraphFrom()
	{
		try 
		{
			final GraphWithOperations gwo=FakeCGraphBuilder.buildGraphWithOperations();
			final CGraph c=GraphUtil.getCGraphFrom(GraphUtil.saveGraphWithOperation(gwo));
			assertNotNull(c);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testgetDeterministicComplexity()
	{
		try 
		{
			assertEquals(2,GraphUtil.computeMinPathLength(FakeCGraphBuilder.buildCGraphForExample().getInternalGraph()));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	@Test
	public void testcomputeDistWithRoot()
	{
		try 
		{
			final GraphWithOperations gwo=FakeCGraphBuilder.buildGraphWithOperations();
			final CNode firstRoot=GraphUtil.getFirstRoot(gwo);			
			assertEquals(0,GraphUtil.computeDistWithRoot(gwo,firstRoot));
			assertEquals(1,GraphUtil.computeDistWithRoot(gwo,gwo.getNeighbors(firstRoot).iterator().next()));			
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testdiff()
	{
		try 
		{
			final GraphWithOperations gwo=FakeCGraphBuilder.buildGraphWithOperations();
			final GraphWithOperations gwo2=FakeCGraphBuilder.buildGraphWithOperations();
			final List<GraphOperation> diff=GraphUtil.diff(CUser.ANONYMOUS,gwo,gwo2);
			assertNotNull(diff);
			assertTrue(diff.isEmpty());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testdiff2()
	{
		try 
		{
			final GraphWithOperations gwo=FakeCGraphBuilder.buildGraphWithOperations();
			final GraphWithOperations gwo2=(GraphWithOperations) GraphUtil.buildNewLocalCGraph().getInternalGraph();
			final List<GraphOperation> diff=GraphUtil.diff(CUser.ANONYMOUS,gwo,gwo2);
			assertNotNull(diff);
			assertTrue(!diff.isEmpty());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testcheckSimplifiable()
	{
		try 
		{
			final GraphWithOperations gwo=FakeCGraphBuilder.buildGraphWithOperationsWithNodesWithSameFinalStates();
			System.out.println("**** before ****");
			System.out.println(new ASCIIGraphDsl().getDslString(gwo));
			final int oldsize=gwo.getVertexCount();
			GraphUtil.simplify(gwo);
			System.out.println("**** after ****");
			System.out.println(new ASCIIGraphDsl().getDslString(gwo));
			final int newsize=gwo.getVertexCount();
			assertTrue(oldsize>newsize);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
