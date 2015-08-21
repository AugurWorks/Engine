function addDataSet() {
	var html = '<tr>';
	html += '<td><div class="ui radio checkbox"><input type="radio" name="dependant"' + ($('#dataSets tbody tr').length ? '' : 'checked') + ' /></div></td>'
	html += '<td class="stock">' + $('#stock').val() + '</td>';
	html += '<td class="offset">' + $('#offset').val() + '</td>';
	html += '<td class="aggregation">' + $('#aggregation').val() + '</td>';
	html += '<td><button onclick="removeRow(this)" class="ui button">Remove</button></td>';
	html += '</tr>';
	$('#dataSets tbody').append(html);
	$('.ui.checkbox').checkbox();
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
			if (data.ok) {
				window.location.href = '/algorithmRequest/show/' + data.id
			} else {
				swal('Error', data.error, 'error');
			}
		}
	});
}

function deleteRequest() {
	$.ajax({
		url: '/algorithmRequest/deleteRequest',
		data: {
			id: $('#id').val()
		},
		success: function(data) {
			if (data.ok) {
				window.location.href = '/algorithmRequest/'
			} else {
				swal('Error', 'There was an error deleting the algorithm. The error has been logged.', 'error');
			}
		}
	});
}

function getDataSets() {
	return $('#dataSets tbody > tr').toArray().map(function(d) {
		return {
			name: $(d).children('.stock').text(),
			offset: $(d).children('.offset').text(),
			aggregation: $(d).children('.aggregation').text(),
			dependant: $(d).find('input[name=dependant]').is(':checked')
		}
	});
}