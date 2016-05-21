<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="Engine"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.1.8/semantic.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/sweetalert/1.0.1/sweetalert.min.js"></script>
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.1.8/semantic.min.css" type="text/css">
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/sweetalert/1.0.1/sweetalert.min.css" type="text/css">
		<asset:stylesheet href="custom.css" />

		<g:layoutHead/>
	</head>

	<body>
		<%@ page import="com.augurworks.engine.domains.AlgorithmResult" %>
		<div class="ui inverted blue segment">
			<div class="ui two column grid">
				<div class="column">
					<a href="/">
						<img class="ui image" src="${resource(dir: 'static/images', file: 'augurworks_logo.png')}" style="max-height: 60px;" />
					</a>
				</div>
			</div>
		</div>
		<div class="menu-wrapper">
			<div class="ui menu">
				<a class="item menu-home" href="/">
					<i class="icon home"></i>
					Home
				</a>
				<sec:ifAllGranted roles="ROLE_ADMIN">
					<g:link controller="algorithmRequest" class="item">
						<i class="icon cubes"></i> List Data Sets
					</g:link>
					<div class="ui dropdown item">
						<i class="icon line chart"></i>
						Graphs <i class="icon dropdown"></i>
						<div class="menu">
							<g:link class="item" controller="graph" action="line">Line Graph</g:link>
						</div>
					</div>
					<div class="ui dropdown item">
						<i class="icon setting"></i>
						Admin Actions <i class="icon dropdown"></i>
						<div class="menu">
							<a class="item" href="/controllers">Internal Controllers</a>
						</div>
					</div>
					<div class="ui dropdown item">
						<i class="building outline icon"></i>
						Scaffolding <i class="icon dropdown"></i>
						<div class="menu">
							<g:link controller="algorithmResult" class="item">Algorithm Result</g:link>
							<g:link controller="dataSet" class="item">Data Set</g:link>
							<g:link controller="predictedValue" class="item">Predicted Value</g:link>
							<g:link controller="requestDataSet" class="item">Request Data Set</g:link>
						</div>
					</div>
					<div class="item" data-title="Algorithms Currently Running">
						<i class="loading refresh icon"></i>
						${ AlgorithmResult.countByComplete(false) }
					</div>
				</sec:ifAllGranted>
				<div class="right menu">
					<sec:ifLoggedIn>
						<div class="item">
							 <sec:username />
						</div>
						<g:if test="${ !pageProperty(name: 'page.hideAvatar') }">
							<aw:avatar class="ui avatar circular image" style="width: 40px; height: 40px; float: right;" />
						</g:if>
					</sec:ifLoggedIn>
					<sec:ifNotLoggedIn>
						<oauth:connect provider="github" class="item">
							<i class="github square icon"></i>
							Log In With GitHub
						</oauth:connect>
					</sec:ifNotLoggedIn>
				</div>
			</div>
		</div>

		<div class="content">
			<g:layoutBody/>
		</div>

		<div class="menu-wrapper">
			<div class="ui segment" style="margin-bottom: 15px;">
				<b>AugurWorks Engine ${ grailsApplication.metadata['app.version'] }</b>
			</div>
		</div>

		<script>
			$(function() {
				$('.menu-wrapper .ui.dropdown').dropdown({
					on: 'hover'
				});
				$('.item[data-title]').popup({
					position: 'top center'
				});
			});
		</script>
	</body>
</html>