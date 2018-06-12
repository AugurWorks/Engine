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
                        <th>Volatile Percent Limit</th>
                        <th>Diff Max</th>
                        <th>Diff Min</th>
                        <th>RT Diff Threshold</th>
                        <th>RT Change Threshold</th>
                        <th>Close Diff Threshold</th>
                        <th>Close Change Threshold</th>
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
                        <td>
                            <div class="ui fluid input">
                                <g:field name="volatilePercentLimit" placeholder="Volatile Percent Limit" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="diffUpperThreshold" placeholder="Diff Max" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="diffLowerThreshold" placeholder="Diff Min" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="realTimeDiffThreshold" placeholder="RT Diff Threshold" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="realTimeChangeThreshold" placeholder="RT Change Threshold" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="closeDiffThreshold" placeholder="Close Diff Threshold" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="closeChangeThreshold" placeholder="Close Change Threshold" />
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
                        name: $('#name').val(),
                        volatilePercentLimit: $('#volatilePercentLimit').val(),
                        diffUpperThreshold: $('#diffUpperThreshold').val(),
                        diffLowerThreshold: $('#diffLowerThreshold').val(),
                        realTimeDiffThreshold: $('#realTimeDiffThreshold').val(),
                        realTimeChangeThreshold: $('#realTimeChangeThreshold').val(),
                        closeDiffThreshold: $('#closeDiffThreshold').val(),
                        closeChangeThreshold: $('#closeChangeThreshold').val(),
                    },
                    success: function(data) {
                        $('table > tbody tr').eq(-1).before(data);
                        $('#name').val('');
                        $('#volatilePercentLimit').val('');
                        $('#diffUpperThreshold').val('');
                        $('#diffLowerThreshold').val('');
                        $('#realTimeDiffThreshold').val('');
                        $('#realTimeChangeThreshold').val('');
                        $('#closeDiffThreshold').val('');
                        $('#closeChangeThreshold').val('');
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
