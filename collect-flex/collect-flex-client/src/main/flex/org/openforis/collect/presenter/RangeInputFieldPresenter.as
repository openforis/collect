package org.openforis.collect.presenter
{
	import mx.rpc.AsyncResponder;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.input.RangeInputField;
	
	/**
	 * @author S. Ricci
	 */
	public class RangeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:RangeInputField;
		
		public function RangeInputFieldPresenter(inputField:RangeInputField) {
			_view = inputField;
			_view.fieldIndex = -1;
			super(inputField);
		}
		
	}
}