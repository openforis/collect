package org.openforis.collect.ui.component.input {
	import flash.events.Event;
	import flash.events.TextEvent;
	
	import mx.controls.TextInput;
	import mx.core.mx_internal;
	
	import org.openforis.collect.util.StringUtil;
	
	use namespace mx_internal;
	
	/***
	 * @author Stefano Ricci
	 * */
	[Style(name="remarksIconVisible",type="Boolean",defaultValue="false")]
	[Style(name="approvedIconVisible",type="Boolean",defaultValue="false")]
	public class TextInput extends mx.controls.TextInput {
		
		private static const TEXT_PADDING_TOP:Number = 2;
		
		private var _restrictPattern:String;
		private var _upperCase:Boolean = true;
		
		public function TextInput() {
			super();
			minWidth = 0;
			addEventListener(TextEvent.TEXT_INPUT, textInputHandler);
			addEventListener(Event.CHANGE, changeHandler);
		}
		
		override protected function updateDisplayList(unscaledWidth:Number,
													  unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			//middle vertical alignment of textField
			textField.y += TEXT_PADDING_TOP;
		}
		
		public function selectAll():void {
			if(!StringUtil.isBlank(this.text)){
				this.setSelection(0, this.text.length);
			}
		}
		
		protected function textInputHandler(event:TextEvent):void {
			if(_restrictPattern != null) {
				var begin:String = text.substr(0, selectionBeginIndex);
				var end:String = text.substr(selectionEndIndex);
				var inserted:String = event.text;
				var newText:String = begin + inserted + end;
				var regExp:RegExp = new RegExp(_restrictPattern, "i");
				var valid:Boolean = regExp.test(newText);
				if(! valid) {
					event.preventDefault();
				}
			}
		}
		
		protected function changeHandler(event:Event):void {
			if(upperCase) {
				text = text.toUpperCase();
			}
		}
		
		public function get restrictPattern():String {
			return _restrictPattern;
		}

		public function set restrictPattern(value:String):void {
			_restrictPattern = value;
		}

		public function get upperCase():Boolean {
			return _upperCase;
		}

		public function set upperCase(value:Boolean):void {
			_upperCase = value;
		}
		
		
		
	}
}