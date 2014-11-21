package org.openforis.collect.client {
	
	import mx.rpc.mxml.Concurrency;
	import mx.rpc.remoting.Operation;
	import mx.rpc.remoting.RemoteObject;
	 
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class AbstractClient {

		internal static const CONCURRENCY_MULTIPLE:String = Concurrency.MULTIPLE;
		internal static const CONCURRENCY_SINGLE:String = Concurrency.SINGLE;
		internal static const CONCURRENCY_LAST:String = Concurrency.LAST;
		internal static const TIMEOUT_SECONDS:int = 300;
		
		internal var _remoteObject:RemoteObject;
		
		public function AbstractClient(destination:String = "applicationService") {
			this._remoteObject = new RemoteObject(destination);
			this._remoteObject.channelSet = ChannelSetFactory.servicesChannelSet;
			this._remoteObject.requestTimeout = TIMEOUT_SECONDS;
		}
		
		internal function getOperation(name:String, concurrency:String="single", showBusyCursor:Boolean=true):Operation {
			var operation:Operation = this._remoteObject.getOperation(name) as Operation;
			operation.concurrency = concurrency;
			operation.showBusyCursor = showBusyCursor;
			return operation;
		}
	}
}