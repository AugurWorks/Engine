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
		<%@ page import="com.augurworks.engine.helper.AlfredEnvironment" %>
		<%@ page import="com.augurworks.engine.helper.AlgorithmType" %>
		<div class="ui segment">
			<h1 class="ui floated left header">${ algorithm.name }</h1>
            <i id="info-check" class="big green check circle icon" style="display: none; float: right;"></i>
			<div style="clear: both; padding-bottom: 1em;">
                <input type="hidden" id="id" value="${ algorithm.id }" />
                <g:select name="modelType" class="ui dropdown" from="${ AlgorithmType.values()*.name }"></g:select>
                <button class="ui primary button" onclick="kickOff(this, ${ algorithm.id })">Kick Off Evaluation</button>
                <g:link controller="algorithmRequest" action="create" id="${ algorithm.id }" class="ui positive button">Edit</g:link>
                <button onclick="deleteRequest()" class="ui negative button">Delete</button>
                <g:link controller="graph" action="line" id="${ algorithm.id }" class="ui button">Graph</g:link>
            </div>
			<div class="ui form" style="clear: both;">
				<h3 class="ui dividing header">Edit Info</h3>
				<g:field type="hidden" name="id" value="${ algorithm?.id }" />
				<div class="four fields">
					<div class="field">
						<label>Alfred Environment</label>
						<g:select from="${ AlfredEnvironment.values()*.name }" name="alfredEnvironment" class="ui search dropdown" value="${ algorithm?.alfredEnvironment?.name }" />
					</div>
					<div class="field">
						<label>Cron Algorithms</label>
						<g:select from="${ AlgorithmType.values()*.name }" name="cronAlgorithms" class="ui search dropdown" multiple="true" value="${ algorithm?.cronAlgorithms*.name }" />
					</div>
					<div class="field">
						<label>Cron Expression (<a href="http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger" target="_blank">Help</a>)</label>
						<g:field type="text" name="cronExpression" value="${ algorithm ? algorithm?.cronExpression : '0 0 3 ? * *' }" placeholder="0 0 3 ? * *" />
					</div>
					<div class="field">
						<label>Tags</label>
						<g:field type="text" name="tags" value="${ algorithm?.tags*.name?.join(', ') }" />
					</div>
				</div>
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
				$('#cronAlgorithms').dropdown();
                $('#alfredEnvironment, #cronAlgorithms, #cronExpression, #tags').change(function() {
                    saveRequest();
                    $('#info-check').show();
                    setTimeout(function() {
                        $('#info-check').fadeOut();
                    }, 1000);
                });
			});

			function getAdditional(algorithmResultId) {
				getMore(algorithmResultId, page++)
			}
		</script>
	</body>
</html>
