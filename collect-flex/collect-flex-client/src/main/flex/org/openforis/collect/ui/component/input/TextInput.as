package org.openforis.collect.ui.component.input {
	import flash.events.Event;
	
	import mx.controls.TextInput;
	
	import org.openforis.collect.util.StringUtil;
	import mx.core.UIComponent;
	
	/***
	 * @author Stefano Ricci
	 * */
	
	[Style(name="remarksIconVisible",type="Boolean",defaultValue="false")]
	[Style(name="approvedIconVisible",type="Boolean",defaultValue="false")]
	public class TextInput extends mx.controls.TextInput {
		public static var text:UIComponent;
		
		public function TextInput() {
			super();
			minWidth = 0;
		}
		
		public function selectAll():void {
			if(!StringUtil.isBlank(this.text)){
				this.setSelection(0, this.text.length);
			}
		}
		
	}
}