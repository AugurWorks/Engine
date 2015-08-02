function getData(id, success, selector, pending, prediction) {
	var uri = prediction ? '/graph/getResultData/' : '/graph/getData/'
	$(pending).show();
	$(selector).html('');
	$.ajax({
		url: uri + id,
		success: function(result) {
			$(pending).hide();
			success(result, selector);
		}
	});
}

function lineGraph(result, selector) {
	var data = result.data;
	var dates = mergeDates(data);
	var columns = data.map(function(dataSet) {
		return createDataColumn(dataSet, dates);
	});
	columns.unshift(['x'].concat(dates));
	var chart = c3.generate({
		bindto: selector,
		data: {
			x: 'x',
			columns: columns
		},
		axis: {
			x: {
				type: 'timeseries',
				tick: {
					format: '%Y-%m-%d',
					count: 20
				}
			}
		},
		point: {
			show: false
		}
	});
}

function createDataColumn(dataSetObject, dates) {
	var dateMap = dataSetObject.values.reduce(function(map, cur) {
		map[cur.date] = cur.value;
		return map;
	}, {});
	var values = dates.map(function(d) {
		var val = dateMap[d];
		return val ? val : null;
	});
	values.unshift(dataSetObject.name);
	return values;
}

function mergeDates(allData) {
	return allData.reduce(function(allDates, dataSet) {
		var mergedDates = dataSet.values.map(function(row) {
			return row.date;
		}).concat(allDates);
		return $.unique(mergedDates);
	}, []);
}