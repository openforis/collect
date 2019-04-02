OF.UI = function() {};

OF.UI._MESSAGE_CONTAINER_TEMPLATE = 
	'<div class="col-md-4 col-md-offset-4 alert alert-dismissable" style="display: none;">' +
		'<button type="button" class="close no-background" data-dismiss="messageContainer">×</button>' +
		'<span></span>' +
	'</div>';

OF.UI._messageContainer = null;

OF.UI._CONFIRM_CONTAINER_TEMPLATE = 
	'<div id="of-confirm-dialog" class="dialog">' +
		'<div class="content"></div>' +
		'<div class="footer">' +
			'<button type="button" class="btn btn-default yes">Yes</button>' +
			'<button type="button" class="btn btn-default no" data-dismiss="modal">No</button>' +
		'</div>' +
	'</div>';

/**
 * Opens a confirm dialog.
 * The dialog will appear in the fixed position specified or it will be horizontally and vertically centered. 
 * 
 * @param message
 * @param yesHandler
 * @param noHandler (optional)
 * @param position (optional) object with top, left, right, bottom numeric values.
 */
OF.UI.confirm = function(message, yesHandler, noHandler, position) {
	 var dialog = $(OF.UI._CONFIRM_CONTAINER_TEMPLATE);
	 dialog.find(".content").text(message);
	 var $yesBtn = dialog.find(".yes");
	 $yesBtn.click(function(event){
		 dialog.remove();
		 if ( yesHandler) {
			 yesHandler();
		 }
	 });
	 var $noBtn = dialog.find(".no");
	 $noBtn.click(function(event){
	 	dialog.remove();
		if (noHandler) {
			noHandler();
		}
	 });
	 dialog.css({
		 position: "fixed"
	 });
	 
	 if ( position ) {
		 dialog.css({
			top: position.top,
			left: position.left,
			right: position.right,
			bottom: position.bottom
		 });
	 } else {
		 dialog.css({
		 	top: "30%",
		 	left: "50%"
		 });
	 }
	 $("body").append(dialog);
 };

/**
 * Shows application error message
 */
 OF.UI.showError = function( message, hide ) {
	 OF.UI.showMessage("error", message, hide);
};

 /**
  * Shows application warning message
  */
OF.UI.showWarning = function( message, hide ) {
	OF.UI.showMessage("warning", message, hide);
};

 /**
  * Shows application success  message
  */
OF.UI.showSuccess = function(message, hide) {
	OF.UI.showMessage("success", message, hide);
};

/**
 * Shows application message
 */
OF.UI.showMessage = function(type, message, autoHide) {
	var container = OF.UI._messageContainer;
	if (container == null) {
		container = $(OF.UI._MESSAGE_CONTAINER_TEMPLATE);
		$(document.body).append(container);
		
		container.find(".close")
			.click(function(e){	
				container.fadeOut(800);
			});
		OF.UI._messageContainer = container;
	}
	container.removeClass("alert-danger alert-warning alert-success");
 	
 	switch ( type ) {
 	case "error":
 		alertClass = "alert-danger";
 		break;
 	case "warning":
 		alertClass = "alert-warning";
 		break;
 	default:
 		alertClass = "alert-success";
 	}
 	
 	container.addClass(alertClass);

 	container.find("span").html( message );
 	
 	container.fadeIn( 400 );
 	
 	if ( autoHide == true ) {
 		// fade out after 2 seconds
 		container.delay( 2000 ).fadeOut( 800 );
 	}
 };