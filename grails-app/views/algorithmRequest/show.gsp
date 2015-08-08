<!DOCTYPE html>
<html>
	<head>
		<title>Requests</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
		<script src="//cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.4.1/jquery.timeago.min.js"></script>
		<asset:javascript src="graph.js" />
		<asset:javascript src="algorithm/show.js" />
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.css" type="text/css">
	</head>
	<body>
		<div class="ui segment">
			<h1 class="ui floated left header">${ algorithm.name }</h1>
			<g:link controller="algorithmRequest" action="create" id="${ algorithm.id }" class="ui positive button" style="float: right;">Edit Request</g:link>
			<button class="ui primary button" onclick="kickOff(this, ${ algorithm.id })" style="float: right;">Kick Off Evaluation</button>
			<h2 class="ui header" style="clear: both;">Results</h2>
			<div class="ui one cards">
				<g:each in="${ algorithm.algorithmResults.sort { it.dateCreated }.reverse() }" var="result">
					<g:render template="/layouts/resultCard" model="${ [result: result] }" />
				</g:each>
			</div>
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
