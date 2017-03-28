/**
 * 
 */
package org.openforis.collect.metamodel.ui;

/**
 * @author S. Ricci
 *
 */
public abstract class UITableHeadingComponent extends UIModelObject {

	private static final long serialVersionUID = 1L;
	
	UITableHeadingComponent(UITableHeadingContainer parent, int id) {
		super((UIModelObject) parent, id);
	}

	private UITable getTable() {
		UIModelObject currentParent = getParent();
		while(currentParent != null && !(currentParent instanceof UITable)) {
			currentParent = currentParent.getParent();
		}
		return (UITable) currentParent;
	}
	
	public int getRow() {
		int row = 1;
		UIModelObject currentParent = getParent();
		while (! (currentParent instanceof UITable)) {
			currentParent = currentParent.getParent();
			row ++;
		}
		return row;
	}
	
	public int getRowSpan() {
		return getTable().getTotalHeadingRows() - getRow() - getNestedRowsCount() + 1;
	}
	
	public int getCol() {
		UITableHeadingContainer parentContainer = (UITableHeadingContainer) getParent();
		int index = parentContainer.indexOf(this);
		if (index == 0) {
			if (parentContainer instanceof UITable) {
				return 1;
			} else {
				return ((UITableHeadingComponent) parentContainer).getCol();
			}
		} else {
			UITableHeadingComponent previousSibling = parentContainer.getHeadingComponents().get(index - 1);
			return previousSibling.getCol() + previousSibling.getColSpan();
		}
	}
	
	public abstract int getColSpan();

	protected int getNestedColumnsCount() {
		return 0;
	}

	protected int getNestedRowsCount() {
		return 0;
	}

}
