package org.openforis.collect.ui.component.datagrid {
	import mx.core.ClassFactory;
	
	import spark.components.gridClasses.GridColumn;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class SelectRecordColumn extends GridColumn {
		public function SelectRecordColumn(columnName:String=null)
		{
			super(columnName);
			
			headerRenderer = new ClassFactory(SelectRecordColumnHeaderRenderer);
			itemRenderer = new ClassFactory(SelectRecordColumnItemRenderer);
		}
	}
}