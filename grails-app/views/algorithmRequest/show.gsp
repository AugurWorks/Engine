<!DOCTYPE html>
<html>
	<head>
		<title>Requests</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
		<script src="//cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js"></script>
		<asset:javascript src="graph.js" />
		<asset:stylesheet href="c3.css" />
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.4.1/jquery.timeago.min.js"></script>
	</head>
	<body>
		<%@ page import="com.augurworks.engine.helper.Global" %>
		<div class="ui segment">
			<h1 class="ui floated left header">${ algorithm.name }</h1>
			<g:link controller="algorithmRequest" action="create" id="${ algorithm.id }" class="ui positive button" style="float: right;">Edit Request</g:link>
			<g:link controller="algorithmRequest" action="run" id="${ algorithm.id }" class="ui primary button" style="float: right;">Kick Off Evaluation</g:link>
			<h2 class="ui header" style="clear: both;">Results</h2>
			<div class="ui one cards">
				<g:each in="${ algorithm.algorithmResults.sort { it.dateCreated }.reverse() }" var="result">
					<g:set var="complete" value="${ !result.machineLearningModel }" />
					<div id="result-${ result.id }" class="ui raised card">
						<div class="content">
							<span class="ui ${ complete ? 'green' : 'yellow' } right corner label"><i class="${ complete ? 'check mark' : 'refresh' } icon"></i></span>
							<div class="header">${ result.dateCreated.format(Global.DATE_FORMAT) }</div>
							<div class="meta">
								<span data-title="Date Created"><i class="plus icon"></i> <abbr class="timeago" title="${ result.dateCreated }"></abbr></span>
								<span data-title="Number of Predicted Values"><i class="cubes icon"></i> ${ result.predictedValues.size() }</span>
							</div>
							<g:if test="${ complete }">
								<g:render template="/layouts/pending" model="${ [id: 'pending-' + result.id] }" />
								<div id="chart-${ result.id }" class="data-chart"></div>
							</g:if>
						</div>
					</div>
				</g:each>
			</div>
		</div>
		<script>
			$(function() {
				$('.timeago').timeago();
				$('span[data-title]').popup({
					position: 'top center'
				});
				$('.data-chart').toArray().forEach(function(me) {
					var id = $(me).attr('id').split('-')[1];
					getData(id, lineGraph, '#chart-' + id, '#pending-' + id, true);
				});
			});
		</script>
	</body>
</html>
