package org.openforis.collect.manager.speciesImport;


/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonParsingError {
	
	private int row;
	private String column;
	private String message;
	private Type type;
	
	public enum Type {
		WRONG_HEADER, EMPTY, WRONG_VALUE;
	}
	public TaxonParsingError(Type type) {
		this(type, -1, (String) null, (String) null);
	}
	
	public TaxonParsingError(Type type, String message) {
		this(type, -1, (String) null, message);
	}
	
	public TaxonParsingError(int row, String column) {
		this(Type.WRONG_VALUE, row, column, (String) null);
	}
	
	public TaxonParsingError(Type type, int row, String column, String message) {
		super();
		this.type = type;
		this.row = row;
		this.column = column;
		this.message = message;
	}

	public TaxonParsingError(Type type, int row, TaxonFileColumn column) {
		this(type, row, column, (String) null);
	}	
	
	public TaxonParsingError(int row, TaxonFileColumn column) {
		this(Type.WRONG_VALUE, row, column, (String) null);
	}	
	
	public TaxonParsingError(Type type, int row, TaxonFileColumn column, String message) {
		this(type, row, column.getName(), message);
	}
	
	public TaxonParsingError(int row, TaxonFileColumn column, String message) {
		this(Type.WRONG_VALUE, row, column.getName(), message);
	}	
	
	public Type getType() {
		return type;
	}
	
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}