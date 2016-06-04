package com.augurworks.engine.services

import grails.transaction.Transactional

import java.util.concurrent.ScheduledFuture

import javax.annotation.PostConstruct

import org.quartz.CronExpression
import org.slf4j.MDC
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.jobs.AlgorithmRequestJob

@Transactional
class AutoKickoffService {

	@SuppressWarnings("GrailsStatelessService")
	Map<Long, ScheduledFuture> runningJobs = [:]

	@SuppressWarnings("GrailsStatelessService")
	ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler()

	@PostConstruct
	void init() {
		executor.setPoolSize(4)
		executor.initialize()
	}

	void scheduleKickoffJob(AlgorithmRequest algorithmRequest) {
		MDC.put('algorithmRequestId', algorithmRequest.id.toString())
		MDC.put('algorithmRequestName', algorithmRequest.name)
		try {
			log.info 'Creating cron job for ' + algorithmRequest
			if (runningJobs[algorithmRequest.id]) {
				clearJob(algorithmRequest)
			}
			Trigger trigger = createTrigger(algorithmRequest)
			Runnable job = new AlgorithmRequestJob(algorithmRequest.id)
			runningJobs[algorithmRequest.id] = executor.schedule(job, trigger)
			log.info runningJobs.keySet().size() + ' total jobs scheduled'
		} catch (AugurWorksException e) {
			log.warn e.getMessage()
		} catch (e) {
			log.error e
		}
	}

	void clearJob(AlgorithmRequest algorithmRequest) {
		log.info 'Clearing cron job for ' + algorithmRequest
		runningJobs[algorithmRequest.id]?.cancel(false)
		runningJobs.remove(algorithmRequest.id)
	}

	Trigger createTrigger(AlgorithmRequest algorithmRequest) {
		if (!algorithmRequest.cronExpression || !CronExpression.isValidExpression(algorithmRequest.cronExpression)) {
			throw new AugurWorksException('Invalid cron expression of ' + algorithmRequest.cronExpression + ' for ' + algorithmRequest)
		}
		return new CronTrigger(algorithmRequest.cronExpression)
	}
}
