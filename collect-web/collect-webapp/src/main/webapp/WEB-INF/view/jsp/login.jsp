<%@page import="org.openforis.collect.Collect"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page pageEncoding="UTF-8"%>
<% response.setHeader("X-Frame-Options", "ALLOWALL"); %>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="Pragma" content="no-cache">
	<meta name="viewport"
		content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="msapplication-tap-highlight" content="no">
	
	<title>Open Foris Collect - Login</title>
	
	<link rel="shortcut icon" href="assets/images/favicon.ico" />
	<link rel="stylesheet" href="assets/css/login-style.css">
	
	<!-- CORE CSS-->
	
	<link href="assets/css/materialize/1.0.0-alpha2/materialize.min.css"
		type="text/css" rel="stylesheet" media="screen,projection">
	<link href="assets/css/material-icons.css" type="text/css"
		rel="stylesheet">
	<link href="assets/css/layouts/page-center.css" type="text/css"
		rel="stylesheet" media="screen,projection">
	<link href="assets/css/preloader.css" type="text/css"
		rel="stylesheet" media="screen,projection">
	<link href="assets/css/login-style.css" type="text/css" rel="stylesheet"
		media="screen,projection">
</head>

<body>
	<!-- Start Page Loading -->
	<div id="loader-wrapper">
		<div id="loader"></div>
		<div class="loader-section section-left"></div>
		<div class="loader-section section-right"></div>
	</div>
	<!-- End Page Loading -->

	<div id="login-page" class="row">
		<div class="col s12 z-depth-4 card-panel">
			<c:url var="loginUrl" value="/login" />
			<form id="login-form" class="login-form" name="f" action="${loginUrl}" method="POST">
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />

				<div class="row">
					<div class="input-field col s12 center">
						<img src="assets/images/of_collect_logo.png" alt=""
							class="circle responsive-img valign profile-image-login">
						<h2 class="center login-form-text">Open Foris Collect</h2>
						<c:if test="${param.logout}">
							<h4 data-i18n="collect.login.logged_out"></h4>
						</c:if>
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
				</div>
				<div class="row margin">
					<div class="input-field col s12">
						<i class="material-icons prefix">person_outline</i>
						<input id="username" name="username" type="text"> 
						<label for="username" class="center-align" 
							data-i18n="collect.login.username"></label>
					</div>
				</div>
				<div class="row margin">
					<div class="input-field col s12">
						<i class="material-icons prefix">lock_outline</i> 
						<input id="password" name="password" type="password"> 
						<label for="password" data-i18n="collect.login.password"></label>
					</div>
				</div>
				<!--         <div class="row">           -->
				<!--           <div class="input-field col s12 m12 l12  login-text"> -->
				<!--               <input type="checkbox" id="remember-me" /> -->
				<!--               <label for="remember-me">Remember me</label> -->
				<!--           </div> -->
				<!--         </div> -->
				<div class="row">
					<div class="input-field col s12">
						<button type="submit" class="btn waves-effect waves-light col s12" data-i18n="">Login</button>
					</div>
				</div>
				<!--         <div class="row"> -->
				<!--           <div class="input-field col s6 m6 l6"> -->
				<!--             <p class="margin medium-small"><a href="page-register.html">Register Now!</a></p> -->
				<!--           </div> -->
				<!--           <div class="input-field col s6 m6 l6"> -->
				<!--               <p class="margin right-align medium-small"><a href="page-forgot-password.html">Forgot password ?</a></p> -->
				<!--           </div>           -->
				<!--         </div> -->
			</form>
		</div>
	</div>

	<!-- ================================================
		Scripts
	================================================ -->
	<script>
		LOGGED_OUT = <c:out value="${param.logout == true || param.logout == 1}" />;
		ERROR = <c:out value="${param.login_error == true || param.login_error == 1}" />;
	</script>
	<!--materialize js-->
	<script type="text/javascript"
		src="assets/js/materialize/1.0.0-alpha2/materialize.min.js"></script>
	<script type="text/javascript"
		src='assets/js/jquery/3.2.1/jquery-3.2.1.min.js'></script>
	<script type="text/javascript"
		src="assets/js/jquery/jquery-i18n-properties/1.2.0/jquery.i18n.properties.js"></script>
	<script type="text/javascript" src="assets/js/openforis/of.js"></script>
	<script type="text/javascript" src="assets/js/openforis/of-i18n.js"></script>
	<script type="text/javascript" src="assets/js/sessionping.js"></script>
	<script type="text/javascript" src="assets/js/login.js"></script>
</body>
</html>
