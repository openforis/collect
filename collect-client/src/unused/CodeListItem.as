package org.openforis.collect.ui.component.input
{
	import mx.collections.ArrayList;
	import mx.collections.IList;
	
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;

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
		
		public static function fromList(list:IList, attributes:IList):IList {
			var result:IList = new ArrayList();
			for each(var item:CodeListItemProxy in list) {
				var codeItem:CodeListItem = new CodeListItem();
				codeItem.code = item.code;
				codeItem.label = item.code + " - " + item.getLabelText();
				codeItem.qualifiable = item.qualifiable;
				//TODO set selected and qualifier
				result.addItem(codeItem);
			}
			return result;
		}
	}
}