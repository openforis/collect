package org.openforis.collect.io.data.csv;

public class Column {

	public enum DataType {
		STRING, INTEGER, DECIMAL, DATE, TIME
	}
	
	String header;
	DataType dataType;
	
	public Column(String header) {
		this(header, DataType.STRING);
	}

	public Column(String header, DataType dataType) {
		super();
		this.header = header;
		this.dataType = dataType;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Column other = (Column) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Column [header=" + header + "]";
	}
}
