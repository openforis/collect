package org.openforis.collect.ui.component
{
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.util.ObjectUtil;
	
	public class DifferenceProgressBarItemRenderer extends ProgressBarItemRenderer {
		
		public function DifferenceProgressBarItemRenderer():void {
			super();
		}

		override protected function setProgress(value:Number):void {
			super.setProgress(value);
			if (value >= 0) {
				progressBar.styleName = "positive";
			} else {
				progressBar.styleName = "negative";
			}
		}
		
	}
}