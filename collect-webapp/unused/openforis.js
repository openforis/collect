/**
 * @author S. Ricci
 */
var OPENFORIS = {
	CLEAR_ACTIVE_RECORD_PATH: "clearActiveRecord.htm",
	
	FLASH_OBJECT_ID: "collect",
	
	editingRecord: false,
	preview: false,
			
	init: function() {
		//init beforeunload and unload event listeners
		window.onbeforeunload = OPENFORIS.onBeforeUnloadFun;
		window.onunload = OPENFORIS.onUnloadFun;

		//init mouse down handler
		/*
		if(navigator.appName == "Netscape") {
			document.captureEvents(Event.MOUSEDOWN);
			document.addEventListener("mousedown", OPENFORIS.onNsRightClick, true);
		} else {
			document.onmousedown = OPENFORIS.onIeRightClick;
		}
		*/
	},

	getFlexApp: function() {
		return document.getElementById(OPENFORIS.FLASH_OBJECT_ID);
	},
	
	onBeforeUnloadFun: function() {
		//if there is an item being edited, show a confirm before exiting page

		if ( OPENFORIS.isEditingRecord() && ! OPENFORIS.isPreview() ) {
			var mainApp = OPENFORIS.getFlexApp();
			var leavingPageMessage = mainApp && mainApp.getLeavingPageMessage && mainApp.getLeavingPageMessage();
			return leavingPageMessage;
		}
	},
	
	onUnloadFun: function() {
		//unlock current item being edited (if any)
		if ( OPENFORIS.isEditingRecord() ) {
			$.ajax({
			  async: false,
			  type: "POST",
			  url: OPENFORIS.CLEAR_ACTIVE_RECORD_PATH,
			  success: function(){
				//do nothing
			  }
			});
		}
	},
	
	isEditingRecord: function() {
		return this.editingRecord;
	},
	
	setEditingRecord: function(editing) {
		this.editingRecord = editing;
	},
	
	isPreview: function() {
		return this.preview;
	},
	
	setPreview: function(preview) {
		this.preview = preview;
	},
	
	onNsRightClick: function(e){
		if(e.which == 3){
			OPENFORIS.killEvent(e);
			OPENFORIS.getFlexApp().openContextMenu();
		}
		return false;
	},

	onIeRightClick: function() {
		if ( event.button == 2 ){
			OPENFORIS.getFlexApp().openContextMenu();
			
			//TO-DO stop event propagation
			throw new Error("");
			/*
			event.stopPropagation();
			return false;
			*/
		}
		
	},
	
	killEvent: function(eventObject) {
		if(eventObject) {
			if (eventObject.stopPropagation) { eventObject.stopPropagation(); }
			if (eventObject.preventDefault) { eventObject.preventDefault(); }
			if (eventObject.preventCapture) { eventObject.preventCapture(); }
		    if (eventObject.preventBubble) { eventObject.preventBubble(); }
		}
	}
};

