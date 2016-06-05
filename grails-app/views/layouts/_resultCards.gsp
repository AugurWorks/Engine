<g:each in="${ results }" var="result">
	<g:render template="/layouts/resultCard" model="${ [result: result] }" />
</g:each>