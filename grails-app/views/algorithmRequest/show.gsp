<!DOCTYPE html>
<html>
	<head>
		<title>Requests</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.4.1/jquery.timeago.min.js"></script>
	</head>
	<body>
		<%@ page import="com.augurworks.engine.helper.Global" %>
		<div class="ui segment">
			<h1 class="ui floated left header">${ algorithm.name }</h1>
			<g:link controller="algorithmRequest" action="create" id="${ algorithm.id }" class="ui positive button" style="float: right;">Edit Request</g:link>
			<g:link controller="algorithmRequest" action="run" id="${ algorithm.id }" class="ui primary button" style="float: right;">Kick Off Evaluation</g:link>
			<h2 class="ui header" style="clear: both;">Results</h2>
			<div class="ui two cards">
				<g:each in="${ algorithm.algorithmResults.sort { it.dateCreated }.reverse() }" var="result">
					<g:set var="complete" value="${ !result.machineLearningModel }" />
					<div class="ui raised card">
						<div class="content">
							<span class="ui ${ complete ? 'green' : 'yellow' } right corner label"><i class="${ complete ? 'check mark' : 'refresh' } icon"></i></span>
							<div class="header">${ result.dateCreated.format(Global.DATE_FORMAT) }</div>
							<div class="meta">
								<span data-title="Date Created"><i class="plus icon"></i> <abbr class="timeago" title="${ result.dateCreated }"></abbr></span>
								<span data-title="Number of Predicted Values"><i class="cubes icon"></i> ${ result.predictedValues.size() }</span>
							</div>
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
			});
		</script>
	</body>
</html>
