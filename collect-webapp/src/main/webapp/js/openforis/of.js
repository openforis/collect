OF = function() {};

/**
 * Spring Security CSRF token, set by the server in the XSRF-TOKEN cookie: to be sent back in the X-XSRF-TOKEN header
 * (or in the _csrf parameter of a form) of state-changing requests.
 */
OF.csrfToken = function() {
	var match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
	return match ? match[1] : "";
};