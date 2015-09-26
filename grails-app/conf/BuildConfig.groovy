grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		// excludes 'ehcache'
	}
	log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
	legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

	repositories {
		inherits true // Whether to inherit repository definitions from plugins

		grailsPlugins()
		grailsHome()
		mavenLocal()
		grailsCentral()
		mavenCentral()
		mavenRepo "http://download.java.net/maven/2/"
		mavenRepo "http://repo.spring.io/milestone/"
		// uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
	}

	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
		// runtime 'mysql:mysql-connector-java:5.1.27'
		// runtime 'org.postgresql:postgresql:9.3-1100-jdbc41'
		runtime 'com.amazonaws:aws-java-sdk:1.10.6'
		compile "org.codehaus.gpars:gpars:1.1.0"
	}

	plugins {
		// plugins for the build system only
		build ":tomcat:7.0.54"

		// plugins for the compile step
		compile ":scaffolding:2.1.2"
		compile ":cache:1.1.1"
		compile ":quartz:1.0.1"
		compile ":oauth:2.6.1"

		// plugins needed at runtime but not for compilation
		runtime ":hibernate4:4.3.8.1"
		compile ":asset-pipeline:2.3.2"
		compile ":spring-security-core:2.0-RC5"

		compile ":slack-logger:1.0.1"
		compile ":build-test-data:2.4.0"

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