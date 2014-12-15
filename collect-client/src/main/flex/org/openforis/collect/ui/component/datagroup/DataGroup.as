package org.openforis.collect.ui.component.datagroup {
	import mx.collections.IList;
	import mx.core.IFactory;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.rpc.events.ResultEvent;
	
	import spark.components.DataGroup;
	import spark.layouts.VerticalLayout;
	
	public class DataGroup extends spark.components.DataGroup {
		
		public function DataGroup() {
			super();
			var vl:VerticalLayout = new VerticalLayout();
			vl.gap = 2;			
			this.layout = vl;
		}
		/*
		override protected function commitProperties():void {
			super.commitProperties();
			addDataProviderListener();
		}
		
		private function addDataProviderListener():void {
			if (dataProvider)
				dataProvider.addEventListener(CollectionEvent.COLLECTION_CHANGE, dataProviderCollectionChangeHandler, false, 400, true);
		}
		
		protected function dataProviderCollectionChangeHandler(event:CollectionEvent):void {
			switch (event.kind) {
				case CollectionEventKind.UPDATE: {
					event.stopImmediatePropagation();
				}
			}
		}
		
		override public function set dataProvider(value:IList):void {
			super.dataProvider = value;
		}
		
		public function deleteItemAt(index:int):void {
			_openForisComponent.deleteItem(null, index);
			if(_selectedIndex == index) {
				_selectedIndex = -1;
			}
			dispatchEvent(new OpenForisDataGroupEvent(OpenForisDataGroupEvent.ITEM_REMOVED));
		}
		
		public function replaceBlankOnRowWithReasonBlankInfo(index:int, reasonBlankCode:String):void {
			_openForisComponent.executeForInputFields(setInfo);
			
			function setInfo(inputField:OpenForisInputField):void {
				if(inputField.isEmpty()) {
					inputField.changeReasonBlankInfo(reasonBlankCode);
				}
			}
		}
		
		[Bindable]
		public function get openForisComponent():OpenForisComponent {
			return _openForisComponent;
		}

		public function set openForisComponent(value:OpenForisComponent):void {
			_openForisComponent = value;
		}

		public function moveItem(indexFrom:int, indexTo:int):void {
			
		}
		
		protected function moveItemResultHandler(event:ResultEvent, token:Object):void {
		}
		
		public function recreateItemRenderer():void{
			var iFactory:IFactory = this.itemRenderer;
			this.itemRenderer = null;
			this.itemRenderer = iFactory;
		}
		*/
	}
}