function kickOff(me, id) {
	$(me).addClass('loading');
	$.ajax({
		url: '/algorithmRequest/run/' + id,
		success: function(data) {
			if (data.ok) {
				window.location.reload();
			}
		}
	});
}