package org.openforis.collect.presenter {
	
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	import flash.utils.setTimeout;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ItemResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.MetaModelClient;
	import org.openforis.collect.client.SessionClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.idm.model.impl.AbstractValue;
	import org.openforis.collect.idm.model.impl.BooleanValueImpl;
	import org.openforis.collect.model.TestSurvey;
	import org.openforis.idm.metamodel.impl.SurveyImpl;

	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;

		private var _view:collect;
		private var _metaModelClient:MetaModelClient;
		private var _sessionClient:SessionClient;
		private var _contextMenuPresenter:ContextMenuPresenter;
		
		private var _keepAliveTimer:Timer;
		
		//semaphores
		private var _sessionStateLoaded:Boolean = false;
		private var _surveysLoaded:Boolean = false;
		
		public function CollectPresenter(view:collect) {
			super();
			
			this._view = view;
			this._metaModelClient = ClientFactory.metaModelClient;
			this._sessionClient = ClientFactory.sessionClient;
			/*
			_keepAliveTimer = new Timer(KEEP_ALIVE_FREQUENCY)
			_keepAliveTimer.addEventListener(TimerEvent.TIMER, sendKeepAliveMessage);
			_keepAliveTimer.start();
			
			this._sessionClient.getSessionState(new ItemResponder(getSessionStateResultHandler, faultHandler));
			
			this._metaModelClient.getSurveys(new ItemResponder(getSurveysResultHandler, faultHandler));
			
			*/
			
			//this._sessionClient.testGetValue(new ItemResponder(getValueResultHandler, faultHandler));
			
			
			this._contextMenuPresenter = new ContextMenuPresenter(view);
			
			//this._sessionClient.testGetValue(new ItemResponder(getValueResultHandler, faultHandler));
				
			function getValueResultHandler(event:ResultEvent, token:Object = null):void {
				/*
				var result:ArrayCollection = event.result as ArrayCollection;
				for each(var item:FakeObject in result) {
					trace(item.privateProp);
				}
				
				var abstractValue:AbstractValue = event.result as AbstractValue;
				var booleanValue:BooleanValueImpl = event.result as BooleanValueImpl;
				trace(abstractValue.text1);
				*/
			}
			
			
			//test initialization...
			//test data
			var surveys:ArrayCollection = new ArrayCollection();
			
			var survey:SurveyImpl = new TestSurvey();
			survey.name = "Survey 1";
			surveys.addItem(survey);
			/*
			survey = new TestSurvey();
			survey.name = "Survey 2";
			surveys.addItem(survey);
			*/
			Application.SURVEYS = surveys;
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.SURVEYS_LOADED));
			
			//symulate loading...
			/*
			setTimeout(
				function():void {
					eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.APPLICATION_INITIALIZED));
				}, 2000);
			*/
			
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.APPLICATION_INITIALIZED));
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
		
		internal function checkInitializationComplete():void {
			//when all information is loaded, dispatch applicationInitialized event
			if(_surveysLoaded && _sessionStateLoaded ) {
				eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.APPLICATION_INITIALIZED));
			}
		}
		
		internal function getSessionStateResultHandler(event:ResultEvent, token:Object = null):void {
			//Application.SESSION_ID = event.result as String;
			//TODO: Add sessionState to Application
			
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