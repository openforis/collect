package org.openforis.collect.ui.component.detail.input {
	import org.openforis.collect.presenter.InputFieldPresenter;
	
	import spark.components.Group;

	/**
	 * 
	 * @author Mino Togna
	 * */
	public class InputField extends Group {
		
		private var _presenter:InputFieldPresenter;
		//TODO change to attributevalue object
		private var _attributeValue:Object;
		
		private var _renderInDataGroup:Boolean;
		
		public function InputField() {
			super();
			this._presenter = new InputFieldPresenter();
		}
		
		protected function focusOutEventHandler(event:*):void {
			// TODO Auto-generated method stub
		}
		
		public function set relevant(relevant:Boolean):void {
			//add css state for relevant
		}
		
		public function set error(value:String):void {
		
		}
		
		public function set warn(value:String):void {
		
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

		/**
		 * The attribute value
		 * */
		public function get attributeValue():Object {
			return _attributeValue;
		}

		/**
		 * @private
		 */
		public function set attributeValue(value:Object):void {
			_attributeValue = value;
		}

	}
}