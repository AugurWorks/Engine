<g:set var="threshold" value="${ grailsApplication.config.augurworks.threshold[unit] }" />
<g:set var="predictions" value="${ predictedValues.grep { it.actual != null } }" />
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
<h3 class="ui header">${ title ? title + ': ' : '' }Statistics - Threshold: ${ threshold }</h3>
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