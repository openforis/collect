package org.openforis.collect.ui.component.input {
	import flash.events.Event;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.core.ClassFactory;
	import mx.core.IFactory;
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.DataGrid;
	import spark.components.Group;

	/**
	 * 
	 * @author Mino Togna
	 * */
	public class InputField extends Group {
		
		protected static const WARN_STYLE:String = "warn";
		protected static const ERROR_STYLE:String = "error";
		protected static const APPROVED_STYLE:String = "approved";
		protected static const NOT_RELEVANT_STYLE:String = "notRelevant";
		protected static const REMARKS_PRESENT_STYLE:String = "remarksPresent";
		
		public static const STATE_SAVING:String = "saving";
		public static const STATE_SAVE_COMPLETE:String = "saveComplete";
		public static const STATE_ERROR_SAVING:String = "errorSaving";
		
		private var _presenter:InputFieldPresenter;
		
		protected var _textInput:UIComponent;
		
		private var _attributeDefinition:AttributeDefinitionProxy;

		private var _attribute:AttributeProxy; 
		
		private var _parentEntity:EntityProxy;
		
		public function InputField() {
			super();
			this.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
		}
		
		protected function creationCompleteHandler(event:FlexEvent):void {
			initPresenter();
		}
		
		protected function initPresenter():void {
			this._presenter = new InputFieldPresenter(this);
		}
		
		public function changeSymbol(symbol:AttributeSymbol, remarks:String = null):void {
			presenter.changeSymbol(symbol, remarks);
		}
		
		/**
		 * returns trus if there is not an attribute associated to the field or
		 * the attribute's value is null
		 */
		public function isEmpty():Boolean {
			return attribute == null || (attribute.value == null && attribute.symbol == null);
		}
		
		public function set relevant(value:Boolean):void {
			if(value) {
				UIUtil.removeStyleName(validationListener, NOT_RELEVANT_STYLE);
			} else {
				UIUtil.addStyleName(validationListener, NOT_RELEVANT_STYLE);
			}
		}
		
		public function get text():String {
			return _textInput["text"];
		}
		
		public function get textInput():UIComponent {
			return _textInput;
		}
		
		public function set textInput(value:UIComponent):void {
			_textInput = value;
		}
		
		public function set error(value:String):void {
			if(StringUtil.isBlank(value)) {
				UIUtil.removeStyleName(validationListener, ERROR_STYLE);
			} else {
				UIUtil.addStyleName(validationListener, ERROR_STYLE);
			}
		}
		
		public function set warning(value:String):void {
			if(StringUtil.isBlank(value)) {
				UIUtil.removeStyleName(validationListener, WARN_STYLE);
			} else {
				UIUtil.addStyleName(validationListener, WARN_STYLE);
			}
		}
		
		public function set approved(value:Boolean):void {
			if(value) {
				UIUtil.addStyleName(validationListener, APPROVED_STYLE);
			} else {
				UIUtil.removeStyleName(validationListener, APPROVED_STYLE);
			}
		}

		public function set remarks(value:String):void {
			if(StringUtil.isBlank(value)) {
				UIUtil.removeStyleName(validationListener, REMARKS_PRESENT_STYLE);
			} else {
				UIUtil.addStyleName(validationListener, REMARKS_PRESENT_STYLE);
			}
		}

		public function get validationListener():UIComponent {
			//return validation result listener
			return _textInput;
		}
		
		public function get presenter():InputFieldPresenter {
			return _presenter;
		}

		public function set presenter(value:InputFieldPresenter):void {
			_presenter = value;
		}

		[Bindable]
		public function get attribute():AttributeProxy {
			return _attribute;
		}
		
		public function set attribute(value:AttributeProxy):void {
			this._attribute = value;
		}
		
		[Bindable]
		public function get attributeDefinition():AttributeDefinitionProxy {
			return _attributeDefinition;
		}

		public function set attributeDefinition(value:AttributeDefinitionProxy):void {
			_attributeDefinition = value;
		}

		public function get parentEntity():EntityProxy {
			return _parentEntity;
		}

		public function set parentEntity(value:EntityProxy):void {
			_parentEntity = value;
		}


	}
}