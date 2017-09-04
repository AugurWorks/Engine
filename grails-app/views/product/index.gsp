<!DOCTYPE html>
    <html>
    <head>
        <title>Products</title>
        <meta name="layout" content="semantic">
    </head>
    <body>
        <%@ page import="com.augurworks.engine.domains.AlgorithmRequest" %>
        <div class="ui segment">
            <h1 class="ui header">Products</h1>
            <table class="ui small compact sortable celled table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Requests</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${ products }" var="product">
                        <tr class="row">
                            <td>${ product.name }</td>
                            <td>
                                <g:each in="${ AlgorithmRequest.findAllByProduct(product).sort { it.name } }" var="request">
                                    <g:link controller="algorithmRequest" action="show" id="${ request.id }">${ request.name }</g:link><br />
                                </g:each>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </body>
</html>
