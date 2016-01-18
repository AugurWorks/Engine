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
				swal('Error', data.error, 'error');
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