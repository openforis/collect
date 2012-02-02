package org.openforis.collect.ui.component.input
{
	[Bindable]
	public class CodeListItem
	{
		public var label:String;
		public var code:String;
		public var selected:Boolean;
		public var qualifiable:Boolean;
		public var qualifier:String;
		
		public function CodeListItem(code:String = null, label:String = null, selected:Boolean = false, qualifiable:Boolean = false, qualifier:String = null) {
			this.code = code;
			this.label = label;
			this.selected = selected;
			this.qualifiable = qualifiable;
			this.qualifier = qualifier;
		}
	}
}