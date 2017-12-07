$(window, document, undefined).ready(function() {

	const DEFAULT_PASSWORD_ACTIVE_ENDPOINT = "api/defaultpasswordactive"
	const DEFAULT_USERNAME = 'admin';
	const DEFAULT_PASSWORD = 'admin';
	
	if (LOGGED_OUT || ERROR) {
		initializeForm();
	} else {
		checkDefaultPasswordActive();
	}
	
	function checkDefaultPasswordActive() {
		$.ajax({
			url: DEFAULT_PASSWORD_ACTIVE_ENDPOINT
		}).done(function(defaultPasswordActive) {
			setTimeout(function() {
				if (defaultPasswordActive) {
					submitFormWithDefaultUsernameAndPassword();
				} else {
					initializeForm();
				}
			}, 200);
		});
	};
	
	function submitFormWithDefaultUsernameAndPassword() {
		var form = document.f;
		form.username.value = DEFAULT_USERNAME;
		form.password.value = DEFAULT_PASSWORD;
		form.submit();
	}
	
	function initializeForm() {
		jQuery.i18n.properties({
			name : 'messages',
			path : 'assets/bundle/',
			mode : 'both', // We specified mode: 'both' so
							// translated values will be available
							// as JS vars/functions and as a map
			checkAvailableLanguages : true
		});

		OF.i18n.initializeAll();

		document.f.username.focus();

		//set input fields as 'used' if fields have been filled by browser autofill
		setTimeout(function() {
			if ($(document.f.username).val().length > 0) {
				$(document.f.username).addClass('used');
				$(document.f.password).addClass('used');
			}
		}, 500);
		
		
		function toggleInputUsedClass() {
			var $this = $(this);
			var empty = $this.val().length === 0;
			$this.toggleClass('used', !empty);
		}
		
		$('input').blur(toggleInputUsedClass);

		var $ripples = $('.ripples');

		$ripples.on('click.Ripples', function(e) {

			var $this = $(this);
			var $offset = $this.parent().offset();
			var $circle = $this.find('.ripplesCircle');

			var x = e.pageX - $offset.left;
			var y = e.pageY - $offset.top;

			$circle.css({
				top : y + 'px',
				left : x + 'px'
			});

			$this.addClass('is-active');

		});

		$ripples.on('animationend webkitAnimationEnd mozAnimationEnd oanimationend MSAnimationEnd', function(e) {
			$(this).removeClass('is-active');
		});

		$('body').addClass('loaded');
	}
});

/*


*/