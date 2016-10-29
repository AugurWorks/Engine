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
			<h1 class="ui floated left header">Algorithm Requests</h1>
			<g:link controller="algorithmRequest" action="create" class="ui primary button" style="float: right;">New Request</g:link>
			<div class="ui input" style="float: right; margin-right: 1em;">
				<input id="filter" type="text" placeholder="Filter">
			</div>
			<div class="ui two doubling cards" style="clear: both;">
				<g:each in="${ requests }" var="request">
					<g:link controller="algorithmRequest" action="show" id="${ request.id }" class="card" name="${ request.toString() }">
						<div class="content">
							<div class="header">${ request.toString() }</div>
							<div class="meta">
								<span data-title="Date Created"><i class="plus icon"></i> <abbr class="timeago" title="${ request.dateCreated }"></abbr></span>
								<span data-title="Request Start Date"><i class="green calendar icon"></i> ${ request.startDate.format(Global.DATE_FORMAT) }</span>
								<span data-title="Request End Date"><i class="red calendar icon"></i> ${ request.endDate.format(Global.DATE_FORMAT) }</span>
							</div>
							<div class="meta">
								<span data-title="Result Set Count"><i class="cubes icon"></i> ${ request.algorithmResults.size() }</span>
								<span data-title="Time Period"><i class="wait icon"></i> ${ request.unit.name }</span>
								<span data-title="Cron Expression"><i class="repeat icon"></i> ${ request.cronExpression ?: 'None' }</span>
							</div>
						</div>
					</g:link>
				</g:each>
			</div>
		</div>
		<script>
			$(function() {
				$('#filter').focus();
				$('.timeago').timeago();
				$('span[data-title]').popup({
					position: 'top center'
				});
				$('#filter').keyup(function() {
					var val = $('#filter').val().toLowerCase();
					$('.card').show();
					$('.card').toArray().forEach(function(c) {
						if ($(c).attr('name').toLowerCase().indexOf(val) == -1) {
							$(c).hide();
						}
					});
				});
			});
		</script>
	</body>
</html>
