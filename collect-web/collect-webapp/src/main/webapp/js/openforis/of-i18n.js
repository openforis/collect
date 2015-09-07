OF.i18n = {};

OF.i18n.initAll = function(el) {
	if (! el) {
		el = document;
	}
	$(el).find("[data-messagekey]").each(function(index, subEl) {
		OF.i18n.initEl(subEl);
	});
};

OF.i18n.initEl = function(el) {
	var messageKey = $(el).data("messagekey");
	if (messageKey) {
		var message = jQuery.i18n.prop(messageKey);
		$(el).html(message);
	}
};