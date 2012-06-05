package org.openforis.collect.ui.component.input
{
	import mx.controls.DateField;
	
	public class DateField extends mx.controls.DateField {
		
		private var _showTextInput:Boolean = false;
		
		public function DateField() {			
			super();
		}
	
		override protected function createChildren():void{
			super.createChildren();
			if(!showTextInput) {
				super.textInput.width = 0;
				super.textInput.visible = false;
				super.textInput.includeInLayout = false;
			}
		}

		public function get showTextInput():Boolean {
			return _showTextInput;
		}

		public function set showTextInput(value:Boolean):void {
			_showTextInput = value;
		}

		
	}
}