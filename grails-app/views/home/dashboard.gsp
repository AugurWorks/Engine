<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="semantic"/>
		<title>Alfred Dashboard</title>
	</head>
	<body>
		<%@ page import="com.augurworks.engine.helper.TrainingStopReason" %>
		<div class="ui segment">
			<h2 class="ui floated left header">
				<i class="dashboard icon"></i>
				<div class="content">
					Alfred Dashboard
				</div>
			</h2>
			<g:select class="ui dropdown" name="timeRange" from="${ timeRanges }" optionKey="value" optionValue="key" value="${ timeRange }" style="float: right;" onchange="changeTimeDuration()" />
			<h3 class="ui dividing header" style="clear: both;">Training Stop Reason</h3>
			<div class="ui four statistics">
				<div class="statistic">
					<div class="value">
						${ runs.size() }
					</div>
					<div class="label">
						Total Runs
					</div>
				</div>
				<div class="statistic">
					<div class="value">
						${ runs.grep { it.trainingStopReason == TrainingStopReason.HIT_PERFORMANCE_CUTOFF }.size() }
					</div>
					<div class="label">
						Performance Cutoff
					</div>
				</div>
				<div class="statistic">
					<div class="value">
						${ runs.grep { it.trainingStopReason == TrainingStopReason.OUT_OF_TIME }.size() }
					</div>
					<div class="label">
						Out of Time
					</div>
				</div>
				<div class="statistic">
					<div class="value">
						${ runs.grep { it.trainingStopReason == TrainingStopReason.HIT_TRAINING_LIMIT }.size() }
					</div>
					<div class="label">
						Hit Training Limit
					</div>
				</div>
			</div>
			<h3 class="ui dividing header">Training Stats</h3>
			<div class="ui three statistics">
				<div class="statistic">
					<div class="value">
						${ runs.size() > 0 ? Math.round(runs*.secondsElapsed.sum() / runs.size() / 60 * 100) / 100 : 'N/A' }
					</div>
					<div class="label">
						Avg Run Time (Min)
					</div>
				</div>
				<div class="statistic">
					<div class="value">
						${ runs.size() > 0 ? Math.round(runs*.roundsTrained.sum() / runs.size() * 100) / 100 : 'N/A' }
					</div>
					<div class="label">
						Avg Rounds Trained
					</div>
				</div>
				<div class="statistic">
					<div class="value">
						${ runs.size() > 0 ? Math.round(runs*.rmsError.sum() / runs.size() * 1000) / 1000 : 'N/A' }
					</div>
					<div class="label">
						Avg RMS Error
					</div>
				</div>
			</div>
		</div>
		<script>
			function changeTimeDuration() {
				document.location = '?offset=' + $('#timeRange').val();
			}
		</script>
	</body>
</html>
