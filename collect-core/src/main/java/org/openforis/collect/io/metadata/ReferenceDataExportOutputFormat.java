package org.openforis.collect.io.metadata;

public enum ReferenceDataExportOutputFormat {

	CSV("csv"), EXCEL("xlsx");
	
	private String fileExtesion;

	private ReferenceDataExportOutputFormat(String fileExtesion) {
		this.fileExtesion = fileExtesion;
	}
	
	public String getFileExtesion() {
		return fileExtesion;
	}
}
