<%@page import="org.openforis.collect.Collect"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
  <head>
    <title>Open Foris Collect</title>
    
  	<meta http-equiv="Pragma" content="no-cache">
  	
  	<link rel="shortcut icon" href="assets/images/favicon.ico" />
  	
  	<script type="text/javascript" src="script/jquery-1.6.2.min.js"></script>
    <script type="text/javascript" src="script/sessionping.js"></script>
    <script type="text/javascript" src="js/jquery/jquery-i18n-properties/1.2.0/jquery.i18n.properties.js"></script>
    <script type="text/javascript" src="js/openforis/of.js"></script>
    <script type="text/javascript" src="js/openforis/of-i18n.js"></script>
    
<!--     <script type="text/javascript" src="script/js.cookie-2.0.3.min.js"></script> -->
<!--     <script type="text/javascript" src="script/of_collect_cookie.js"></script> -->
	<script type="text/javascript">
		$(function() {
			var DOWNLOAD_LOGO_URL = "downloadLogo.htm";

			jQuery.i18n.properties({
			    name:'messages', 
			    path:'assets/bundle/', 
			    mode:'both', // We specified mode: 'both' so translated values will be available as JS vars/functions and as a map
			    checkAvailableLanguages: true
			});
			
			OF.i18n.initializeAll();

			loadImages(DOWNLOAD_LOGO_URL);
 			
			checkDefaultPasswordActive();

 			document.f.username.focus();
		});
		
		var loadImages = function(downloadLogoUrl) {
			var loadImage = function ( elId, position, defaultImageUrl ) {
				var imgEl = document.getElementById(elId);
				var tmpImg = new Image();
				tmpImg.onerror = function() {
					imgEl.src = defaultImageUrl;
				};
				tmpImg.onload = function() {
					imgEl.src = this.height > 0 ? this.src : defaultImageUrl;
				};
				tmpImg.src = downloadLogoUrl + "?position=" + position + "&time=" + new Date().getTime();
			};

			loadImage("headerImg", "header", "assets/images/header.jpg");
			loadImage("logoImg", "top_right", "assets/images/default-logo.png");
			loadImage("footerImg", "footer", "assets/images/footer.jpg");
		};
		
		var checkDefaultPasswordActive = function() {
			$.ajax({
				url: "default-password-active.json"
			}).done(function(defaultPasswordActive) {
				$("#defaultPasswordActiveContainer").toggle(defaultPasswordActive);
			});
// 			var cookie = OF.Collect.getCookie();
// 			if (cookie == null) {
// 				$("#firstTimeLoginAdviceDiv").show();
// 				OF.Collect.initCookie();
// 			}
		};
	</script>
    <link rel="stylesheet" type="text/css" href="assets/login.css" />
  </head>

  <body>
  	<div id="mainContainer">
  		<!-- HEADER -->
  		<div id="header">
			<img id="headerImg" alt="Loading..."
				style="position: absolute; left: 0px;"></img>
			<img id="logoImg" alt="Loading..." width="80px" height="80px" 
				style="position: absolute; right: 0px; top: 10px;" ></img>
    	</div>
    	<!-- INTERNAL CONTAINER -->
		<div id="internalContainer">
			<div>
	    		<c:if test="${not empty param.session_expired}">
			      <div class="warn" data-i18n="collect.login.session_expired"></div>
			      <br/>
			    </c:if>
			    <c:if test="${param.login_error == 1}">
			      <div class="error">
			      	<span data-i18n="collect.login.unsuccessfull_login"></span>
			      	<br/><br/>
			      	<span data-i18n="collect.login.unsuccessfull_login_reason"></span>
			        <span>: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.</span>
			       </div>
			    </c:if>
	    	</div>
	    	
	    	<c:url var="loginUrl" value="/login"/>
			<form name="f" action="${loginUrl}" method="POST">
				<input type="hidden" 
					name="${_csrf.parameterName}"
					value="${_csrf.token}"/>
				<table class="login" width="100%" align="center" style="vertical-align: top; height: 100">
					<c:if test="${param.logout != null}">
						<tr>
							<td data-i18n="collect.login.logged_out"></td>
						</tr>
					</c:if>
					<tr>
						<td colspan="2" align="center"><strong style="font-size: 13px;" data-i18n="collect.login.please_login"></strong></td>
					</tr>
					<tr>
						<td width="50%" align="right">
							<span data-i18n="collect.login.username"></span>:
						</td>
						<td width="50%" align="left">
							<input type='text' name='username'
								value='<c:if test="${param.login_error == 1}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>' />
						</td>
					</tr>
					<tr>
						<td width="50%" align="right">
							<span data-i18n="collect.login.password"></span>:
						</td>
						<td width="50%" align="left">
							<input type='password' name='password'>
						</td>
					</tr>
					<!-- tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td>Don't ask for my password for two weeks</td></tr-->
					<tr>
						<td colspan='2' width="100%" style="text-align: center;">
							<button type="submit" class="button" data-i18n="collect.login.login"></button> 
						</td>
					</tr>
					<tr><td></td></tr>
					<tr><td></td></tr>
					<tr id="defaultPasswordActiveContainer">
						<td colspan="2" align="center" style="font-size: 10px;">
							<i><span>*</span><span data-i18n="collect.login.default_user_message"></span></i>
						</td>
					</tr>
				</table>
			</form>
		</div>
		<!-- FOOTER -->
		<div id="footer">
			<img id="footerImg" alt="Loading..." />
			<label>
				<span data-i18n="collect.global.application_version"></span><span>: <%= org.openforis.collect.Collect.VERSION %></span>
			</label>
		</div>
  	</div>
  </body>
</html>
