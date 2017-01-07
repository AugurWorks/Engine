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
			<div class="ui divider" style="clear: both;"></div>
            <h4 class="ui floated left header">
                <span id="request-count">${ requests.size() }</span> Request(s)
            </h3>
            <div class="ui toggle checkbox" style="float: right;">
                <input type="checkbox" id="hideNoResults">
                <label>Hide requests with no results</label>
            </div>
            <div class="ui divider" style="clear: both;"></div>
            <table class="ui celled table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th><i class="plus icon"></i> Created</th>
                        <th><i class="green calendar icon"></i> Start Date</th>
                        <th><i class="red calendar icon"></i> End Date</th>
                        <th><i class="wait icon"></i> Period</th>
                        <th><i class="repeat icon"></i> Cron</th>
                        <th><i class="tag icon"></i> Tags</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${ requests }" var="request">
                        <tr class="row results-${ request.algorithmResults.size() }" name="${ request.toString() }">
                            <td><g:link controller="algorithmRequest" action="show" id="${ request.id }">${ request.toString() }</g:link></td>
                            <td><abbr class="timeago" title="${ request.dateCreated }"></abbr></td>
                            <td>${ request.startDate.format(Global.DATE_FORMAT) }</td>
                            <td>${ request.endDate.format(Global.DATE_FORMAT) }</td>
                            <td>${ request.unit.name }</td>
                            <td>${ request.cronExpression ?: 'None' }</td>
                            <td>${ request.tags*.name.sort().join(', ') }</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
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
					$('.row').show();
					$('.row').toArray().forEach(function(c) {
						if ($(c).attr('name').toLowerCase().indexOf(val) == -1) {
							$(c).hide();
						}
					});
				});
				$('.ui.toggle').checkbox({
				    onChecked: function() {
				        $('.results-0').hide();
				        $('#request-count').text($('.row:visible').length);
				    },
				    onUnchecked: function() {
				        $('.results-0').show();
				        $('#request-count').text($('.row:visible').length);
				    }
				});
			});
		</script>
	</body>
</html>
