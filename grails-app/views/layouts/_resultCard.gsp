<%@ page import="com.augurworks.engine.helper.Global" %>
<%@ page import="com.augurworks.engine.helper.AlgorithmType" %>
<g:set var="complete" value="${ result.complete }" />
<g:set var="dateFormat" value="${ result.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT }" />
<div id="result-${ result.id }" class="ui raised card ${ !complete ? 'incomplete' : '' }">
	<div class="content">
		<span class="ui ${ complete ? 'green' : 'yellow' } right corner label"><i class="${ complete ? 'check mark' : 'refresh' } icon"></i></span>
		<div class="header">${ (title ? result.algorithmRequest.toString() + ' - ' : '') + result.dateCreated.format(dateFormat) }</div>
		<div class="meta">
			<g:if test="${ requestLink }">
				<span data-title="Parent Request"><i class="bookmark icon"></i> <g:link controller="algorithmRequest" action="show" id="${ result.algorithmRequest.id }">Request</g:link></span>
			</g:if>
			<span data-title="Model Type"><i class="terminal icon"></i> ${ result.modelType.name }</span>
			<g:if test="${ result.modelType == AlgorithmType.ALFRED && complete }">
				<span><i class="file text outline icon"></i> <a href="${ grailsApplication.config.alfred.url + '/logs/' + result.alfredModelId }" target="_blank">Logs</a></span>
				<span><i class="history icon"></i> <a href="${ grailsApplication.config.alfred.url + '/result/' + result.alfredModelId }" target="_blank">Stats</a></span>
			</g:if>
			<span data-title="Start Date"><i class="green calendar outline icon"></i>${ result.startDate.format(dateFormat) }</span>
			<span data-title="End Date"><i class="red calendar outline icon"></i>${ result.endDate.format(dateFormat) }</span>
			<span data-title="Date Created"><i class="wait icon"></i> <abbr class="timeago" title="${ result.dateCreated }"></abbr></span>
			<span data-title="Number of Predicted Values"><i class="cubes icon"></i> ${ result.predictedValues.size() }</span>
			<span data-title="Delete Result" style="cursor: pointer;" onclick="deleteResult(${ result.id })"><i class="red trash icon"></i></span>
		</div>
		<g:if test="${ complete }">
			<g:render template="/layouts/pending" model="${ [id: 'pending-' + result.id] }" />
			<div id="chart-${ result.id }" class="data-chart"></div>
		</g:if>
	</div>
</div>