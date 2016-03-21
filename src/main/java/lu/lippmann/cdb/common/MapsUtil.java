/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.util.*;


/**
 * MapsUtil.
 * 
 * @author the WP1 team
 */
public final class MapsUtil
{
	private MapsUtil() {}

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map)
    {
    	return sortByValue(map,true);
    }
	
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map,final boolean asc)
    {
        final List<Map.Entry<K, V>> list=new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare(final Map.Entry<K, V> o1,final Map.Entry<K, V> o2 )
            {
				final int v = (o1.getValue()).compareTo(o2.getValue());
            	if (asc) return v;
				else return -v;
            }
        } );

        final Map<K, V> result = new LinkedHashMap<K, V>();
        for (final Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

}
