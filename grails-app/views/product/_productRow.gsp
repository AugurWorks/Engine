<%@ page import="com.augurworks.engine.domains.AlgorithmRequest" %>
<tr id="product-${ product.id }" class="row">
    <td>${ product.name }</td>
    <td>${ product.getSnsTopicArn() }</td>
    <td>
        <g:each in="${ AlgorithmRequest.findAllByProduct(product).sort { it.name } }" var="request">
            <g:link controller="algorithmRequest" action="show" id="${ request.id }">${ request.name }</g:link><br />
        </g:each>
    </td>
    <td style="text-align: center;"><i class="large red trash link icon" onclick="deleteProduct(${ product.id })"></i></td>
</tr>