package org.openforis.collect.model {


	//TODO: Remove? generated from client?
    [Bindable]
    public class Phase {

        public static const DATA_ENTRY:Phase = new Phase("1", "Data Entry");
        public static const DATA_CLEANSING:Phase = new Phase("2", "Data Cleansing");
        public static const DATA_ANALYSIS:Phase = new Phase("3", "Data Analysis");

		private var _name:String;
		private var _label:String;
		
        function Phase(name:String = null, label:String = null) {
			this._name = name;
			this._label = label;
        }

        public static function get constants():Array {
            return [DATA_ENTRY, DATA_CLEANSING, DATA_ANALYSIS];
        }

		public static function valueOf(name:String):Phase {
			for each(var item:Phase in constants) {
				if(item.name == name) {
					return item;
				}
			}
			return null;
		}

		public function get name():String {
			return _name;
		}
		
		public function get label():String {
			return _label;
		}
		
		
    }
}
