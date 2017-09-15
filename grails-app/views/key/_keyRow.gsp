<tr id="key-${ key.id }" class="row">
    <td>${ key.name }</td>
    <td style="white-space: nowrap;">${ key.key }</td>
    <td style="white-space: nowrap;">${ key.lastUsed ? key.lastUsed.format('HH:mma MM/dd/yyyy') : '' }</td>
    <td>
        <g:select class="ui dropdown" name="products" from="${ products }" optionKey="id" optionValue="name" multiple="true" value="${ key.products }" />
        <i id="success-${ key.id }" class="large green check success circle icon" style="float: right; display: none;"></i>
        <i id="failure-${ key.id }" class="large red remove icon" style="float: right; display: none;"></i>
    </td>
    <td style="text-align: center;"><i class="large red trash link icon" onclick="deleteKey(${ key.id })"></i></td>
</tr>