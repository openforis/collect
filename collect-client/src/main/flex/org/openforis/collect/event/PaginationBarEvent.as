package org.openforis.collect.event
{
	import flash.events.Event;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class PaginationBarEvent extends Event {
		
		public static const PAGE_CHANGE:String = "pageChange";
		
		private var _currentPage:int;
		private var _offset:int;
		private var _recordsPerPage:int;
		
		public function PaginationBarEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
		
		public function get currentPage():int {
			return _currentPage;
		}

		public function set currentPage(value:int):void {
			_currentPage = value;
		}

		public function get offset():int {
			return _offset;
		}

		public function set offset(value:int):void {
			_offset = value;
		}

		public function get recordsPerPage():int {
			return _recordsPerPage;
		}
		
		public function set recordsPerPage(value:int):void {
			_recordsPerPage = value;
		}


	}
}