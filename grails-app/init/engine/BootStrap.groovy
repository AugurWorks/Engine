package engine

import grails.core.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.Role
import com.augurworks.engine.domains.User
import com.augurworks.engine.domains.UserRole
import com.augurworks.engine.jobs.SqsPollingJob
import com.augurworks.engine.services.AutoKickoffService
import com.augurworks.engine.services.DataGeneratorService

class BootStrap {

	private static final Logger log = LoggerFactory.getLogger(BootStrap)

    GrailsApplication grailsApplication
	DataGeneratorService dataGeneratorService
	AutoKickoffService autoKickoffService

	def createUser(name, role) {
		User me = new User(username: name).save()
		UserRole.create(me, role, true)
	}

	def init = { servletContext ->
        log.info('Starting bootstrap')

        SqsPollingJob.triggerNow()

        if (Role.count() == 0) {
            Role adminRole = new Role(authority: "ROLE_ADMIN").save()
            createUser('TheConnMan', adminRole)
            createUser('augurworks1', adminRole)
            createUser('gbcolema11', adminRole)

            dataGeneratorService.bootstrapDefaultRequests()
        }

        if (grailsApplication.config.cron.requests.on.toBoolean()) {
            AlgorithmRequest.findAllByCronExpressionIsNotNull().each { AlgorithmRequest algorithmRequest ->
                autoKickoffService.scheduleKickoffJob(algorithmRequest)
            }
        }

		log.info('Finished bootstrapping')
	}
}
