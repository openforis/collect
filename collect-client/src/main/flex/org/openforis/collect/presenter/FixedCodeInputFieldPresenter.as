package org.openforis.collect.presenter {
	import flash.events.FocusEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.model.proxy.CodeAttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.FieldUpdateRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.input.FixedCodeInputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class FixedCodeInputFieldPresenter extends InputFieldPresenter {
		
		private static const QUALIFIER_FIELD_IDX:int = 1;

		public function FixedCodeInputFieldPresenter(inputField:FixedCodeInputField) {
			super(inputField);
		}
		
		private function get view():FixedCodeInputField {
			return FixedCodeInputField(_view);
		}

		override protected function initEventListeners():void {
			super.initEventListeners();
			view.qualifierTextInput.addEventListener(FocusEvent.FOCUS_OUT, qualifierFocusOutHandler);
		}
		
		override protected function updateView():void {
			super.updateView();
			var qualifiable:Boolean = false;
			var qualifier:String = null;
			var codeAttribute:CodeAttributeProxy = view.attribute as CodeAttributeProxy;
			if(codeAttribute != null && codeAttribute.codeListItem != null) {
				qualifiable = codeAttribute.codeListItem.qualifiable;
				if(qualifiable) {
					var qualifierField:FieldProxy = view.attribute.getField(1);
					if(qualifierField.value != null) {
						qualifier = String(qualifierField.value);
					}
				}
			}
			view.qualifiable = qualifiable;
			view.qualifierTextInput.text = qualifier;
			if(view.parentEntity) {
				var entityName:String = view.parentEntity.name;
				var ancestorEntityId:Number = view.parentEntity.parentId;
				var ancestorEntity:EntityProxy = Application.activeRecord.getNode(ancestorEntityId) as EntityProxy;
				var width:Number = UIBuilder.getEnumeratedCodeHeaderWidth(view.attributeDefinition, ancestorEntity);
				view.width = width;
			}
		}
		
		override protected function getTextFromValue():String {
			var result:String = null;
			var codeAttribute:CodeAttributeProxy = view.attribute as CodeAttributeProxy;
			if(codeAttribute != null && codeAttribute.codeListItem != null) {
				result = codeAttribute.codeListItem.getLabelText();
			}
			return result;
		}
		
		protected function qualifierFocusOutHandler(event:FocusEvent):void {
			var value:String = view.qualifierTextInput.text;
			value = StringUtil.trim(value);
			applyQualifier(value);
		}
		
		protected function applyQualifier(value:String):void {
			var r:FieldUpdateRequestProxy = new FieldUpdateRequestProxy();
			r.nodeId = view.attribute.id;
			r.fieldIndex = QUALIFIER_FIELD_IDX;
			r.value = value;
			var reqSet:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy(r);
			dataClient.updateActiveRecord(reqSet, null, faultHandler);
		}
		
	}
}
