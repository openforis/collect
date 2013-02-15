package org.openforis.collect.ui.view {
	import mx.controls.ProgressBar;
	
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	
	import spark.components.DataGrid;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public interface IReferenceDataImportView {

		function get progressLabelText():String;
		
		function set progressLabelText(value:String):void;
		
		function get progressTitleText():String;
		
		function set progressTitleText(value:String):void;

		function get paginationBar():PaginationBar;
		
		function get summaryDataGrid():DataGrid;

		function get progressBar():ProgressBar;

		function get errorsDataGrid():DataGrid;

	}
}