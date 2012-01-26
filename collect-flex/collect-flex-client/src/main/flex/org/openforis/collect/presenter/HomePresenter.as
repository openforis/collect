package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import flash.events.MouseEvent;
	
	import mx.events.EventListenerRequest;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.view.HomeView;

	public class HomePresenter extends AbstractPresenter {
		
		private var _view:HomeView;
		
		public function HomePresenter(view:HomeView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
		}

		protected function applicationInitializedHandler(event:ApplicationEvent):void {
			
		}
			
	}
}