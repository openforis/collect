package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.ui.Keyboard;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.core.UIComponent;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.managers.IFocusManagerComponent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.ui.component.input.codelist.CodeListAllowedValuesPreviewDialog;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
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
			if(view.attributeDefinition != null) {
				return codeAttributeToText(view.attribute);
			}
			return "";
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
				return StringUtil.concat(": ", code, qualifier);
			}
			return "";
		}
		
	}
}
