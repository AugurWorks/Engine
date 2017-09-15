<!DOCTYPE html>
    <html>
    <head>
        <title>API Keys</title>
        <meta name="layout" content="semantic">
    </head>
    <body>
        <div class="ui segment">
            <h1 class="ui header">API Keys</h1>
            <table class="ui small compact sortable celled table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th class="collapsing">Key</th>
                        <th class="collapsing">Last Used</th>
                        <th>Products</th>
                        <th class="collapsing">Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${ keys }" var="key">
                        <g:render template="/key/keyRow" model="${ [key: key, products: products] }" />
                    </g:each>
                </tbody>
            </table>
        </div>
        <script>
            $(function() {
                $('.dropdown').dropdown();
            });

            function deleteKey(id) {
                swal({
                    title: 'Are you sure?',
                    type: 'warning',
                    showCancelButton: true,
                    confirmButtonText: 'Yes',
                    closeOnConfirm: true
                },
                function(){
                    $.ajax({
                        url: '/key/delete/' + id,
                        success: function(data) {
                            if (data.ok) {
                                $('#key-' + id).remove();
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
