<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="semantic"/>
		<title>TheConnMan</title>
	</head>
	<body>
		<div class="ui segment">
			<sec:ifLoggedIn>
				<sec:username />
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<oauth:connect provider="github">Connect to GitHub</oauth:connect>
			</sec:ifNotLoggedIn>
			<h1>Welcome to TheConnMan's Grails Base</h1>
		</div>
	</body>
</html>
