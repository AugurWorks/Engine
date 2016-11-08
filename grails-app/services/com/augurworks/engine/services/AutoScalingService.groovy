package com.augurworks.engine.services

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.autoscaling.model.AutoScalingGroup
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest
import com.augurworks.engine.exceptions.AugurWorksException
import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Transactional
class AutoScalingService {

	private static final Logger log = LoggerFactory.getLogger(AutoScalingService)

	GrailsApplication grailsApplication

	void checkSpinUp() {
		String asgName = grailsApplication.config.autoscaling.name
		if (asgName) {
			checkAsg(asgName)
		}
	}

	private void checkAsg(String asgName) {
		AmazonAutoScalingClient asgClient = new AmazonAutoScalingClient()
		List<AutoScalingGroup> autoScalingGroups = asgClient.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(asgName)).getAutoScalingGroups()
		if (autoScalingGroups.size() != 1) {
			throw new AugurWorksException('Unanticipated number of ASGs: ' + autoScalingGroups*.getAutoScalingGroupName().join(', '))
		}
		AutoScalingGroup asg = autoScalingGroups.first()
		if (asg.getDesiredCapacity() == 0) {
			log.info('ASG ' + asgName + ' size is 0, spinning up an instance')
			asgClient.setDesiredCapacity(new SetDesiredCapacityRequest().withAutoScalingGroupName(asgName).withDesiredCapacity(1))
		}
	}
}
