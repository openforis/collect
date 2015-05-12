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
		
		public function TabbedFormPresenter(view:TabbedFormContainer) {
			super(view);
		}
		
		private function get view():TabbedFormContainer {
			return TabbedFormContainer(_view);
		}
		
		override public function init():void {
			buildView();
		}
		
		protected function buildView():void {
			if ( view.entityDefinition != null && view.uiTabSet != null ) {
				if ( view.uiTabSet is UITabProxy ) {
					view.definitionsPerCurrentTab = UIOptionsProxy.getDefinitionsPerTab(view.entityDefinition, view.modelVersion, UITabProxy(view.uiTabSet));
				}
				view.innerUITabs = UIOptionsProxy.getInnerTabs(view.entityDefinition, view.modelVersion, view.uiTabSet);
			}
		}
	}
}