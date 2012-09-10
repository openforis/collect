package org.openforis.collect.client {
	
	import mx.rpc.AbstractOperation;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.model.Queue;

	/**
	 * @author S. Ricci
	 */
	public class RemoteCallQueueProcessor {
		private var _queue:Queue; //queue of ProcessableItem objects
		private var _responder:IResponder;
		private var _lastCall:RemoteCallWrapper;
		private var _resultHandler:Function;
		private var _faultHandler:Function;
		private var _maxAttempts:int;
		
		public function RemoteCallQueueProcessor(maxAttempts:int, resultHandler:Function, faultHandler:Function = null) {
			this._queue = new Queue();
			this._maxAttempts = maxAttempts;
			this._resultHandler = resultHandler;
			this._faultHandler = faultHandler;
			_responder = new AsyncResponder(callResultHandler, callFaultHandler);
		}
		
		public function isEmpty():Boolean {
			return this._queue.isEmpty();
		}
		
		public function appendOperation(token:Object, resultHandler:Function, faultHandler:Function, operation:AbstractOperation, ... args:Array):void {
			var queueItem:RemoteCallWrapper = new RemoteCallWrapper(operation, args);
			queueItem.token = token;
			queueItem.resultHandler = resultHandler;
			queueItem.faultHandler = faultHandler;
			_queue.push(queueItem);
			sendHeadRemoteCall();
		}
		
		public function removeHeadOperation():RemoteCallWrapper {
			if(_queue.isEmpty()) {
				return null;
			}
			var call:RemoteCallWrapper = RemoteCallWrapper(_queue.pop());
			return call;
		}
		
		public function sendHeadRemoteCall():void {
			if(_queue.isEmpty()) {
				return;
			}
			var remoteCall:RemoteCallWrapper = getHeadElement();
			if(! remoteCall.active) {
				var token:AsyncToken = remoteCall.send();
				token.addResponder(_responder);
			}
		}

		protected function callResultHandler(event:ResultEvent, token:Object = null):void {
			var call:RemoteCallWrapper = RemoteCallWrapper(_queue.pop()); //removes the first element
			call.reset();
			_lastCall = call;
			_resultHandler(event, call.token);
			if(call.resultHandler != null) {
				call.resultHandler(event, call.token);
			}
			sendHeadRemoteCall();
		}
		
		protected function callFaultHandler(event:FaultEvent, token:Object = null):void {
			//remove the failed call from  the queue
			var call:RemoteCallWrapper = _queue.pop() as RemoteCallWrapper;

			if(call.faultHandler != null) {
				call.faultHandler(event, call.token);
			}
		}
		
		private function getHeadElement():RemoteCallWrapper {
			var call:RemoteCallWrapper = RemoteCallWrapper(_queue.element);
			return call;
		}
		
		public function get lastCall():RemoteCallWrapper {
			return _lastCall;
		}

	}
}