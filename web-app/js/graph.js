function getData(id, success) {
	$.ajax({
		url: '/graph/getData/' + id,
		success: function() {
			$('.pending').remove();
			success.apply(this, arguments);
		}
	});
}

function lineGraph(result) {
	var data = result.data;
	var dates = mergeDates(data);
	var columns = data.map(function(dataSet) {
		return createDataColumn(dataSet, dates);
	});
	columns.unshift(['x'].concat(dates));
	var chart = c3.generate({
		data: {
			x: 'x',
			columns: columns
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

function createDataColumn(dataSetObject, dates) {
	var dateMap = dataSetObject.values.reduce(function(map, cur) {
		map[cur[0]] = cur[1];
		return map;
	}, {});
	var values = dates.map(function(d) {
		var val = dateMap[d];
		return val ? val : 0;
	});
	values.unshift(dataSetObject.name);
	return values;
}

function mergeDates(allData) {
	return allData.reduce(function(allDates, dataSet) {
		var mergedDates = dataSet.values.map(function(row) {
			return row[0];
		}).concat(allDates);
		return $.unique(mergedDates);
	}, []);
}