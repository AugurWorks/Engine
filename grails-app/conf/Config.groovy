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

alfred.url = System.getProperty('ALFRED_URL') ?: (System.getenv('ALFRED_URL') ?: 'http://localhost:8081')
logging.files = System.getProperty('ENGINE_LOGGING_FILES') ?: false
slack.slash.token = System.getProperty('SLASH_TOKEN') ?: System.getenv('SLASH_TOKEN')
slack.on = System.getProperty('SLACK_ON') ?: (System.getenv('SLACK_ON') ?: 'false')

environments {
	development {
		grails.logging.jul.usebridge = true
		grails.serverURL = System.getProperty('SERVER_URL') ?: (System.getenv('SERVER_URL') ?: 'http://localhost:8080')
		oauth.providers.github.key = System.getProperty('OAUTH_KEY') ?: System.getenv('OAUTH_KEY')
		oauth.providers.github.secret = System.getProperty('OAUTH_SECRET') ?: System.getenv('OAUTH_SECRET')
		aws.bucket = 'aw-files-dev'
		augurworks.predictions.channel = '#testing'
	}
	test {
		grails.logging.jul.usebridge = true
		grails.serverURL = System.getProperty('SERVER_URL') ?: (System.getenv('SERVER_URL') ?: 'http://localhost:8080')
		oauth.providers.github.key = System.getProperty('OAUTH_KEY') ?: System.getenv('OAUTH_KEY')
		oauth.providers.github.secret = System.getProperty('OAUTH_SECRET') ?: System.getenv('OAUTH_SECRET')
		aws.bucket = 'aw-files-test'
		augurworks.predictions.channel = '#testing'
	}
	production {
		grails.logging.jul.usebridge = false
		grails.serverURL = System.getProperty('SERVER_URL') ?: (System.getenv('SERVER_URL') ?: 'http://localhost:8080')
		oauth.providers.github.key = System.getProperty('OAUTH_KEY') ?: System.getenv('OAUTH_KEY')
		oauth.providers.github.secret = System.getProperty('OAUTH_SECRET') ?: System.getenv('OAUTH_SECRET')
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
		key = System.getProperty('QUANDL_KEY') ?: System.getenv('QUANDL_KEY')
	}
	barchart {
		key = System.getProperty('BARCHART_KEY') ?: System.getenv('BARCHART_KEY')
	}
	td {
		key = System.getProperty('TD_KEY') ?: System.getenv('TD_KEY')
	}
	datePathFormat = 'yyyy/MM/dd/'
	ml {
		max = System.getProperty('ML_MAX') ?: (System.getenv('ML_MAX') ?: 10)
	}
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