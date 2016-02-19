package org.openforis.collect.ui.component
{
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.util.ObjectUtil;
	
	public class RecordStepProgressBarItemRenderer extends ProgressBarItemRenderer {
		
		public function RecordStepProgressBarItemRenderer():void {
			super();
			this.toolTipFunction = stepToolTipFunction;
			this.maximum = CollectRecord$Step.constants.length;
		}
		
		private function stepToolTipFunction(data:Object):String {
			var stepNumber:Number = ObjectUtil.getValue(data, valuePropertyName);
			var stepName:String = CollectRecord$Step.constants[stepNumber - 1].name;
			return Message.get("phase." + stepName);
		}
		
	}
}