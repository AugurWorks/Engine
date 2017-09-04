<!DOCTYPE html>
    <html>
    <head>
        <title>Products</title>
        <meta name="layout" content="semantic">
    </head>
    <body>
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
                        <g:render template="/product/productRow" model="${ [product: product] }" />
                    </g:each>
                    <tr class="row">
                        <td>
                            <div class="ui fluid input">
                                <g:field name="name" placeholder="Product Name" />
                            </div>
                        </td>
                        <td></td>
                        <td><div class="ui small primary button" onclick="createProduct()">Create</div> </td>
                    </tr>
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

            function createProduct() {
                $.ajax({
                    url: '/product/create',
                    data: {
                        name: $('#name').val()
                    },
                    success: function(data) {
                        $('table > tbody tr').eq(-1).before(data);
                        $('#name').val('');
                    },
                    error: function(error) {
                        swal({
                            title: 'Error',
                            text: data.error,
                            type: 'error',
                            html: true
                        });
                    }
                });
            }
        </script>
    </body>
</html>
