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
                        <th class="collapsing">Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${ products }" var="product">
                        <tr id="product-${ product.id }" class="row">
                            <td>${ product.name }</td>
                            <td>
                                <g:each in="${ AlgorithmRequest.findAllByProduct(product).sort { it.name } }" var="request">
                                    <g:link controller="algorithmRequest" action="show" id="${ request.id }">${ request.name }</g:link><br />
                                </g:each>
                            </td>
                            <td style="text-align: center;"><i class="large red trash link icon" onclick="deleteProduct(${ product.id })"></i></td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
        <script>
            function deleteProduct(id) {
                swal({
                    title: 'Are you sure?',
                    type: 'warning',
                    showCancelButton: true,
                    confirmButtonText: 'Yes',
                    closeOnConfirm: true
                },
                function(){
                    $.ajax({
                        url: '/product/delete/' + id,
                        success: function(data) {
                            if (data.ok) {
                                $('#product-' + id).remove();
                            } else {
                                swal({
                                    title: 'Error',
                                    text: data.error,
                                    type: 'error',
                                    html: true
                                });
                            }
                        }
                    });
                });
            }
        </script>
    </body>
</html>
