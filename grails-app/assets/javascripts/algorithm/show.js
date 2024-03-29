function initCharts() {
	init();
	$('.result:not(.initialized) .data-chart').toArray().forEach(function(me) {
		$(me).parents('.result').addClass('initialized');
		var id = $(me).attr('id').split('-')[1];
		getData(id, lineGraph, '#chart-' + id, '#pending-' + id, true);
	});
}

function init() {
	$('.timeago').timeago();
	$('span[data-title]').popup({
		position: 'top center'
	});
}

function kickOff(me, id) {
	$(me).addClass('loading');
	$.ajax({
		url: '/algorithmRequest/run/' + id,
		data: {
			type: $('#modelType').val()
		},
		success: function(data) {
			if (data.ok) {
				window.location.reload();
			} else {
				$(me).removeClass('loading');
				swal({
					title: 'Error',
					text: data.error,
					type: 'error',
					html: true
				});
			}
		}
	});
}

function refreshAllResultCards() {
	$('.incomplete').toArray().forEach(function(me) {
		var id = $(me).attr('id').split('-')[1];
		refreshResultCard(id);
		getData(id, lineGraph, '#chart-' + id, '#pending-' + id, true);
	});
}

function refreshResultCard(id) {
	$.ajax({
		url: '/algorithmRequest/resultCard/' + id,
		success: function(html) {
			$('#result-' + id).replaceWith(html);
			init();
		}
	});
}

function deleteResult(resultId) {
	$.ajax({
		url: '/algorithmRequest/deleteResult/' + resultId,
		success: function(data) {
			if (data.ok) {
				$('#result-' + resultId).remove();
				$('.ui.popup').remove();
			} else {
				swal('Error', 'That result could not be deleted, please try again later', 'error');
			}
		}
	});
}

function getMore(algorithmResultId, page) {
	$('#more').addClass('loading');
	$.ajax({
		url: '/algorithmRequest/showMore/' + algorithmResultId + '?page=' + page,
		success: function(html) {
			$('#results').append(html);
			initCharts();
			$('#more').removeClass('loading');
			if ($('.result').length == total) {
				$('#more').remove();
			}
		}
	});
}