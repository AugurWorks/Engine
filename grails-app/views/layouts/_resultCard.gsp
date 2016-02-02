<%@ page import="com.augurworks.engine.helper.Global" %>
<g:set var="complete" value="${ result.complete }" />
<g:set var="dateFormat" value="${ result.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT }" />
<div id="result-${ result.id }" class="ui raised card ${ !complete ? 'incomplete' : '' }">
	<div class="content">
		<span class="ui ${ complete ? 'green' : 'yellow' } right corner label"><i class="${ complete ? 'check mark' : 'refresh' } icon"></i></span>
		<div class="header">${ (title ? result.algorithmRequest.toString() + ' - ' : '') + result.dateCreated.format(dateFormat) }</div>
		<div class="meta">
			<span data-title="Model Type"><i class="terminal icon"></i> ${ result.modelType }</span>
			<span data-title="Start Date"><i class="green calendar outline icon"></i>${ result.startDate.format(dateFormat) }</span>
			<span data-title="End Date"><i class="red calendar outline icon"></i>${ result.endDate.format(dateFormat) }</span>
			<span data-title="Date Created"><i class="plus icon"></i> <abbr class="timeago" title="${ result.dateCreated }"></abbr></span>
			<span data-title="Number of Predicted Values"><i class="cubes icon"></i> ${ result.predictedValues.size() }</span>
		</div>
		<g:if test="${ complete }">
			<g:render template="/layouts/pending" model="${ [id: 'pending-' + result.id] }" />
			<div id="chart-${ result.id }" class="data-chart"></div>
		</g:if>
	</div>
</div>