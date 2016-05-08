function addDataSet() {
	var stockInfo = $('#stock').val().split('-');
	addDataSetRow($('#stock option:selected').text(), stockInfo[0], stockInfo[1], $('#offset').val(), $('#aggregation').val());
}

function addManualDataSet() {
	var ticker = $('#stock-manual').val();
	addDataSetRow(ticker, ticker, $('#datasource').val(), $('#offset-manual').val(), $('#aggregation-manual').val());
}

function addDataSetRow(name, ticker, datasource, offset, aggregation) {
	var html = '<tr>';
	html += '<input type="hidden" class="name" value="' + name + '" />';
	html += '<input type="hidden" class="datasource" value="' + datasource + '" />';
	html += '<td><div class="ui radio checkbox"><input type="radio" name="dependant"' + ($('#dataSets tbody tr').length ? '' : 'checked') + ' /></div></td>'
	html += '<td class="stock">' + ticker + '</td>';
	html += '<td class="offset">' + offset + '</td>';
	html += '<td class="aggregation">' + aggregation + '</td>';
	html += '<td><button onclick="removeRow(this)" class="ui button">Remove</button></td>';
	html += '</tr>';
	$('#dataSets tbody').append(html);
	$('.ui.checkbox').checkbox();
}

function removeRow(me) {
	$(me).parents('tr').remove();
}

function submitRequest(overwrite) {
	$.ajax({
		url: '/algorithmRequest/submitRequest',
		data: {
			id: $('#id').val(),
			name: $('#name').val(),
			dataSets: JSON.stringify(getDataSets()),
			startOffset: $('#startOffset').val(),
			endOffset: $('#endOffset').val(),
			unit: $('#unit').val(),
			cronExpression: $('#cronExpression').val(),
			cronAlgorithms: JSON.stringify($('#cronAlgorithms').val() || []),
			overwrite: overwrite
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

function checkRequest() {
	$('.ui.message').hide();
	$('#checking').show();
	$.ajax({
		url: '/algorithmRequest/checkRequest',
		data: {
			dataSets: JSON.stringify(getDataSets()),
			startOffset: $('#startOffset').val(),
			endOffset: $('#endOffset').val(),
			unit: $('#unit').val(),
			cronExpression: $('#cronExpression').val()
		},
		success: function(data) {
			$('#checking').hide();
			if (data.ok) {
				$('#valid').show();
			} else {
				$('#invalid').show();
				$('#invalid p').text(data.error);
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
			symbol: $(d).children('.stock').text(),
			name: $(d).children('.name').val(),
			datasource: $(d).children('.datasource').val(),
			offset: $(d).children('.offset').text(),
			aggregation: $(d).children('.aggregation').text(),
			dependant: $(d).find('input[name=dependant]').is(':checked')
		}
	});
}