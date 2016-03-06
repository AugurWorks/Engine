grails.servlet.version = "3.0"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
	inherits("global")
	log "error"
	checksums true
	legacyResolve false

	repositories {
		inherits true

		grailsPlugins()
		grailsHome()
		mavenLocal()
		grailsCentral()
		mavenCentral()
		mavenRepo "http://download.java.net/maven/2/"
		mavenRepo "http://repo.spring.io/milestone/"
	}

	dependencies {
		runtime 'com.amazonaws:aws-java-sdk:1.10.6'
		compile "org.codehaus.gpars:gpars:1.1.0"
		runtime 'mysql:mysql-connector-java:5.1.22'
	}

	plugins {
		build ":tomcat:7.0.54"

		compile ":scaffolding:2.1.2"
		runtime ":database-migration:1.4.1"
		compile ":cache:1.1.1"
		compile ":quartz:1.0.1"
		compile ":oauth:2.6.1"
		compile "org.grails.plugins:cache-ehcache:1.0.5"

		runtime ":hibernate4:4.3.8.1"
		compile ":asset-pipeline:2.3.2"
		compile ":spring-security-core:2.0-RC5"
		compile ":spring-security-ui:1.0-RC2"

		compile ":slack-logger:1.0.1"
		compile ":build-test-data:2.4.0"
		compile ":rest-client-builder:2.1.1"

		test ":codenarc:0.22"
		test ":code-coverage:2.0.3-3"
	}
}
codenarc.reports = {
	MyXmlReport('xml') {
		outputFile = 'target/CodeNarcReport.xml'
		title = 'Engine XML Report'
	}
	MyHtmlReport('html') {
		outputFile = 'target/CodeNarcReport.html'
		title = 'Engine HTML Report'
	}
}
codenarc.ruleSetFiles="file:test/CodeNarcRules.groovy"
codenarc.processViews = true

codenarc.systemExitOnBuildException = false

coverage {
	enabledByDefault = false
}