package org.openforis.collect.ui.container {

	import spark.components.Group;
	
	/**
	 * @author S. Ricci
	 * 
	 * */
	public class ControlledScrollGroup extends Group {
		
		private var _step_size:int = 0;
		
		public function ControlledScrollGroup()
		{
			super();
		}
		
		public function get stepSize():int {
			return _step_size;
		}
		
		public function set stepSize(value:int):void {
			_step_size = value;
		}
		
		override public function getVerticalScrollPositionDelta(navigationUnit:uint):Number {
			var megaValue:Number = super.getVerticalScrollPositionDelta(navigationUnit);
			
			if(megaValue == 0)
			{
				return 0;
			}
			
			var smallerValue:int =  _step_size;
			
			if(smallerValue ==0)
			{
				return megaValue;
			}
			
			if(megaValue < 0)
			{
				smallerValue = -1*smallerValue;
			}
			
			return smallerValue;
		}
		
	}
}