package org.openforis.idm.util;

import java.util.Collection;
import java.util.Iterator;

import org.openforis.idm.metamodel.DeepComparable;

/**
 * 
 * @author S. Ricci
 *
 */
public class DeepEquals {

	public static <T extends DeepComparable> boolean deepEquals(Collection<T> coll1, Collection<T> coll2) {
		if (coll1 == coll2)
            return true;

		Iterator<T> e1 = coll1.iterator();
        Iterator<T> e2 = coll2.iterator();
        while (e1.hasNext() && e2.hasNext()) {
        	DeepComparable o1 = e1.next();
        	DeepComparable o2 = e2.next();
            if (!(o1==null ? o2==null : o1.deepEquals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
	}
	
}
