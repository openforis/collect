package org.openforis.collect.client {
	import mx.messaging.ChannelSet;
	import mx.messaging.channels.AMFChannel;
	
	import org.openforis.collect.Application;

	/**
	 * @author Mino Togna
	 * */
	public class ChannelSetFactory {
		
		internal static const AMF_SERVICES_CONTEXT_PATH:String = "messagebroker/amf";
		internal static const AMF_MESSAGING_CONTEXT_PATH:String = "messagebroker/amfpolling";
		
		private static var _servicesChannelSet:ChannelSet;
		private static var _messagingChannelSet:ChannelSet;
		
		public function ChannelSetFactory() {
		}
		
		public static function get servicesChannelSet():ChannelSet{
			if(_servicesChannelSet == null){
				var channel:AMFChannel = new AMFChannel("my-amf", Application.URL + AMF_SERVICES_CONTEXT_PATH);
				_servicesChannelSet = new ChannelSet();
				_servicesChannelSet.addChannel(channel);
			}
			return _servicesChannelSet;
		}

		public static function get messagingChannelSet():ChannelSet {
			if(_messagingChannelSet == null){
				var channel:AMFChannel = new AMFChannel("my-polling-amf", Application.URL + AMF_MESSAGING_CONTEXT_PATH);
				_messagingChannelSet = new ChannelSet();
				_messagingChannelSet.addChannel(channel);
			}
			return _messagingChannelSet;
		}

	}
}