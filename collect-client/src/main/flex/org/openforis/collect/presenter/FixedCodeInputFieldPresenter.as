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

		private var _view:FixedCodeInputField;
		
		public function FixedCodeInputFieldPresenter(inputField:FixedCodeInputField) {
			this._view = inputField;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			_view.qualifierTextInput.addEventListener(FocusEvent.FOCUS_OUT, qualifierFocusOutHandler);
		}
		
		override protected function updateView():void {
			super.updateView();
			var qualifiable:Boolean = false;
			var qualifier:String = null;
			var codeAttribute:CodeAttributeProxy = _view.attribute as CodeAttributeProxy;
			if(codeAttribute != null && codeAttribute.codeListItem != null) {
				qualifiable = codeAttribute.codeListItem.qualifiable;
				if(qualifiable) {
					var qualifierField:FieldProxy = _view.attribute.getField(1);
					if(qualifierField.value != null) {
						qualifier = String(qualifierField.value);
					}
				}
			}
			_view.qualifiable = qualifiable;
			_view.qualifierTextInput.text = qualifier;
			if(_view.parentEntity) {
				var entityName:String = _view.parentEntity.name;
				var ancestorEntityId:Number = _view.parentEntity.parentId;
				var ancestorEntity:EntityProxy = Application.activeRecord.getNode(ancestorEntityId) as EntityProxy;
				var width:Number = UIBuilder.getEnumeratedCodeHeaderWidth(_view.attributeDefinition, ancestorEntity);
				_view.width = width;
			}
		}
		
		override protected function getTextFromValue():String {
			var result:String = null;
			var codeAttribute:CodeAttributeProxy = _view.attribute as CodeAttributeProxy;
			if(codeAttribute != null && codeAttribute.codeListItem != null) {
				result = codeAttribute.codeListItem.getLabelText();
			}
			return result;
		}
		
		protected function qualifierFocusOutHandler(event:FocusEvent):void {
			var value:String = _view.qualifierTextInput.text;
			value = StringUtil.trim(value);
			applyQualifier(value);
		}
		
		protected function applyQualifier(value:String):void {
			var r:FieldUpdateRequestProxy = new FieldUpdateRequestProxy();
			r.nodeId = _view.attribute.id;
			r.fieldIndex = QUALIFIER_FIELD_IDX;
			r.value = value;
			var reqSet:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy(r);
			dataClient.updateActiveRecord(reqSet, null, faultHandler);
		}
		
	}
}
