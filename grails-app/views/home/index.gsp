<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="semantic"/>
		<title>Home - Engine</title>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
		<script src="//cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.4.1/jquery.timeago.min.js"></script>
		<asset:javascript src="graph.js" />
		<asset:javascript src="algorithm/show.js" />
		<asset:javascript src="algorithmRequest.js" />
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.css" type="text/css">
	</head>
	<body>
		<div class="ui segment">
			<h2 class="ui header">Recent Runs</h2>
			<sec:ifLoggedIn>
				<g:if test="${ recentRuns.size() == 0 }">
					<h3 class="ui center aligned icon header">
						<i class="wait icon"></i>
						No Recent Runs
					</h3>
				</g:if>
				<g:else>
					<div class="ui one cards">
						<g:each in="${ recentRuns.sort { it.dateCreated }.reverse() }" var="result">
							<g:render template="/layouts/resultCard" model="${ [result: result, title: true, requestLink: true] }" />
						</g:each>
					</div>
				</g:else>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h3 class="ui center aligned icon header">
					<i class="lock icon"></i>
					Unauthorized
					<div class="sub header">Please log in to see recent runs</div>
				</h3>
			</sec:ifNotLoggedIn>
		</div>
		<script>
			$(function() {
				init();
				$('.data-chart').toArray().forEach(function(me) {
					var id = $(me).attr('id').split('-')[1];
					getData(id, lineGraph, '#chart-' + id, '#pending-' + id, true);
				});
				setInterval(refreshAllResultCards, 30000);
			});
		</script>
	</body>
</html>
