<%@page import="org.openforis.collect.Collect"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
  <head>
    <title>OpenForis Collect</title>
  	<meta http-equiv="Pragma" content="no-cache">
  	<script type="text/javascript" src="script/jquery-1.6.2.min.js"></script>
    <script type="text/javascript" src="script/sessionping.js"></script>
<!--     <script type="text/javascript" src="script/js.cookie-2.0.3.min.js"></script> -->
<!--     <script type="text/javascript" src="script/of_collect_cookie.js"></script> -->
	<script type="text/javascript">
		$(function() {
			var DOWNLOAD_LOGO_URL = "downloadLogo.htm";
			loadImages(DOWNLOAD_LOGO_URL);
// 			checkFirstTimeLogin();
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
		
// 		var checkFirstTimeLogin = function() {
// 			var cookie = OF.Collect.getCookie();
// 			if (cookie == null) {
// 				$("#firstTimeLoginAdviceDiv").show();
// 				OF.Collect.initCookie();
// 			}
// 		};
	</script>
    <link rel="stylesheet" type="text/css" href="assets/login.css" />
  </head>

  <body onload="document.f.j_username.focus();">
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
			      <div class="warn">
			        Your session has expired.<br/>
			      </div>
			    </c:if>
			    <c:if test="${not empty param.login_error}">
			      <div class="error">
			        Your login attempt was not successful, try again.<br/><br/>
			        Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
			      </div>
			    </c:if>
	    	</div>
			<form name="f" action="<c:url value='j_spring_security_check'/>" method="POST">
				<table class="login" width="100%" align="center" style="vertical-align: top; height: 100">
					<tr>
						<td colspan="2" align="center"><strong style="font-size: 13px;">Please Log In</strong></td>
					</tr>
					<tr>
						<td width="50%" align="right">User:</td>
						<td width="50%" align="left">
							<input type='text' name='j_username'
								value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>' />
						</td>
					</tr>
					<tr>
						<td width="50%" align="right">Password:</td>
						<td width="50%" align="left">
							<input type='password' name='j_password'>
						</td>
					</tr>
					<!-- tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td>Don't ask for my password for two weeks</td></tr-->
					<tr>
						<td colspan='2' width="100%" style="text-align: center;">
							<input name="submit" type="submit" class="button" value="Login" /> 
						</td>
					</tr>
					<tr><td></td></tr>
					<tr><td></td></tr>
					<tr id="firstTimeLoginAdviceDiv">
						<td colspan="2" align="center" style="font-size: 10px;">
							<i>*Default user/password : <b>admin</b>/<b>admin</b></i>
						</td>
					</tr>
				</table>
			</form>
		</div>
		<!-- FOOTER -->
		<div id="footer">
			<img id="footerImg" alt="Loading..." />
			<label>Application version: <%= org.openforis.collect.Collect.VERSION %></label>
		</div>
  	</div>
  </body>
</html>
