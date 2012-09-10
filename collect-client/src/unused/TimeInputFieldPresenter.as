package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.managers.IFocusManagerComponent;
	
	import org.openforis.collect.model.proxy.TimeProxy;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TimeInputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TimeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:TimeInputField;
		
		public function TimeInputFieldPresenter(inputField:TimeInputField = null) {
			_view = inputField;
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			//hours
			_view.hoursTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.hoursTextInput.addEventListener(Event.CHANGE, changeHandler);
			//minutes
			_view.minutesTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.minutesTextInput.addEventListener(Event.CHANGE, changeHandler);
			
			_view.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
		}

		override protected function focusOutHandler(event:FocusEvent):void {
			var focussedField:IFocusManagerComponent = _view.focusManager.getFocus();
			if(changed && focussedField != _view.hoursTextInput && focussedField != _view.minutesTextInput) {
				applyChanges();
			}
		}
		
		override protected function createRequestValue():Array {
			var result:Array = new Array(2);
			result[0] = StringUtil.trim(_view.hoursTextInput.text);
			result[1] = StringUtil.trim(_view.minutesTextInput.text);
			return result;
		}
		
		protected function getTimeFromFields():TimeProxy {
			//check if input text is valid
			if(StringUtil.isNotBlank(_view.hoursTextInput.text) && 
				StringUtil.isNotBlank(_view.minutesTextInput.text)) {
			}
			return null;
		}
		
		override protected function updateView():void {
			super.updateView();
			_view.hoursTextInput.text =_view.minutesTextInput.text = "";
			if(_view.attribute != null) {
				if(_view.attribute.symbol != null && getReasonBlankShortKey(_view.attribute.symbol)) {
					_view.hoursTextInput.text = getReasonBlankShortKey(_view.attribute.symbol);
				} else {
					var time:TimeProxy = _view.attribute.value as TimeProxy;
					if(time != null) {
						_view.hoursTextInput.text = StringUtil.zeroPad(time.hour);
						_view.minutesTextInput.text = StringUtil.zeroPad(time.minute);
					}
				}
			}
		}
		
	}
}
