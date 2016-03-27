import com.theconnman.slacklogger.SlackAppender

def loc = ['../UserConfig.groovy', 'webapps/ROOT/Jenkins.groovy'].grep { new File(it).exists() }.first()
def localConfig = new ConfigSlurper(grailsSettings.grailsEnv).parse(new File(loc).toURI().toURL())

grails.project.groupId = appName

grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [
	all:           '*/*',
	atom:          'application/atom+xml',
	css:           'text/css',
	csv:           'text/csv',
	form:          'application/x-www-form-urlencoded',
	html:          ['text/html','application/xhtml+xml'],
	js:            'text/javascript',
	json:          ['application/json', 'text/json'],
	multipartForm: 'multipart/form-data',
	rss:           'application/rss+xml',
	text:          'text/plain',
	hal:           ['application/hal+json','application/hal+xml'],
	xml:           ['text/xml', 'application/xml']
]

grails.views.default.codec = "html"

grails.controllers.defaultScope = 'singleton'

grails {
	views {
		gsp {
			encoding = 'UTF-8'
			htmlcodec = 'xml'
			codecs {
				expression = 'html'
				scriptlet = 'html'
				taglib = 'none'
				staticparts = 'none'
			}
		}
	}
}

 
grails.converters.encoding = "UTF-8"
grails.scaffolding.templates.domainSuffix = 'Instance'

grails.json.legacy.builder = false
grails.enable.native2ascii = true
grails.spring.bean.packages = []
grails.web.disable.multipart=false

grails.exceptionresolver.params.exclude = ['password']

grails.hibernate.cache.queries = false

grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

alfred.url = (localConfig.alfred.url ?: 'http://localhost:8080')
logging.files = System.getProperty('ENGINE_LOGGING_FILES') ?: false
slack.slash.token = System.getProperty('SLASH_TOKEN') ?: System.getenv('SLASH_TOKEN')
slack.on = System.getProperty('SLACK_ON') ?: (System.getenv('SLACK_ON') ?: 'false')

environments {
	development {
		grails.logging.jul.usebridge = true
		grails.serverURL = (localConfig.local.ip ?: 'http://localhost') + ':8080'
		oauth.providers.github.key = localConfig.oauth.github.key
		oauth.providers.github.secret = localConfig.oauth.github.secret
		aws.bucket = 'aw-files-dev'
		augurworks.predictions.channel = '#testing'
	}
	test {
		grails.logging.jul.usebridge = true
		grails.serverURL = (localConfig.local.ip ?: 'http://localhost') + ':8080'
		oauth.providers.github.key = localConfig.oauth.github.key
		oauth.providers.github.secret = localConfig.oauth.github.secret
		aws.bucket = 'aw-files-test'
		augurworks.predictions.channel = '#testing'
	}
	production {
		grails.logging.jul.usebridge = false
		grails.serverURL = System.getProperty('SERVER_URL') ?: (System.getenv('SERVER_URL') ?: (localConfig.local.ip ? localConfig.local.ip + ':8080' : null))
		oauth.providers.github.key = localConfig.oauth.github.key
		oauth.providers.github.secret = localConfig.oauth.github.secret
		aws.bucket = System.getProperty('BUCKET') ?: (System.getenv('BUCKET') ?: 'aw-files-dev')
		augurworks.predictions.channel = System.getProperty('CHANNEL') ?: (System.getenv('CHANNEL') ?: '#testing')
		grails.plugin.databasemigration.updateOnStart = true
	}
}

oauth {
	providers {
		github {
			api = com.theconnman.GitHubApi
			successUri = '/github'
			failureUri = '/error'
			callback = "${ grails.serverURL }/oauth/github/callback"
		}
	}
}

augurworks {
	quandl {
		key = localConfig.augurworks.quandlKey
	}
	datePathFormat = 'yyyy/MM/dd/'
}

grails.cache.config = {
	cache {
		name 'externalData'
		overflowToDisk true
		timeToLiveSeconds 300
	}
}

log4j = {
	appenders {
		console name: 'stdout', threshold: org.apache.log4j.Level.ERROR
		rollingFile name: 'debug', file: 'logs/debug.log', layout: pattern(conversionPattern: '[%p] %d{yyyy-MM-dd HH:mm:ss} %c{2} - %m%n'), threshold: org.apache.log4j.Level.DEBUG
		rollingFile name: 'info', file: 'logs/info.log', layout: pattern(conversionPattern: '[%p] %d{yyyy-MM-dd HH:mm:ss} %c{2} - %m%n'), threshold: org.apache.log4j.Level.INFO
		rollingFile name: 'warn', file: 'logs/warn.log', layout: pattern(conversionPattern: '[%p] %d{yyyy-MM-dd HH:mm:ss} %c{2} - %m%n'), threshold: org.apache.log4j.Level.WARN
		appender new SlackAppender(name: 'slackAppender', layout: pattern(conversionPattern: '%c{2} - %m%n'), threshold: org.apache.log4j.Level.ERROR)
	}

	warn 'warn': [
		'grails.app.controllers.com.augurworks.engine',
		'grails.app.services.com.augurworks.engine',
		'grails.app.conf.com.augurworks.engine',
		'grails.app.domain.com.augurworks.engine',
		'com.augurworks.engine.helper'
	]

	info 'info': [
		'grails.app.controllers.com.augurworks.engine',
		'grails.app.services.com.augurworks.engine',
		'grails.app.conf.com.augurworks.engine',
		'grails.app.domain.com.augurworks.engine',
		'com.augurworks.engine.helper'
	]

	debug 'debug': [
		'grails.app.controllers.com.augurworks.engine',
		'grails.app.services.com.augurworks.engine',
		'grails.app.conf.com.augurworks.engine',
		'grails.app.domain.com.augurworks.engine',
		'com.augurworks.engine.helper'
	]
}

grails.app.context = '/'

grails {
	plugin {
		slacklogger {
			webhook = localConfig.augurworks.slacklogger.webhook
			botName = 'Engine Errors'
			channel = '#engine-errors'
		}
	}
}

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.augurworks.engine.domains.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.augurworks.engine.domains.UserRole'
grails.plugin.springsecurity.authority.className = 'com.augurworks.engine.domains.Role'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	'/**':								['permitAll'],
	'/graph/**':						['ROLE_ADMIN'],
	'/algorithmRequest/**':				['ROLE_ADMIN'],
	'/algorithmResult/**':				['ROLE_ADMIN'],
	'/dataSet/**':						['ROLE_ADMIN'],
	'/predictedValue/**':				['ROLE_ADMIN'],
	'/requestDataSet/**':				['ROLE_ADMIN'],
	'/machineLearningModel/**':			['ROLE_ADMIN'],
	'/user/**':							['ROLE_ADMIN'],
	'/role/**':							['ROLE_ADMIN']
]

grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.auth.loginFormUrl = '/login/denied'
grails.plugin.springsecurity.adh.errorPage = null