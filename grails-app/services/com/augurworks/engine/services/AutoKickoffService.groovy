package com.augurworks.engine.services

import grails.transaction.Transactional

import java.util.concurrent.ScheduledFuture

import javax.annotation.PostConstruct

import org.quartz.CronExpression
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.jobs.AlgorithmRequestJob

@Transactional
class AutoKickoffService {

	Map<Long, ScheduledFuture> runningJobs = [:]

	ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler()

	@PostConstruct
	void init() {
		executor.setPoolSize(4)
		executor.initialize()
	}

	void scheduleKickoffJob(AlgorithmRequest algorithmRequest) {
		try {
			if (runningJobs[algorithmRequest.id]) {
				runningJobs[algorithmRequest.id].cancel(false)
			}
			Trigger trigger = createTrigger(algorithmRequest)
			Runnable job = new AlgorithmRequestJob(algorithmRequest.id)
			runningJobs[algorithmRequest.id] = executor.schedule(job, trigger)
		} catch (AugurWorksException e) {
			log.warn e.getMessage()
		} catch (e) {
			log.error e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
		}
	}

	Trigger createTrigger(AlgorithmRequest algorithmRequest) {
		if (!algorithmRequest.cronExpression || !CronExpression.isValidExpression(algorithmRequest.cronExpression)) {
			throw new AugurWorksException('Invalid cron expression of ' + algorithmRequest.cronExpression + ' for ' + algorithmRequest)
		}
		return new CronTrigger(algorithmRequest.cronExpression)
	}
}
