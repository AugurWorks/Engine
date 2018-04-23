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
                        <th>Diff Upper</th>
                        <th>Diff Lower</th>
                        <th>RT Positive %</th>
                        <th>RT Negative %</th>
                        <th>Close Positive %</th>
                        <th>Close Negative %</th>
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
                                <g:field name="diffUpperThreshold" placeholder="Diff Upper" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="diffLowerThreshold" placeholder="Diff Lower" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="isRealTimePositiveThresholdPercent" placeholder="RT Positive %" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="isRealTimeNegativeThresholdPercent" placeholder="RT Negative %" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="isClosePositiveThresholdPercent" placeholder="Close Positive %" />
                            </div>
                        </td>
                        <td>
                            <div class="ui fluid input">
                                <g:field name="isCloseNegativeThresholdPercent" placeholder="Close Negative %" />
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
                        isRealTimePositiveThresholdPercent: $('#isRealTimePositiveThresholdPercent').val(),
                        isRealTimeNegativeThresholdPercent: $('#isRealTimeNegativeThresholdPercent').val(),
                        isClosePositiveThresholdPercent: $('#isClosePositiveThresholdPercent').val(),
                        isCloseNegativeThresholdPercent: $('#isCloseNegativeThresholdPercent').val(),
                    },
                    success: function(data) {
                        $('table > tbody tr').eq(-1).before(data);
                        $('#name').val('');
                        $('#volatilePercentLimit').val('');
                        $('#diffUpperThreshold').val('');
                        $('#diffLowerThreshold').val('');
                        $('#isRealTimePositiveThresholdPercent').val('');
                        $('#isRealTimeNegativeThresholdPercent').val('');
                        $('#isClosePositiveThresholdPercent').val('');
                        $('#isCloseNegativeThresholdPercent').val('');
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
