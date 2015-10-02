package org.openforis.collect.datacleansing;

import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataQueryEvaluator {

	List<Node<?>> evaluate(CollectRecord record);

}