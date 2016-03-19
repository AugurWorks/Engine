<!DOCTYPE html>
<html>
	<head>
		<title>Create Algorithm Request</title>
		<meta name="layout" content="semantic">
		<asset:javascript src="algorithmRequest.js" />
	</head>
	<body>
		<%@ page import="com.augurworks.engine.helper.Aggregation" %>
		<div class="ui segment">
			<g:if test="${ algorithmRequest }">
				<h1 class="ui header">Edit: ${ algorithmRequest.toString() }</h1>
			</g:if>
			<g:else>
				<h1 class="ui header">Create Algorithm Request</h1>
			</g:else>
			<div id="checking" class="ui icon message" style="display: none;">
				<i class="notched circle loading icon"></i>
				<div class="content">
					<div class="header">Checking Request Validity</div>
					<p>Checking to see if the new Algorithm Request has valid data</p>
				</div>
			</div>
			<div id="valid" class="ui positive icon message" style="display: none;">
				<i class="positive check icon"></i>
				<div class="content">
					<div class="header">Valid</div>
					<p>The current request contains valid data for the selected input and date range</p>
				</div>
			</div>
			<div id="invalid" class="ui negative icon message" style="display: none;">
				<i class="negative remove icon"></i>
				<div class="content">
					<div class="header">Invalid</div>
					<p></p>
				</div>
			</div>
			<div class="ui form">
				<h3 class="ui dividing header">Name and Boundary Dates</h3>
				<g:field type="hidden" name="id" value="${ algorithmRequest?.id }" />
				<div class="four fields">
					<div class="field">
						<label>Name</label>
						<g:field type="text" name="name" value="${ algorithmRequest?.name ?: 'New Request' }" />
					</div>
					<div class="field">
						<label>Start Offset</label>
						<g:field type="number" name="startOffset" value="${ algorithmRequest?.startOffset ?: -14 }" />
					</div>
					<div class="field">
						<label>End Offset</label>
						<g:field type="number" name="endOffset" value="${ algorithmRequest?.endOffset ?: 0 }" />
					</div>
					<div class="field">
						<label>Time Period</label>
						<g:select name="unit" from="${ ['Day', 'Hour', 'Half Hour'] }" value="${ algorithmRequest?.unit }" />
					</div>
				</div>
				<h3 class="ui dividing header">Add Data Set</h3>
				<div class="four fields">
					<div class="field">
						<label>Stock</label>
						<g:select from="${ dataSets }" name="stock" class="ui search dropdown" />
					</div>
					<div class="field">
						<label>Interval Offset</label>
						<g:field type="number" name="offset" value="0" />
					</div>
					<div class="field">
						<label>Aggregation</label>
						<g:select from="${ Aggregation.values()*.name }" name="aggregation" class="ui search dropdown" value="Period Percent Change" />
					</div>
					<div class="field">
						<label>Add Data Set</label>
						<button onclick="addDataSet()" class="ui primary button">Add Data Set</button>
					</div>
				</div>
				<div class="fields">
					<div class="field">
						<label>Submit</label>
						<button onclick="submitRequest(false)" class="ui positive button">${ algorithmRequest ? 'Update' : 'Create' } Request</button>
					</div>
					<g:if test="${ algorithmRequest }">
						<div class="field">
							<label>Overwrite</label>
							<button onclick="submitRequest(true)" class="ui primary button">Overwrite Request</button>
						</div>
						<div class="field">
							<label>Delete</label>
							<button onclick="deleteRequest()" class="ui negative button">Delete Request</button>
						</div>
					</g:if>
					<div class="field">
						<label>Check Request Validity</label>
						<button onclick="checkRequest()" class="ui button">Check Validity</button>
					</div>
				</div>
				<h3 class="ui dividing header">Data Sets</h3>
				<table id="dataSets" class="ui table">
					<thead>
						<tr>
							<th>Dependant</th>
							<th>Stock</th>
							<th>Interval Offset</th>
							<th>Aggregation</th>
							<th>Remove</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${ algorithmRequest?.requestDataSets }" var="requestDataSet">
							<tr>
								<td>
									<div class="ui radio checkbox">
										<g:field type="radio" name="dependant" checked="${ algorithmRequest.dependantDataSet == requestDataSet.dataSet }" value="${ requestDataSet.dataSet.id }" />
									</div>
								</td>
								<td class="stock">${ requestDataSet.dataSet.toString() }</td>
								<td class="offset">${ requestDataSet.offset }</td>
								<td class="aggregation">${ requestDataSet.aggregation }</td>
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
