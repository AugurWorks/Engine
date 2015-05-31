<!DOCTYPE html>
<html>
	<head>
		<title>Access Denied</title>
		<meta name="layout" content="semantic">
	</head>
	<body>
		<div class="ui segment">
			<h1 class="ui center aligned icon header">
				<i class="lock icon"></i>
				<div class="content">
					Sorry, you do not have access to that page.
					<sec:ifLoggedIn>
						<div class="sub header">Click <a href="/">here</a> to return to safety</div>
					</sec:ifLoggedIn>
					<sec:ifNotLoggedIn>
						<div class="sub header"><oauth:connect provider="github" class="ui primary button">Log In With GitHub</oauth:connect></div>
					</sec:ifNotLoggedIn>
				</div>
			</h1>
		</div>
	</body>
</html>
