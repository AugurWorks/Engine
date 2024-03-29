<!DOCTYPE html>
<html>
	<head>
		<title>Requests</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.4.1/jquery.timeago.min.js"></script>
		<asset:javascript src="tablesort.min.js"/>
		<asset:javascript src="algorithmRequest.js" />
		<style>
		    body > .content {
		        max-width: 1200px;
		    }
		</style>
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
                <label>Hide requests with no cron expression</label>
            </div>
            <div class="ui divider" style="clear: both;"></div>
            <table class="ui small compact sortable celled table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th class="collapsing"><i class="green calendar icon"></i> Start</th>
                        <th class="collapsing"><i class="red calendar icon"></i> End</th>
                        <th class="collapsing"><i class="wait icon"></i> Period</th>
                        <th><i class="repeat icon"></i> Cron</th>
                        <th><i class="tag icon"></i> Tags</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${ requests }" var="request">
                        <tr id="request-${ request.id }" class="row results-${ request.algorithmResults.size() }" name="${ request.toString() }">
                            <td><g:link controller="algorithmRequest" action="show" id="${ request.id }">${ request.toString() }</g:link> <i class="green check success circle icon" style="display: none;"></i></td>
                            <td>${ request.startOffset }</td>
                            <td>${ request.endOffset }</td>
                            <td>${ request.unit.name }</td>
                            <td>
                                <div class="ui small input" style="width: 100%;">
                                    <g:field type="text" name="cronExpression" class="cronExpression" value="${ request.cronExpression }" placeholder="0 0 3 ? * *" />
                                </div>
                            </td>
                            <td>
                                <div class="ui small input" style="width: 100%;">
                                    <g:field type="text" name="tags" class="tags" value="${ request.tags*.name?.join(', ') }" />
                                </div>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
		</div>
		<script>
			$(function() {
                $('table').tablesort();
				$('#filter').focus();
				$('span[data-title]').popup({
					position: 'top center'
				});
				$('#filter').keyup(function() {
					var vals = $('#filter').val().toLowerCase().split(' ');
					$('.row').show();
					$('.row').toArray().forEach(function(c) {
						if (!matchTagFilters(vals, $(c).attr('name').toLowerCase())) {
							$(c).hide();
						}
					});
				});
				$('.ui.toggle').checkbox({
				    onChecked: function() {
				        $('.cronExpression').filter(function() { return $(this).val() == ""; }).parents('tr').hide();
				        $('#request-count').text($('.row:visible').length);
				    },
				    onUnchecked: function() {
				        $('tr').show();
				        $('#request-count').text($('.row:visible').length);
				    }
				});
                $('.cronExpression, .tags').change(function(e) {
                    var id = '#' + $(e.target).parents('tr').attr('id');
                    saveRequestReduced(id);
                    $(id + ' .success').show();
                    setTimeout(function() {
                        $(id + ' .success').fadeOut();
                    }, 1000);
                });
			});

			function matchTagFilters(tags, name) {
			    return tags.reduce((matches, tag) => {
			        return matches && name.indexOf(tag) != -1;
			    }, true);
			}
		</script>
	</body>
</html>
