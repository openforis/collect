package org.openforis.idm.db;

import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.path.Axis;
import org.openforis.idm.transform.NodeColumnProvider;
import org.openforis.idm.transform.Transformation;

public class RdbTransformation extends Transformation {

	public RdbTransformation(Axis rowAxis, NodeColumnProvider provider)
			throws InvalidExpressionException {
		super(rowAxis, provider);
		// TODO Auto-generated constructor stub
	}

}
