import org.codenarc.rule.generic.IllegalRegexRule
import org.codenarc.rule.generic.RequiredRegexRule

ruleset {
	description 'Specific CodeNarc settings for Engine.'

	ruleset('rulesets/basic.xml')

	ruleset('rulesets/braces.xml')

	ruleset('rulesets/concurrency.xml')

	ruleset('rulesets/convention.xml') {
		'ParameterReassignment' {
			enabled = false
		}
		'IfStatementCouldBeTernary' {
			doNotApplyToFileNames = "*Controller*,*Command*"
		}
		'NoDef' {
			doNotApplyToFileNames = "*Tests.groovy,*Specification.groovy,*Spec.groovy,*Controller.groovy,*TagLib.groovy"
			excludeRegex = /(beforeInsert|afterInsert|beforeUpdate|afterUpdate|beforeDelete|afterDelete|beforeValidate|onLoad)/
		}
	}

	ruleset('rulesets/enhanced.xml')

	ruleset('rulesets/exceptions.xml')

	ruleset('rulesets/formatting.xml') {
		'LineLength' {
			enabled = false
		}
		'SpaceBeforeOpeningBrace' {
			enabled = false
		}
		'SpaceAfterOpeningBrace' {
			enabled = false
		}
		'SpaceBeforeClosingBrace' {
			enabled = false
		}
		'SpaceAfterClosingBrace' {
			enabled = false
		}
		'SpaceAroundOperator' {
			enabled = false
		}
		'SpaceAfterIf' {
			enabled = false
		}
		'SpaceAfterComma' {
			enabled = false
		}
		'SpaceAfterCatch' {
			enabled = false
		}
		'SpaceAfterWhile' {
			enabled = false
		}
		'SpaceAfterSwitch' {
			enabled = false
		}
		'SpaceAfterFor' {
			enabled = false
		}
		'SpaceAroundMapEntryColon' {
			enabled = false
		}
		'SpaceAroundClosureArrow' {
			enabled = false
		}
		'ClassJavadoc' {
			enabled = false
		}
		'ConsecutiveBlankLines' {
			enabled = false
		}
		'FileEndsWithoutNewline' {
			enabled = false
		}
	}

	ruleset('rulesets/generic.xml') {
		'StatelessClass' {
			enabled = false
		}
	}

	ruleset('rulesets/grails.xml') {
		'GrailsStatelessService' {
			addToIgnoreFieldNames = 'applicationContext, messageSource, grailsApplication, sessionFactory'
			priority = 1
		}
		'GrailsPublicControllerMethod' {
			enabled = false
		}
		'GrailsDomainHasToString' {
			enabled = false
		}
		'GrailsDomainHasEquals' {
			enabled = false
		}
		'GrailsDomainReservedSqlKeywordName' {
			enabled = false
		}
		'GrailsDomainWithServiceReference' {
			enabled = false
		}
	}

	ruleset('rulesets/groovyism.xml') {
		'ExplicitCallToEqualsMethod' {
			ignoreThisReference = true
		}
		'GStringExpressionWithinString' {
			doNotApplyToFileNames = "*Tag*Tests*"
		}
	}

	ruleset('rulesets/imports.xml') {
		'NoWildcardImports' {
			enabled = false
		}
	}

	ruleset('rulesets/jdbc.xml')

	ruleset('rulesets/junit.xml') {
		'JUnitStyleAssertions' {
			enabled = false
		}
		'JUnitLostTest' {
			enabled = false
		}
		'JUnitPublicProperty' {
			enabled = false
		}
	}

	ruleset('rulesets/logging.xml') {
		'Println' {
			doNotApplyToClassNames = "*Tests"
		}
	}

	ruleset('rulesets/naming.xml') {
		'FactoryMethodName' {
			enabled = false
		}
		'FieldName' {
			finalRegex = null
			staticRegex = /[A-Z][A-Z0-9_]*/
		}
		'MethodName' {
			doNotApplyToClassNames = "*Specification,*Spec"
		}
		'VariableName' {
			finalRegex = /[a-z][a-zA-Z0-9]*/
		}
	}

	ruleset("rulesets/security.xml") {
		'FileCreateTempFile' {
			enabled = false
		}
		'JavaIoPackageAccess' {
			enabled = false
		}
	}

	ruleset("rulesets/serialization.xml") {
		'SerializableClassMustDefineSerialVersionUID' {
			enabled = false
		}
	}

	ruleset("rulesets/size.xml") {
		'AbcMetric' {
			doNotApplyToClassNames = "*Tests,*Specification,*Spec"
			enabled = false
		}
		'ClassSize' {
			doNotApplyToClassNames = "*Tests,*Specification,*Spec"
		}
		'CyclomaticComplexity' {
			doNotApplyToClassNames = "*Tests,*Specification,*Spec"
		}
		'MethodCount' {
			doNotApplyToClassNames = "*Tests,*Specification,*Spec"
			maxMethods = 40
		}
		'MethodSize' {
			doNotApplyToClassNames = "*Tests,*Specification,*Spec"
		}
		'NestedBlockDepth' {
			doNotApplyToClassNames = "*Tests,*Specification,*Spec"
		}
	}

	ruleset("rulesets/unnecessary.xml") {
		'UnnecessaryReturnKeyword' {
			enabled = false
		}
		'UnnecessaryObjectReferences' {
			enabled = false
		}
		'UnnecessaryGetter' {
			enabled = false
		}
		'UnnecessaryGString' {
			enabled = false
		}
		'UnnecessaryTransientModifier' {
			enabled = false
		}
		'UnnecessarySemicolon' {
			doNotApplyToFileNames = '*.gsp'
		}
	}

	ruleset('rulesets/unused.xml')

	rule(IllegalRegexRule) {
		name = 'NoBlankConstraintDomainClass'
		regex = /blank\:/
		description = 'Since upgrade to Grails 2.3.5 handles blanks coming in from web, we do not need blank constraints in command objects anymore.'
		priority = 2
		applyToFilesMatching = /.*grails-app\/(controllers|domain)\/.*/
	}

	rule(IllegalRegexRule) {
		name = 'NoRequiresPermissionsAnnotationOnPrivateMethod'
		regex = /@RequiresPermissions\(.*\).*\s*private/
		description = 'RequiresPermissions annotations on private service methods are not intercepted.  These are programming errors.'
		priority = 2
		applyToFilesMatching = /.*grails-app\/services\/.*/
	}

	rule(IllegalRegexRule) {
		name = 'NoTransactionalAnnotationOnPrivateMethod'
		regex = /@Transactional\(.*\).*\s*private/
		description = 'Transactional annotations on private service methods are not intercepted.  These are programming errors.'
		priority = 2
		applyToFilesMatching = /.*grails-app\/services\/.*/
	}

	rule(IllegalRegexRule) {
		name = 'NoTrailingWhitespace'
		regex = /(\t| )(\n|\r|$)/
		description = 'Whitespace at the end of a line is unnecessary and dirties the file.'
		priority = 2
		applyToFilesMatching = /.*/
	}
}