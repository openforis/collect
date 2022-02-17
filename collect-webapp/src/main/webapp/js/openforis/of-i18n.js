OF.i18n = {};

OF.i18n.MESSAGE_KEY_DATA_PROPERTY_NAME = "i18n";

OF.i18n.initializeAll = function(el) {
	if (! el) {
		el = document;
	}
	$(el).find("[data-" + OF.i18n.MESSAGE_KEY_DATA_PROPERTY_NAME + "]").each(function(index, subEl) {
		OF.i18n.initializeEl(subEl);
	});
};

OF.i18n.initializeEl = function(el) {
	var messageKey = $(el).data(OF.i18n.MESSAGE_KEY_DATA_PROPERTY_NAME);
	if (messageKey) {
		var message = OF.i18n.prop(messageKey);
		$(el).html(message);
	}
};

OF.i18n.prop = function(messageKey) {
	var args = Array.prototype.slice.call(arguments, 1);
	var message = jQuery.i18n.prop(messageKey, args);
	return message;
}

OF.i18n.currentLocale = function() {
	var browserLang = jQuery.i18n.browserLang();
	return browserLang.replace("-", "_");
}