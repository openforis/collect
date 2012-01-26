package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.view.SurveySelectionView;
	
	public class SurveySelectionPresenter extends AbstractPresenter {
		
		private var _view:SurveySelectionView;
		
		public function SurveySelectionPresenter(view:SurveySelectionView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
			eventDispatcher.addEventListener(ApplicationEvent.SURVEYS_LOADED, surveysLoadedHandler);
			//_view.addEventListener(UIEvent.SURVEY_SELECTED, surveySelectedHandler);
		}
		
		protected function surveysLoadedHandler(event:ApplicationEvent):void {
			_view.surveyDataGroup.dataProvider = Application.surveySummaries;
		}
		
		protected function surveySelectedHandler(event:UIEvent):void {
			var selectedSurvey:Object = event.obj;
			
		}
		
	}
}