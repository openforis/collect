/**
 * 
 */
package org.openforis.collect.web.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author S. Ricci
 * 
 */
public class PaginatedResponse {

	private int total;
	private List<?> rows;
	
	public PaginatedResponse() {
		this(0, new ArrayList<Object>());
	}

	public PaginatedResponse(int total, List<?> rows) {
		super();
		this.total = total;
		this.rows = rows;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<?> getRows() {
		return rows;
	}

	public void setRows(List<?> rows) {
		this.rows = rows;
	}

	
}
