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
		<script src="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/1.11.4/semantic.min.js"></script>
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/1.11.4/semantic.min.css" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'static/css', file: 'custom.css')}" type="text/css">

		<g:layoutHead/>
		<r:layoutResources />
	</head>

	<body>
		<div class="ui segment">
			<div class="ui two column grid">
				<div class="column">
					<a href="/">
						<img class="ui image" src="${resource(dir: 'static/images', file: 'grails_logo.png')}" style="max-height: 80px;" />
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
						<i class="icon setting"></i>
						Admin Actions<i class="icon dropdown"></i>
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
				</sec:ifAllGranted>
				<div class="right menu">
					<sec:ifLoggedIn>
						<div class="item">
							 <sec:username/>
						</div>
						<aw:avatar class="ui circular image" style="width: 35px; float: right;" />
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

		<g:javascript library="application"/>
		<r:layoutResources />
		<script>
			$(function() {
				$('.ui.dropdown').dropdown({
					on: 'hover'
				});
			});
		</script>
	</body>
</html>