eventCreateWarStart = { warName, stagingDir ->
	ant.copy file:"${basedir}/grails-app/conf/logback.groovy", todir: "${stagingDir}/WEB-INF/classes"
}