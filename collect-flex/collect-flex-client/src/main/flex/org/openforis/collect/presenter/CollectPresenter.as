package org.openforis.collect.presenter {
	
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ItemResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.ModelClient;
	import org.openforis.collect.client.SessionClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.SessionState;
	import org.openforis.idm.metamodel.Survey;

	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;

		private var _view:collect;
		private var _modelClient:ModelClient;
		private var _sessionClient:SessionClient;
		private var _contextMenuPresenter:ContextMenuPresenter;
		
		private var _keepAliveTimer:Timer;
		
		//semaphores
		private var _sessionStateLoaded:Boolean = false;
		private var _surveysLoaded:Boolean = false;
		
		public function CollectPresenter(view:collect) {
			super();
			
			this._view = view;
			this._modelClient = ClientFactory.modelClient;
			this._sessionClient = ClientFactory.sessionClient;

			_keepAliveTimer = new Timer(KEEP_ALIVE_FREQUENCY)
			_keepAliveTimer.addEventListener(TimerEvent.TIMER, sendKeepAliveMessage);
			_keepAliveTimer.start();
			
			this._sessionClient.getSessionState(new ItemResponder(getSessionStateResultHandler, faultHandler));
			
			this._modelClient.getSurvey(new ItemResponder(getSurveyResultHandler, faultHandler), "archenland1");
			
			this._contextMenuPresenter = new ContextMenuPresenter(view);
		}
		
		override internal function initEventListeners():void {
			
		}
		internal function res(event:ResultEvent, token:Object = null):void {
			trace(event);
		}
		
		internal function getSurveysResultHandler(event:ResultEvent, token:Object = null):void {
			_surveysLoaded = true;
			checkInitializationComplete();
			var surveys:IList = event.result as IList;
			Application.SURVEYS = surveys;
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.SURVEYS_LOADED));
		}
		
		internal function getSurveyResultHandler(event:ResultEvent, token:Object = null):void {
			var survey:Survey = event.result as Survey;
			
			Application.SURVEYS = new ArrayCollection();
			Application.SURVEYS.addItem(survey);
			
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.SURVEYS_LOADED));
			
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.APPLICATION_INITIALIZED));
		}
		
		
		internal function checkInitializationComplete():void {
			//when all information is loaded, dispatch applicationInitialized event
			if(_surveysLoaded && _sessionStateLoaded ) {
				eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.APPLICATION_INITIALIZED));
			}
		}
		
		internal function getSessionStateResultHandler(event:ResultEvent, token:Object = null):void {
			//Application.SESSION_ID = event.result as String;
			//TODO: Add sessionState to Application
			var sessionState:SessionState = event.result as SessionState;
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.SESSION_STATE_LOADED));
		}
		
		internal function sendKeepAliveMessage(event:TimerEvent):void {
			this._sessionClient.keepAlive(new ItemResponder(keepAliveResult, faultHandler));
		}
		
		internal function keepAliveResult(event:ResultEvent, token:Object = null):void {
			//keep alive succesfully sent
			//trace("[Keep Alive response received] " + event);
		}
	}
}