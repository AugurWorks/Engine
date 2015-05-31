<!DOCTYPE html>
<html>
	<head>
		<title>Requests</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.4.1/jquery.timeago.min.js"></script>
	</head>
	<body>
		<div class="ui segment">
			<h1 class="ui header">Algorithm Requests</h1>
			<div class="ui four doubling cards">
				<g:each in="${ requests }" var="request">
					<g:link controller="algorithmRequest" action="show" id="${ request.id }" class="card">
						<div class="content">
							<div class="header">Request ${ request.id }</div>
							<div class="meta">
								<span data-title="Date Created"><i class="plus icon"></i> <abbr class="timeago" title="${ request.dateCreated }"></abbr></span>
								<span data-title="Request Start Date"><i class="green calendar icon"></i> ${ request.startDate.format('MM/dd/yy') }</span>
								<span data-title="Request End Date"><i class="red calendar icon"></i> ${ request.endDate.format('MM/dd/yy') }</span>
							</div>
						</div>
						<div class="extra content">
							<div class="header">Data Sets</div>
							<ul>
								<g:each in="${ request.requestDataSets }" var="dataSet">
									<li>${ dataSet.toString() }</li>
								</g:each>
							</ul>
						</div>
					</g:link>
				</g:each>
			</div>
		</div>
		<script>
			$(function() {
				$('.timeago').timeago();
				$('span[data-title]').popup({
					position: 'top center'
				});
			});
		</script>
	</body>
</html>
