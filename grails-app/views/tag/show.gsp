<!DOCTYPE html>
<html>
    <head>
        <title>Tag Statistics - ${ tag }</title>
        <meta name="layout" content="semantic">
    </head>
    <body>
        <div class="ui segment">
            <h1 class="ui header">Tag Statistics - ${ tag }</h1>
            <g:if test="${ requests.size() != 0}">
                <g:set var="units" value="${ requests*.unit*.name()*.toLowerCase().unique() }" />
                <g:render template="/layouts/statistics" model="${ [title: 'All Requests', unit: units.first(), predictedValues: requests*.algorithmResults.flatten()*.getFutureValues().flatten()] }" />
            </g:if>

            <g:each in="${ requests }" var="request">
                <g:render template="/layouts/statistics" model="${ [title: request.name, unit: request.unit.name().toLowerCase(), predictedValues: request.algorithmResults*.getFutureValues().flatten()] }" />
            </g:each>
        </div>
    </body>
</html>
