<%@page import="org.openforis.collect.Collect"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
  <head>
    <title>OpenForis Collect</title>
  	<meta http-equiv="Pragma" content="no-cache">
  	<script type="text/javascript" src="script/jquery-1.6.2.min.js"></script>
    <script type="text/javascript" src="script/sessionping.js"></script>
    <style type="text/css" media="screen">
		html,body {
			height: 100%;
		}
		
		body {
			font-family: verdana, serif, monospace;
			font-size: 1.0em;
			margin: 0 5 5 5;
			padding: 0 10 10 10;
			overflow: hidden;
			text-align: center;
			background-color: #F0F0F0;
		}
		
		#header {
			position: relative;
			width: 974px; 
			height: 102px;
		}
		
		#mainContainer {
			margin: auto;
			width: 974px;
			height: 100%;
			background-color: #FFFFFF;
		}
		
		#internalContainer {
			min-height: 100%; 
			height: 100%;
			padding-bottom: 30px;
		}
		
		#internalContainer:after {
		    content:" ";
		    display:block;
		    clear:both;
		}

		#footer {
			position: absolute;
			bottom: 0px;
			text-align: left;
			height: 30px;
			width: 974px;
			/*background: url("assets/images/footer.jpg");*/
		}
		
		#footer label {
			font-size: 0.7em;
			font-style: italic;
			color: #EEE;
			position: absolute;
			left: 10px;
			top: 10px;
			z-index: 2;
		}
		
		form {
			padding-top: 40px;
		}
		
		fieldset {
			width: 80%;
			padding-top: 10px;
		}
		
		legend {
			font-weight: bold;
			font-size: 0.8em;
		}
		
		.login td {
			font-size: small;
			font-family: verdana, serif, monospace;
			line-height: 1em;
			font-size: 0.7em;
			padding-top: 10;
		}
		
		input.button {
			font-family: verdana, serif, monospace;
			line-height: 1em;
			font-size: 1em;
			padding: 3 8 5 8;
			background-color: #DDDDDD;
			border: 1px outset #DDDDDD;
			color: #000000;
			border-style: outset;
		}
		
		.login input {
			height: 20;
			border: 1px solid #DDDDDD;
			font-family: verdana, serif, monospace;
			line-height: 1em;
			font-size: 1em;
		}
		
		div.error {
			font-weight: bold;
			font-size: 0.8em;
			color: red;
		}
		
		div.warn {
			font-weight: bold;
			font-size: 0.8em;
			color: blue;
		}
	</style>
	<script type="text/javascript">
		$(function() {
			var DOWNLOAD_LOGO_URL = "downloadLogo.htm";

			var loadImage = function ( elId, position, defaultImageUrl ) {
				var imgEl = document.getElementById(elId);
				var tmpImg = new Image();
				tmpImg.onerror = function() {
					imgEl.src = defaultImageUrl;
				};
				tmpImg.onload = function() {
					imgEl.src = this.height > 0 ? this.src : defaultImageUrl;
				};
				tmpImg.src = DOWNLOAD_LOGO_URL + "?position=" + position + "&time=" + new Date().getTime();
			};

			loadImage("headerImg", "header", "assets/images/header.jpg");
			loadImage("logoImg", "top_right", "assets/images/default-logo.png");
			loadImage("footerImg", "footer", "assets/images/footer.jpg");
		});
	</script>
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
				</table>
			</form>
		</div>
		<!-- FOOTER -->
		<div id="footer">
			<img id="footerImg" alt="Loading..." />
			<label>Application version: <%= org.openforis.collect.Collect.getVersion()%></label>
		</div>
  	</div>
  </body>
</html>
