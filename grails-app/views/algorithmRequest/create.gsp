<!DOCTYPE html>
<html>
	<head>
		<title>Create Algorithm Request</title>
		<meta name="layout" content="semantic">
		<script src="https://cdnjs.cloudflare.com/ajax/libs/sweetalert/1.0.1/sweetalert.min.js"></script>
		<asset:javascript src="algorithmRequest.js" />
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/sweetalert/1.0.1/sweetalert.min.css" type="text/css">
	</head>
	<body>
		<div class="ui segment">
			<g:if test="${ algorithmRequest }">
				<h1 class="ui header">Edit: ${ algorithmRequest.toString() }</h1>
			</g:if>
			<g:else>
				<h1 class="ui header">Create Algorithm Request</h1>
			</g:else>
			<div class="ui form">
				<h3 class="ui dividing header">Boundary Dates</h3>
				<g:field type="hidden" name="id" value="${ algorithmRequest?.id }" />
				<div class="two fields">
					<div class="field">
						<label>Start Date</label>
						<g:field type="date" name="startDate" value="${ use (groovy.time.TimeCategory) { (algorithmRequest?.startDate ?: new Date() - 2.months).format('yyyy-MM-dd') } }" />
					</div>
					<div class="field">
						<label>End Date</label>
						<g:field type="date" name="endDate" value="${ (algorithmRequest?.endDate ?: new Date()).format('yyyy-MM-dd') }" />
					</div>
				</div>
				<h3 class="ui dividing header">Add Data Set</h3>
				<div class="three fields">
					<div class="field">
						<label>Stock</label>
						<g:select from="${ dataSets }" name="stock" class="ui search dropdown" />
					</div>
					<div class="field">
						<label>Day Offset</label>
						<g:field type="number" name="offset" value="0" />
					</div>
					<div class="field">
						<label>Add Data Set</label>
						<button onclick="addDataSet()" class="ui primary button">Add Data Set</button>
					</div>
				</div>
				<div class="fields">
					<div class="field">
						<label>Submit</label>
						<button onclick="submitRequest()" class="ui positive button">${ algorithmRequest ? 'Update' : 'Create' } Request</button>
					</div>
					<g:if test="${ algorithmRequest }">
						<div class="field">
							<label>Delete</label>
							<button onclick="deleteRequest()" class="ui negative button">Delete Request</button>
						</div>
					</g:if>
				</div>
				<h3 class="ui dividing header">Data Sets</h3>
				<table id="dataSets" class="ui table">
					<thead>
						<tr>
							<th>Dependant</th>
							<th>Stock</th>
							<th>Day Offset</th>
							<th>Remove</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${ algorithmRequest?.requestDataSets }" var="requestDataSet">
							<tr>
								<td class="ui radio checkbox"><g:field type="radio" name="dependant" checked="${ algorithmRequest.dependantDataSet == requestDataSet.dataSet }" value="${ requestDataSet.dataSet.id }" /></td>
								<td class="stock">${ requestDataSet.dataSet.toString() }</td>
								<td class="offset">${ requestDataSet.offset }</td>
								<td><button onclick="removeRow(this)" class="ui button">Remove</button></td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
		<script>
			var dataSets = [];
			$(function() {
				$('.ui.radio.checkbox').checkbox();
			});
		</script>
	</body>
</html>
