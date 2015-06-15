<!DOCTYPE html>
<html>
	<head>
		<title>Line Graph</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
		<script src="//cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js"></script>
		<script src="${resource(dir: 'static/js', file: 'graph.js')}"></script>
		<link rel="stylesheet" href="${resource(dir: 'static/css', file: 'c3.css')}" type="text/css">
	</head>
	<body>
		<div class="ui segment">
			<h1 class="ui header">Line Graph - ${ algorithmRequest.toString() }</h1>
			<h1 class="ui center aligned icon header pending">
				<i class="loading notched circle icon"></i>
				<div class="content">
					Loading Data...
				</div>
			</h1>
			<div id="chart"></div>
		</div>
		<script>
			$(function() {
				getData(${ algorithmRequest.id }, lineGraph);
			});
		</script>
	</body>
</html>
