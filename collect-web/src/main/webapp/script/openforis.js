/**
 * @author S. Ricci
 */
var OPENFORIS = {
	CLEAR_ACTIVE_RECORD_PATH: "clearActiveRecord.htm",
	
	FLASH_OBJECT_ID: "collect",
			
	isEditingRecord: false,
	
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
		var mainApp = OPENFORIS.getFlexApp();
		var leavingPageMessage = mainApp.getLeavingPageMessage();
		OPENFORIS.editingRecord = mainApp != null && mainApp.isEditingRecord();
		var preview = OPENFORIS.editingRecord && mainApp.isPreview();
		if(OPENFORIS.editingRecord && ! preview) {
			return leavingPageMessage;
		}
	},
	
	onUnloadFun: function() {
		//unlock current item being edited (if any)
		if(OPENFORIS.editingRecord) {
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

