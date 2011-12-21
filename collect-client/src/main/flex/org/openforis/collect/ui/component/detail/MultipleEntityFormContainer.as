package org.openforis.collect.ui.component.detail
{
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	
	import org.openforis.collect.ui.component.detail.input.InputField;
	
	import spark.components.DropDownList;
	import spark.components.FormItem;
	import spark.components.Label;
	import spark.components.NavigatorContent;
	
	public class MultipleEntityFormContainer extends NavigatorContent
	{
		
		[SkinPart(required="true")]
		public var selectionLabel:Label;
		[SkinPart(required="true")]
		public var selectionDropDownList:DropDownList;

		
		private var _entity:Object;
		
		private var _entityValues:ArrayCollection;
		
		private var _selectedEntityValue:Object;
		
		public function MultipleEntityFormContainer() {
			super();
			
			BindingUtils.bindProperty(this, "label", entity, "label");
		}
		
		override protected function partAdded(partName:String, instance:Object):void {
			super.partAdded(partName, instance);
			
			switch(instance) {
				case selectionLabel:
					BindingUtils.bindProperty(selectionLabel, "text", this, ["entity", "label"]);
					break;
				case selectionDropDownList:
					BindingUtils.bindProperty(selectionDropDownList, "dataProvider", this, "entityValues");
					break;
			}
		}
		
		public function addFormItem(label:String, inputField:InputField):void {
			var formItem:FormItem = new FormItem();
			formItem.label = label;
			formItem.addElement(inputField);
			contentGroup.addElement(formItem);
		}
		
		[Bindable]
		public function get entity():Object {
			return _entity;
		}

		public function set entity(value:Object):void {
			_entity = value;
		}

		public function get entityValues():ArrayCollection {
			return _entityValues;
		}

		public function set entityValues(value:ArrayCollection):void {
			_entityValues = value;
		}

		public function get selectedEntityValue():Object {
			return _selectedEntityValue;
		}

		public function set selectedEntityValue(value:Object):void {
			_selectedEntityValue = value;
		}


	}
}