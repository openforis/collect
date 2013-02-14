package org.openforis.collect.ui.view {
	import mx.controls.ProgressBar;
	
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	
	import spark.components.DataGrid;
	import spark.components.Group;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class ReferenceDataImportView extends Group {

		private var _progressTitleText:String;
		private var _progressLabelText:String;
		private var _paginationBar:PaginationBar;
		private var _summaryDataGrid:DataGrid;
		private var _progressBar:ProgressBar;
		private var _errorsDataGrid:DataGrid;
		
		[Bindable]
		public function get progressLabelText():String {
			return _progressLabelText;
		}
		
		public function set progressLabelText(value:String):void {
			_progressLabelText = value;
		}
		
		public function get progressTitleText():String {
			return _progressTitleText;
		}
		
		public function set progressTitleText(value:String):void {
			_progressTitleText = value;
		}

		public function get paginationBar():PaginationBar {
			return _paginationBar;
		}
		
		public function get summaryDataGrid():DataGrid {
			return _summaryDataGrid;
		}

		public function get progressBar():ProgressBar {
			return _progressBar;
		}

		public function set progressBar(value:ProgressBar):void {
			_progressBar = value;
		}

		public function get errorsDataGrid():DataGrid {
			return _errorsDataGrid;
		}

		public function set errorsDataGrid(value:DataGrid):void {
			_errorsDataGrid = value;
		}


	}
}