package org.openforis.collect.designer.model;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zul.AbstractListModel;

public abstract class AbstractPagingListModel<T> extends AbstractListModel<T> {
	
	private static final long serialVersionUID = 2051158696113154870L;
	
	private int _startPageNumber;
	private int _pageSize;
	private int _itemStartNumber;
	
	//internal use only
	private List<T> _items = new ArrayList<T>();
	
	public AbstractPagingListModel(int startPageNumber, int pageSize) {
		super();
		
		this._startPageNumber = startPageNumber;
		this._pageSize = pageSize;
		this._itemStartNumber = startPageNumber * pageSize;
	}

	protected void initItems() {
		this._items = getPageData(_itemStartNumber, _pageSize);
	}
	
	public abstract int getTotalSize();
	protected abstract List<T> getPageData(int itemStartNumber, int pageSize);
	
	@Override
	public T getElementAt(int index) {
		return _items.get(index);
	}

	@Override
	public int getSize() {
		return _items.size();
	}
	
	public int getStartPageNumber() {
		return this._startPageNumber;
	}
	
	public int getPageSize() {
		return this._pageSize;
	}
	
	public int getItemStartNumber() {
		return _itemStartNumber;
	}
}
