package org.openforis.collect.presenter
{
	import org.openforis.collect.ui.component.input.CalculatedAttributeField;
	
	/**
	 * @author S. Ricci
	 */
	public class CalculatedAttributeFieldPresenter extends InputFieldPresenter {
		
		public function CalculatedAttributeFieldPresenter(inputField:CalculatedAttributeField) {
			super(inputField);
		}
		
		private function get view():CalculatedAttributeField {
			return CalculatedAttributeField(_view);
		}
		
		override protected function initContextMenu():void {
			//do not initialize it
		}
		
		override protected function updateView():void {
			var text:String = getTextFromValue();
			view.text = text;
		}
		
	}
}