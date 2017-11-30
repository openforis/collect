<%@page import="org.openforis.collect.Collect"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>
<% response.setHeader("X-Frame-Options", "ALLOWALL"); %>
<html lang="en">
<head>
  	<meta charset="UTF-8" http-equiv="Pragma" content="no-cache">
	
	<title>Open Foris Collect - Login</title>
	
  	<link rel="shortcut icon" href="assets/images/favicon.ico" />
	<link rel="stylesheet" href="assets/css/login-style.css">

	<script type="text/javascript" src='assets/js/jquery/3.2.1/jquery-3.2.1.min.js'></script>
    <script type="text/javascript" src="assets/js/jquery/jquery-i18n-properties/1.2.0/jquery.i18n.properties.js"></script>
    <script type="text/javascript" src="assets/js/openforis/of.js"></script>
    <script type="text/javascript" src="assets/js/openforis/of-i18n.js"></script>
    <script type="text/javascript" src="assets/js/sessionping.js"></script>
	<script type="text/javascript" src="assets/js/login.js"></script>
</head>

<body>
	<hgroup>
		<h1>
			<span>Open Foris Collect</span>
		</h1>
		<c:if test="${param.logout != null}">
			<script>LOGGED_OUT = true</script>
			<h3 data-i18n="collect.login.logged_out"></h3>
		</c:if>
		<c:if test="${param.logout == null}">
			<script>LOGGED_OUT = true</script>
		</c:if>
		<h3 data-i18n="collect.login.please_login"></h3>
	</hgroup>

	<c:url var="loginUrl" value="/login"/>
	
	<form id="loginForm" name="f" action="${loginUrl}" method="POST">
		<input type="hidden" 
			name="${_csrf.parameterName}"
			value="${_csrf.token}"/>
			
		<div class="group">
			<c:if test="${not empty param.session_expired}">
				<div class="warn" data-i18n="collect.login.session_expired"></div>
				<br />
			</c:if>
			<c:if test="${param.login_error == 1}">
				<div class="error">
					<span data-i18n="collect.login.unsuccessfull_login"></span> <br />
				</div>
			</c:if>
		</div>
			
		<div class="group">
			<input type='text' name='username' 
				value='<c:if test="${param.login_error == 1}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>' />
			<span class="highlight"></span>
			<span class="bar"></span> 
			<label data-i18n="collect.login.username"></label>
		</div>
		<div class="group">
			<input type='password' name='password'>
			<span class="highlight"></span>
			<span class="bar"></span> 
			<label data-i18n="collect.login.password"></label>
		</div>
		<button type="submit" class="button buttonBlue">
			<span data-i18n="collect.login.login"></span>
			<div class="ripples buttonRipples">
				<span class="ripplesCircle"></span>
			</div>
		</button>
	</form>
	<footer>
		<p>Powered by <a href="http://www.openfoirs.org/" target="_blank">Open Foris</a></p>
	</footer>
</body>
</html>
