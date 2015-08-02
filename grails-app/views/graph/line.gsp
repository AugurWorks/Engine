<!DOCTYPE html>
<html>
	<head>
		<title>Line Graph</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.0/d3.min.js"></script>
		<script src="//cdnjs.cloudflare.com/ajax/libs/c3/0.4.10/c3.min.js"></script>
		<asset:javascript src="graph.js" />
		<asset:stylesheet href="c3.css" />
	</head>
	<body>
		<div class="ui segment">
			<h1 class="ui header">Line Graph - <g:select name="id" from="${ requests }" value="${ algorithmRequest?.id }" noSelection="${ ['null': 'Select a Request'] }" optionKey="id" optionValue="name"></g:select></h1>
			<g:render template="/layouts/pending" />
			<div id="chart"></div>
		</div>
		<script>
			$(function() {
				$('#id').change(function() {
					getData($('#id').val(), lineGraph, '#chart', '.pending', false);
				});
				$('#id').trigger('change');
			});
		</script>
	</body>
</html>
