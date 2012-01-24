package org.openforis.collect.ui.component.input {
	import flash.events.Event;
	
	import mx.core.ClassFactory;
	import mx.core.IFactory;
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
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
		
		private var _presenterFactory:IFactory;
		
		private var _presenter:InputFieldPresenter;

		private var _renderInDataGroup:Boolean;
		
		protected var changed:Boolean = false;
		
		protected var _textInput:UIComponent;
		
		public function InputField() {
			super();
		}
		
		override protected function initializationComplete():void {
			super.initializationComplete();
			
			initializePresenter();
		}
		
		protected function initializePresenter():void {
			if(_presenterFactory == null) {
				_presenterFactory = new ClassFactory(InputFieldPresenter);
			}
			
			if(this._presenter == null) {
				this._presenter = _presenterFactory.newInstance() as InputFieldPresenter;
				this._presenter.inputField = this;
			}
		}
		
		protected function textInputFocusInHandler(event:Event):void {
			var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.INPUT_FIELD_FOCUS_IN);
			this.dispatchEvent(inputFieldEvent);
		}
		
		protected function focusOutEventHandler(event:*):void {
			if(changed) {
				var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.INPUT_FIELD_VALUE_CHANGE);
				this.dispatchEvent(inputFieldEvent);
			} else {
				//dispatch event validate field
			}
		}
		
		protected function textInputChangeHandler(event:Event):void {
			changed = true;
		}
		
		protected function createAttributeValue():Object {
			var attributeValue:Object = new Object();
			attributeValue.string1 = text;
			return attributeValue;
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
		
		/**
		 * Set to true when this input field is used insed a data group (i.e. if the parent is a multiple entity)
		 * */
		public function get renderInDataGroup():Boolean {
			return _renderInDataGroup;
		}

		/**
		 * @private
		 */
		public function set renderInDataGroup(value:Boolean):void {
			_renderInDataGroup = value;
		}

		public function get presenter():InputFieldPresenter {
			return _presenter;
		}

		public function set presenter(value:InputFieldPresenter):void
		{
			_presenter = value;
		}

		public function get presenterClass():IFactory {
			return _presenterFactory;
		}

		public function set presenterClass(value:IFactory):void {
			_presenterFactory = value;
		}


	}
}