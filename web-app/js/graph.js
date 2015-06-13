function getData(id, success) {
	$.ajax({
		url: '/graph/getData/' + id,
		success: success
	});
}