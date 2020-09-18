/**
 * 
 */
package org.openforis.collect.metamodel.ui;


/**
 * @author S. Ricci
 *
 */
public interface UIFormComponent extends Identifiable {

	int getColumn();
	
	void setColumn(int column);

	int getColumnSpan();
	
	void setColumnSpan(int columnSpan);

	int getRow();
	
	void setRow(int row);
	
	boolean isHideWhenNotRelevant();
	
	void setHideWhenNotRelevant(boolean hide);
	
}
