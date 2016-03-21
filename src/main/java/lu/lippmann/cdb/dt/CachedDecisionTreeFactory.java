/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt;

import java.util.*;
import weka.core.Instances;


/**
 * AbstractDecisionTreeFactory.
 * 
 * @author Olivier PARISOT
 */
public abstract class CachedDecisionTreeFactory implements DecisionTreeFactory 
{
	//
	// Static fields
	//
		
	/** Max size for internal cache. */
	private static final int CACHE_MAX_SIZE=10000;	
	/** Internal cache used for memoization (-> http://en.wikipedia.org/wiki/Memoization). */
	private static final Map<String,DecisionTree> CACHE=new HashMap<String,DecisionTree>(CACHE_MAX_SIZE);
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DecisionTree buildCachedDecisionTree(final Instances instances) throws Exception 
	{
		final String key=this.getDecisionTreeKey(instances);
		if (CACHE.containsKey(key))
		{
			return CACHE.get(key);
		}
		else
		{
			synchronized(CACHE)
			{
				if (CACHE.size()>CACHE_MAX_SIZE) 
				{
					CACHE.clear();
				}
			}
			final DecisionTree dt=buildDecisionTree(instances);
			CACHE.put(key,dt);
			return dt;
		}
	}


}
