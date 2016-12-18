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
		    <g:set var="threshold" value="${ grailsApplication.config.augurworks.threshold[algorithm.unit.name().toLowerCase()] }" />
            <g:set var="predictions" value="${ request.algorithmResults*.getFutureValues().flatten().grep { it.actual != null } }" />
            <g:set var="cdAll" value="${ predictions.grep { it.actual * it.value >= 0 } }" />
            <g:set var="allPct" value="${ predictions.size() == 0 ? null : Math.round(10000 * cdAll.size() / predictions.size()) / 100 }" />
            <g:set var="overThreshold" value="${ predictions.grep { Math.abs(it.value) >= threshold } }" />
            <g:set var="cdOverThreshold" value="${ overThreshold.grep { it.actual * it.value >= 0 } }" />
            <g:set var="valuePct" value="${ overThreshold.size() == 0 ? null : Math.round(10000 * cdOverThreshold.size() / overThreshold.size()) / 100 }" />
            <g:set var="actualOverThreshold" value="${ predictions.grep { Math.abs(it.actual) >= threshold } }" />
            <g:set var="cdActualOverThreshold" value="${ actualOverThreshold.grep { it.actual * it.value >= 0 } }" />
            <g:set var="actualPct" value="${ actualOverThreshold.size() == 0 ? null : Math.round(10000 * cdActualOverThreshold.size() / actualOverThreshold.size()) / 100 }" />
            <g:set var="bothOverThreshold" value="${ predictions.grep { Math.abs(it.value) >= threshold && Math.abs(it.actual) >= threshold } }" />
            <g:set var="cdBothOverThreshold" value="${ bothOverThreshold.grep { it.actual * it.value >= 0 } }" />
            <g:set var="bothPct" value="${ bothOverThreshold.size() == 0 ? null : Math.round(10000 * cdBothOverThreshold.size() / bothOverThreshold.size()) / 100 }" />
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
			<h3 class="ui header">Statistics - Threshold: ${ threshold }</h3>
            <table class="ui small striped compact celled table">
                <thead>
                    <tr>
                        <th></th>
                        <th>Total</th>
                        <th>Correct Direction</th>
                        <th>Correct Direction %</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>All Predictions</td>
                        <td>${ predictions.size() }</td>
                        <td>${ cdAll.size() }</td>
                        <td>
                            <g:if test="${ allPct != null }">
                                <i class="icon ${ allPct > 85 ? "check green" : allPct > 70 ? "attention yellow" : "remove red" }"></i>
                            </g:if>
                            ${ allPct == null ? 'N/A' : allPct + '%' }
                        </td>
                    </tr>
                    <tr>
                        <td>Prediction Over Threshold</td>
                        <td>${ overThreshold.size() }</td>
                        <td>${ cdOverThreshold.size() }</td>
                        <td>
                            <g:if test="${ valuePct != null }">
                                <i class="icon ${ valuePct > 85 ? "check green" : valuePct > 70 ? "attention yellow" : "remove red" }"></i>
                            </g:if>
                            ${ valuePct == null ? 'N/A' : valuePct + '%' }
                        </td>
                    </tr>
                    <tr>
                        <td>Actual Over Threshold</td>
                        <td>${ actualOverThreshold.size() }</td>
                        <td>${ cdActualOverThreshold.size() }</td>
                        <td>
                            <g:if test="${ actualPct != null }">
                                <i class="icon ${ actualPct > 85 ? "check green" : actualPct > 70 ? "attention yellow" : "remove red" }"></i>
                            </g:if>
                            ${ actualPct == null ? 'N/A' : actualPct + '%' }
                        </td>
                    </tr>
                    <tr>
                        <td>Both Over Threshold</td>
                        <td>${ bothOverThreshold.size() }</td>
                        <td>${ cdBothOverThreshold.size() }</td>
                        <td>
                            <g:if test="${ bothPct != null }">
                                <i class="icon ${ bothPct > 85 ? "check green" : bothPct > 70 ? "attention yellow" : "remove red" }"></i>
                            </g:if>
                            ${ bothPct == null ? 'N/A' : bothPct + '%' }
                        </td>
                    </tr>
                </tbody>
            </table>
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
