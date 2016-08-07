package com.augurworks.engine.services

import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.autoscaling.model.AutoScalingGroup
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlfredEnvironment
import com.augurworks.engine.helper.AlgorithmType

@Transactional
class AutoScalingService {

	GrailsApplication grailsApplication

	void checkSpinUp() {
		String asgName = grailsApplication.config.autoscaling.name
		if (asgName) {
			checkAsg(asgName)
		}
	}

	private void checkAsg(String asgName) {
		AmazonAutoScalingClient asgClient = new AmazonAutoScalingClient()
		List<AutoScalingGroup> asgs = asgClient.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(asgName)).getAutoScalingGroups()
		if (asgs.size() != 1) {
			throw new AugurWorksException('Unanticipated number of ASGs: ' + asgs*.getAutoScalingGroupName().join(', '))
		}
		AutoScalingGroup asg = asgs.first()
		if (asg.getDesiredCapacity() == 0) {
			log.info 'ASG ' + asgName + ' size is 0, spinning up an instance'
			asgClient.setDesiredCapacity(new SetDesiredCapacityRequest().withAutoScalingGroupName(asgName).withDesiredCapacity(1))
		}
	}
}
