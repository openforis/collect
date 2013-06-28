package org.openforis.collect.presenter {
	import org.openforis.collect.metamodel.proxy.UIOptionsProxy;
	import org.openforis.collect.metamodel.proxy.UITabProxy;
	import org.openforis.collect.ui.component.detail.TabbedFormContainer;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class TabbedFormPresenter extends AbstractPresenter {
		
		private var _view:TabbedFormContainer;
		
		public function TabbedFormPresenter(view:TabbedFormContainer) {
			_view = view;
			super();
			buildView();
		}
		
		protected function buildView():void {
			if ( _view.entityDefinition != null && _view.uiTabSet != null ) {
				if ( _view.uiTabSet is UITabProxy ) {
					_view.definitionsPerCurrentTab = UIOptionsProxy.getDefinitionsPerTab(_view.entityDefinition, _view.modelVersion, UITabProxy(_view.uiTabSet));
				}
				_view.innerUITabs = UIOptionsProxy.getInnerTabs(_view.entityDefinition, _view.modelVersion, _view.uiTabSet);
			}
		}
	}
}