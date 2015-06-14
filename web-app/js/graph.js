function getData(id, success) {
	$.ajax({
		url: '/graph/getData/' + id,
		success: function() {
			$('.loader').remove();
			success.apply(this, arguments);
		}
	});
}

function lineGraph(result) {
	var data = result.data;
	var chart = c3.generate({
		data: {
			x: data[0].name + '-x',
			columns: [
				createDataColumn(data[0], true),
				createDataColumn(data[0], false)
			]
		},
		axis: {
			x: {
				type: 'timeseries',
				tick: {
					format: '%Y-%m-%d'
				}
			}
		}
	});
}

function createDataColumn(dataSetObject, isXAxis) {
	var values = dataSetObject.values.map(function(d) {
		return d[isXAxis ? 0 : 1];
	});
	values.unshift(dataSetObject.name + (isXAxis ? '-x' : ''));
	return values;
}