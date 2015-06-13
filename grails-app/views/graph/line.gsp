<!DOCTYPE html>
<html>
	<head>
		<title>Line Graph</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
		<script src="//cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js"></script>
		<script src="${resource(dir: 'static/js', file: 'graph.js')}"></script>
	</head>
	<body>
		<div class="ui segment">
			<h1 class="ui header">Line Graph - ${ algorithmRequest.toString() }</h1>
			<div id="chart">
				<div class="loader" style="text-align: center;">
					<i style="margin: 0 auto;" class="huge loading notched circle icon"></i>
				</div>
			</div>
		</div>
		<script>
			$(function() {
				getData(${ algorithmRequest.id }, function(data) {
					console.log(data)
				})
			});
		</script>
	</body>
</html>
