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
		<asset:javascript src="algorithmRequest.js" />
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.css" type="text/css">
	</head>
	<body>
		<%@ page import="com.augurworks.engine.helper.AlgorithmType" %>
		<div class="ui segment">
			<h1 class="ui header">${ algorithm.name }</h1>
			<input type="hidden" id="id" value="${ algorithm.id }" />
			<g:select name="modelType" class="ui dropdown" from="${ AlgorithmType.values()*.name }"></g:select>
			<button class="ui primary button" onclick="kickOff(this, ${ algorithm.id })">Kick Off Evaluation</button>
			<g:link controller="algorithmRequest" action="create" id="${ algorithm.id }" class="ui positive button">Edit</g:link>
			<button onclick="deleteRequest()" class="ui negative button">Delete</button>
			<g:link controller="graph" action="line" id="${ algorithm.id }" class="ui button">Graph</g:link>
			<h3 class="ui header">Tags</h3>
			<div class="ui labels">
				<g:each in="${ algorithm.tags*.name.sort() }" var="tag">
					<div class="ui basic label">${ tag }</div>
				</g:each>
			</div>
			<g:render template="/layouts/statistics" model="${ [unit: algorithm.unit.name().toLowerCase(), predictedValues: request.algorithmResults*.getFutureValues().flatten()] }" />
			<h2 class="ui header" style="clear: both;">Results</h2>
			<div id="results" class="ui one cards">
				<g:render template="/layouts/resultCards" model="${ [results: algorithmResults] }" />
			</div>
			<g:if test="${ total > algorithmResults.size() }">
				<div class="ui center aligned container" style="margin-top: 1em;">
					<div id="more" class="ui primary button" onclick="getAdditional(${ algorithm.id })">More</div>
				</div>
			</g:if>
		</div>
		<script>
			var total = ${ total };
			var page = 1;
			$(function() {
				initCharts();
				setInterval(refreshAllResultCards, 30000);
			});

			function getAdditional(algorithmResultId) {
				getMore(algorithmResultId, page++)
			}
		</script>
	</body>
</html>
