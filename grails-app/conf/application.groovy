String getEnv(String name) {
	return System.getProperty(name) ?: System.getenv(name)
}

grails.project.groupId = appName

version = "${appVersion}"

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

logging.files = getEnv('ENGINE_LOGGING_FILES') ?: false
slack.slash.token = getEnv('SLASH_TOKEN')
slack.webhook = getEnv('SLACK_WEBHOOK')
cron.requests.on = getEnv('CRON_REQUESTS_ON') ?: true

grails.serverURL = getEnv('SERVER_URL') ?: 'http://localhost:8080'

messaging {
	sqsName = getEnv('SQS_NAME') ?: 'Training-Results-Local'
	snsTopicArn = getEnv('SNS_TOPIC_ARN') ?: 'arn:aws:sns:us-east-1:274685854631:Alfred-Training-Local'
}

logging {
	fluentHost = getEnv('FLUENTD_HOST')
	env = getEnv('ENV') ?: 'LOCAL'
}

autoscaling {
	name = getEnv('ASG_NAME')
}

environments {
	development {
		grails.logging.jul.usebridge = true
		aws.bucket = 'aw-files-dev'
		augurworks.predictions.channel = '#testing'
	}
	test {
		grails.logging.jul.usebridge = true
		aws.bucket = 'aw-files-test'
		augurworks.predictions.channel = '#testing'
	}
	production {
		grails.logging.jul.usebridge = false
		aws.bucket = getEnv('BUCKET') ?: 'aw-files-dev'
		augurworks.predictions.channel = getEnv('CHANNEL') ?: '#testing'
		grails.plugin.databasemigration.updateOnStart = true
	}
}

oauth {
	providers {
		github {
			api = com.theconnman.GitHubApi
			successUri = '/github'
			failureUri = '/error'
			key = getEnv('OAUTH_KEY') ?: 'xxxx'
			secret = getEnv('OAUTH_SECRET') ?: 'xxxx'
			callback = "${ grails.serverURL }/oauth/github/callback"
		}
	}
}

augurworks {
	quandl {
		key = getEnv('QUANDL_KEY')
	}
	barchart {
		key = getEnv('BARCHART_KEY')
	}
	td {
		key = getEnv('TD_KEY')
	}
	datePathFormat = 'yyyy/MM/dd/'
	ml {
		max = getEnv('ML_MAX') ?: 10
	}
}

rabbitmq {
	username = getEnv('RABBITMQ_USERNAME') ?: 'guest'
	password = getEnv('RABBITMQ_PASSWORD') ?: 'guest'
	hostname = getEnv('RABBITMQ_HOST') ?: 'rabbitmq'
	port = getEnv('RABBITMQ_PORTNUM') ?: 5672
	env = getEnv('ENV') ?: 'DEV'
}

grails.cache.config = {
	cache {
		name 'externalData'
		overflowToDisk true
		timeToLiveSeconds 300
	}
}

grails.app.context = '/'

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.augurworks.engine.domains.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.augurworks.engine.domains.UserRole'
grails.plugin.springsecurity.authority.className = 'com.augurworks.engine.domains.Role'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
		[pattern: '/**',               access: ['permitAll']],
		[pattern: '/home/dashboard',               access: ['ROLE_ADMIN']],
		[pattern: '/graph/**',               access: ['ROLE_ADMIN']],
		[pattern: '/algorithmRequest/**',               access: ['ROLE_ADMIN']],
		[pattern: '/algorithmResult/**',               access: ['ROLE_ADMIN']],
		[pattern: '/dataSet/**',               access: ['ROLE_ADMIN']],
		[pattern: '/predictedValue/**',               access: ['ROLE_ADMIN']],
		[pattern: '/requestDataSet/**',               access: ['ROLE_ADMIN']],
		[pattern: '/machineLearningModel/**',               access: ['ROLE_ADMIN']],
		[pattern: '/user/**',               access: ['ROLE_ADMIN']],
		[pattern: '/role/**',               access: ['ROLE_ADMIN']]
]

grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.auth.loginFormUrl = '/login/denied'
grails.plugin.springsecurity.adh.errorPage = null

dataSource {
	pooled = true
	jmxExport = true
	driverClassName = "org.h2.Driver"
	username = "sa"
	password = ""
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = false
	cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory'
	singleSession = true
	flush.mode = 'manual'
}

environments {
	development {
		dataSource {
			dbCreate = "create-drop"
			url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
		}
	}
	test {
		dataSource {
			dbCreate = "create-drop"
			url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
		}
	}
	production {
		dataSource {
			driverClassName = "com.mysql.jdbc.Driver"
			dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
			username = System.getProperty('RDS_USERNAME') ?: (System.getenv('RDS_USERNAME') ?: 'root')
			password = System.getProperty('RDS_PASSWORD') ?: System.getenv('RDS_PASSWORD')
			String host = System.getProperty('RDS_HOSTNAME') ?: System.getenv('RDS_HOSTNAME')
			String port = System.getProperty('RDS_PORT') ?: (System.getenv('RDS_PORT') ?: '3306')
			String dbName = System.getProperty('RDS_DB_NAME') ?: (System.getenv('RDS_DB_NAME') ?: 'engine')
			url = "jdbc:mysql://$host:$port/$dbName?useUnicode=true&autoReconnect=true"
			pooled = true
			properties {
				maxActive = -1
				minEvictableIdleTimeMillis=1800000
				timeBetweenEvictionRunsMillis=1800000
				numTestsPerEvictionRun=3
				testOnBorrow=true
				testWhileIdle=true
				testOnReturn=true
				validationQuery="SELECT 1"
			}
		}
	}
}
