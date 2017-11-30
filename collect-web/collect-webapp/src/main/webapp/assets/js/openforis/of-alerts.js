OF.Alerts = function() {};

OF.Alerts._MESSAGE_CONTAINER_TEMPLATE = 
	'<div class="modal fade">' +
		'<div class="modal-dialog">' +
			'<div class="modal-content">' +
				'<div class="modal-header">' +
					'<button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
					'<h4 class="modal-title"></h4>' +
				'</div>' +
				'<div class="modal-body"></div>' +
				'<div class="modal-footer">' +
					'<button type="button" class="btn btn-default ok-btn">Ok</button>' +
				'</div>' +
			'</div>' +
		'</div>' +
	'</div>';

OF.Alerts._messageContainer = null;

OF.Alerts._CONFIRM_CONTAINER_TEMPLATE = 
	'<div id="of-confirm-dialog" class="modal fade">' +
		'<div class="modal-dialog">' +
			'<div class="modal-content">' +
				'<div class="modal-header">' +
					'<button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
					'<h4 class="modal-title">Confirm</h4>' +
				'</div>' +
				'<div class="modal-body"></div>' +
				'<div class="modal-footer">' +
					'<button type="button" class="btn btn-default yes-btn">Yes</button>' +
					'<button type="button" class="btn btn-default no-btn">No</button>' +
				'</div>' +
			'</div>' +
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
OF.Alerts.confirm = function(message, yesHandler, noHandler, title) {
	 var dialog = $(OF.Alerts._CONFIRM_CONTAINER_TEMPLATE);
	 dialog.find(".modal-body").text(message);
	 
	 var $yesBtn = dialog.find(".yes-btn");
	 $yesBtn.click(function(event){
		 dialog.remove();
		 if ( yesHandler) {
			 yesHandler();
		 }
	 });
	 
	 var $noBtn = dialog.find(".no-btn");
	 $noBtn.click(function(event){
	 	dialog.remove();
		if (noHandler) {
			noHandler();
		}
	 });
	 
	 var $closeBtn = dialog.find(".close");
	 $closeBtn.click(function(event) {
		 dialog.modal('hide'); 
	 });
	 
 	 if (title && title != '') {
		 dialog.find(".modal-title").text(title);
	 }
 	 
 	 dialog.on('hide.bs.modal', function (e) {
 		 dialog.remove();
 	 });
	 
	 var options = {
		 show: true,
		 keyboard: true
	 };
	 dialog.modal(options)
 };

/**
 * Shows application error message
 */
 OF.Alerts.showError = function( message, hide ) {
	 OF.Alerts.showMessage("error", message, hide);
};

 /**
  * Shows application warning message
  */
OF.Alerts.warn = function( message, hide ) {
	OF.Alerts.showMessage("warning", message, hide);
};

OF.Alerts.showWarning = function( message, hide ) {
	OF.Alerts.warn(message, hide);
};

 /**
  * Shows application success  message
  */
OF.Alerts.success = function( message, hide ) {
	OF.Alerts.showMessage("success", message, hide);
};

OF.Alerts.showSuccess = function(message, hide) {
	OF.Alerts.success(message, hide);
};

/**
 * Shows application message
 */
OF.Alerts.showMessage = function(type, message, autoDismiss) {
	var container = OF.Alerts._messageContainer;
	if (container == null) {
		container = $(OF.Alerts._MESSAGE_CONTAINER_TEMPLATE);
		
		container.find(".close").click(function(e) {
			container.modal('hide');
		});
		container.find(".ok-btn").click(function(e) {
			container.modal('hide');
		});
		
		OF.Alerts._messageContainer = container;
	}
	
	//try to look for an internationalized message
	if (OF.i18n) {
		var i18nMessage = OF.i18n.prop(message);
		if (i18nMessage) {
			message = i18nMessage;
		}
	}
	
	var title;
	var alertClass;
	
 	switch ( type ) {
 	case "error":
 		title = "Error";
 		alertClass = "error";
 		break;
 	case "warning":
 		title = "Warning";
 		alertClass = "warning";
 		break;
 	default:
 		title = "Success";
 		alertClass = "success";
 	}
 	
 	container.removeClass("error, warning, success");
 	container.addClass(alertClass);

 	container.find(".modal-title").text(title);
 	container.find(".modal-body").html( message );
 	
 	if ( autoDismiss == true ) {
 		// fade out after 2 seconds
 		container.delay( 2000 ).fadeOut( 800 );
 	}

	var options = {
		show: true,
		keyboard: true,
		backdrop: false
	};
	container.modal(options);
};