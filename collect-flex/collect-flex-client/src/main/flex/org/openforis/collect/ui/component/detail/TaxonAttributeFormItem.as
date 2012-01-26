package org.openforis.collect.ui.component.detail {
	import mx.containers.FormItem;
	import mx.core.IVisualElementContainer;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TextInput;
	
	import spark.components.DropDownList;
	
	public class TaxonAttributeFormItem extends AttributeFormItem implements CollectFormItem {
		
		private var _isInDataGroup:Boolean;
		
		private var _codeInputField:InputField;
		private var _scientificNameInputField:InputField;
		private var _vernacularNameInputField:InputField;
		private var _vernacularLangInputField:InputField;
		
		public function TaxonAttributeFormItem(isInDataGroup:Boolean = false) {
			super();
			this._isInDataGroup = isInDataGroup;

			//create input fields
			_codeInputField = createCodeInputField();
			_scientificNameInputField = createScientificNameInputField();
			_vernacularNameInputField = createVernacularNameInputField();
			_vernacularLangInputField = createVernacularLangInputField();
		}
		
		override public function addTo(parent:IVisualElementContainer):void {
			if(this._isInDataGroup) {
				parent.addElement(_codeInputField);
				parent.addElement(_scientificNameInputField);
				parent.addElement(_vernacularNameInputField);
				parent.addElement(_vernacularLangInputField);
			} else {
				var formItem:FormItem;
				//code
				formItem = new FormItem();
				formItem.label = Message.get('edit.taxon.code');
				formItem.addElement(_codeInputField);
				parent.addElement(formItem);
				//scientific name
				formItem = new FormItem();
				formItem.label = Message.get('edit.taxon.scientificName');
				formItem.addElement(_scientificNameInputField);
				parent.addElement(formItem);
				//vernacular name
				formItem = new FormItem();
				formItem.label = Message.get('edit.taxon.vernacularName');
				formItem.addElement(_vernacularNameInputField);
				parent.addElement(formItem);
				//vernacular lang
				formItem = new FormItem();
				formItem.label = Message.get('edit.taxon.vernacularLang');
				formItem.addElement(_vernacularLangInputField);
				parent.addElement(formItem);
			}
		}
		
		private function createSRSDropDownList():DropDownList {
			var result:DropDownList = new DropDownList();
			result.id = "srsDropDownList";
			result.prompt = Message.get("global.dropDownPrompt");
			return result;
		}
		
		private function createCodeInputField():InputField {
			var result:InputField = new InputField();
			result.id = "codeInputField";
			return result;
		}
		
		private function createScientificNameInputField():InputField {
			var result:InputField = new InputField();
			result.id = "scientificNameInputField";
			return result;
		}
		
		private function createVernacularNameInputField():InputField {
			var result:InputField = new InputField();
			result.id = "vernacularNameInputField";
			return result;
		}

		private function createVernacularLangInputField():InputField {
			var result:InputField = new InputField();
			result.id = "vernacularLangInputField";
			var textInput:TextInput = result.textInput as TextInput;
			if(textInput != null) {
				textInput.editable = false;
			}
			return result;
		}

	}
}