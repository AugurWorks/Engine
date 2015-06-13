function addDataSet() {
	var html = '<tr>';
	html += '<td class="stock">' + $('#stock').val() + '</td>';
	html += '<td class="offset">' + $('#offset').val() + '</td>';
	html += '<td><button onclick="removeRow(this)" class="ui button">Remove</button></td>';
	html += '</tr>';
	$('#dataSets tbody').append(html);
}

function removeRow(me) {
	$(me).parents('tr').remove();
}

function submitRequest() {
	$.ajax({
		url: '/algorithmRequest/submitRequest',
		data: {
			id: $('#id').val(),
			dataSets: JSON.stringify(getDataSets()),
			startDate: $('#startDate').val(),
			endDate: $('#endDate').val()
		},
		success: function(data) {
			if (data.success) {
				window.location.href = '/algorithmRequest/create/' + data.id
			}
		}
	});
}

function getDataSets() {
	return $('#dataSets tbody > tr').toArray().map(function(d) {
		return {
			name: $(d).children('.stock').text(),
			offset: $(d).children('.offset').text()
		}
	});
}