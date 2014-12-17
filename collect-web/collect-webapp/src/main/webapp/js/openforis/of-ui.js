OF.UI = function() {};

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
	var template = 
		'<div id="of-confirm-dialog" class="dialog">' +
			'<div class="content"></div>' +
			'<div class="footer">' +
				'<button type="button" class="btn btn-default yes">Yes</button>' +
				'<button type="button" class="btn btn-default no" data-dismiss="modal">No</button>' +
			'</div>' +
		'</div>';
	 var dialog = $(template);
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
		 if ( noHandler) {
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
