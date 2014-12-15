package org.openforis.collect.presenter {
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private var _lastLoadCodesAsyncToken:AsyncToken;
		
		public function CodeInputFieldPresenter(view:CodeInputField) {
			super(view);
			view.fieldIndex = -1;
		}
		
		private function get view():CodeInputField {
			return CodeInputField(_view);
		}
		
		public function loadCodes(view:CodeInputField, resultHandler:Function):void {
			var codeAttributeDef:CodeAttributeDefinitionProxy = view.attributeDefinition as CodeAttributeDefinitionProxy;
			var attribute:String = codeAttributeDef.name;
			var parentEntityId:int = view.parentEntity.id;
			var responder:IResponder = new AsyncResponder(resultHandler, faultHandler);
			_lastLoadCodesAsyncToken = dataClient.findAssignableCodeListItems(responder, parentEntityId, attribute);
		}
		
		override protected function getTextFromValue():String {
			if(view.attributeDefinition == null) {
				return "";
			} else {
				return codeAttributeToText(view.attribute);
			}
		}
		
		protected function codeAttributeToText(attribute:AttributeProxy):String {
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(0);
				if(field.symbol != null) {
					var shortCut:String = FieldProxy.getShortCutForReasonBlank(field.symbol);
					if(shortCut != null) {
						return shortCut;
					}
				}
				var code:String = field.value as String;
				var qualifierField:FieldProxy = attribute.getField(1);
				var qualifier:String = qualifierField.value as String;
				return getTextValue(code, qualifier);
			}
			return "";
		}
		
		protected function getTextValue(code:String, qualifier:String):String {
			return StringUtil.concat(": ", code, qualifier);
		}
		
		override protected function getField():FieldProxy {
			if (view.hasOwnProperty("attributes")) {
				var attributes:IList = ObjectUtil.getValue(view, "attributes");
				if (CollectionUtil.isEmpty(attributes)) {
					return super.getField();
				} else {
					var attr:AttributeProxy = AttributeProxy(attributes.getItemAt(0));
					var field:FieldProxy = attr.getField(0);
					return field;
				}
			} else {
				return super.getField();
			}
		}

	}
}
