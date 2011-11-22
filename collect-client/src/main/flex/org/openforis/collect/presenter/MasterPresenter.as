package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import org.openforis.collect.ui.component.MasterView;

	public class MasterPresenter extends AbstractPresenter {
		
		private var _view:MasterView;
		
		public function MasterPresenter(view:MasterView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
		
		}
		
	}
}