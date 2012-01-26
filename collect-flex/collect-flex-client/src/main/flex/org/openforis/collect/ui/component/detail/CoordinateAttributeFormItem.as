package org.openforis.collect.ui.component.detail {
	import mx.containers.FormItem;
	import mx.core.IVisualElementContainer;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.component.input.InputField;
	
	import spark.components.DropDownList;
	
	public class CoordinateAttributeFormItem extends AttributeFormItem implements CollectFormItem {
		
		private var _isInDataGroup:Boolean;
		
		private var _srsDropDownList:DropDownList;
		private var _xInputField:InputField;
		private var _yInputField:InputField;
		
		public function CoordinateAttributeFormItem(isInDataGroup:Boolean = false) {
			super();
			this._isInDataGroup = isInDataGroup;
			_srsDropDownList = createSRSDropDownList();
			_xInputField = createXInputField();
			_yInputField = createYInputField();
		}
		
		override public function addTo(parent:IVisualElementContainer):void {
			if(this._isInDataGroup) {
				parent.addElement(_srsDropDownList);
				parent.addElement(_xInputField);
				parent.addElement(_yInputField);
			} else {
				var formItem:FormItem;
				//srs
				formItem = new FormItem();
				formItem.label = Message.get('edit.coordinate.srs');
				formItem.addElement(_srsDropDownList);
				parent.addElement(formItem);
				//x
				formItem = new FormItem();
				formItem.label = Message.get('edit.coordinate.x');
				formItem.addElement(_xInputField);
				parent.addElement(formItem);
				//y
				formItem = new FormItem();
				formItem.label = Message.get('edit.coordinate.y');
				formItem.addElement(_yInputField);
				parent.addElement(formItem);
			}
		}
		
		private function createSRSDropDownList():DropDownList {
			var result:DropDownList = new DropDownList();
			result.id = "srsDropDownList";
			result.prompt = Message.get("global.dropDownPrompt");
			return result;
		}
		
		private function createXInputField():InputField {
			var result:InputField = new InputField();
			result.id = "xInputField";
			return result;
		}

		private function createYInputField():InputField {
			var result:InputField = new InputField();
			result.id = "yInputField";
			return result;
		}

	}
}