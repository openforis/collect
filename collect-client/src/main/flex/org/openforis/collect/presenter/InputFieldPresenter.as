package org.openforis.collect.presenter {
	import org.openforis.collect.ui.component.detail.input.InputField;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class InputFieldPresenter extends AbstractPresenter {
		
		private var _inputField:InputField;
		
		public function InputFieldPresenter(inputField:InputField) {
			super();
			this._inputField = inputField;
		}
		
		override internal function initEventListeners():void{
			
		}
	}
}