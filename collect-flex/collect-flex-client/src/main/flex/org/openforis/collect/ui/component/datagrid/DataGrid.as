package org.openforis.collect.ui.component.datagrid
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import spark.components.DataGrid;
	
	/**
	 * @author S. Ricci
	 */
	public class DataGrid extends spark.components.DataGrid
	{
		/**
		 * list of items selected using checkboxes (see SelectRecordColumnHeaderRenderer and SelectRecordColumnItemRenderer) 
		 */ 
		[Bindable]
		public var selectedByCheckBox:ArrayCollection = new ArrayCollection();
		
		public function DataGrid()
		{
			super();
		}
		
		override public function set dataProvider(value:IList):void {
			super.dataProvider = value;
			selectedByCheckBox.removeAll();
		}
		
		public function setAllItemsByChecbox():void {
			selectedByCheckBox.removeAll();
			selectedByCheckBox.addAll(dataProvider);
		}
	}
}