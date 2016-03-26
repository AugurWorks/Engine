package com.augurworks.engine.services

import grails.transaction.Transactional

import javax.annotation.PostConstruct

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.rest.BarchartClient

@Transactional
class BarchartService {

	GrailsApplication grailsApplication
	BarchartClient barchartClient

	@PostConstruct
	void init() {
		barchartClient = new BarchartClient(grailsApplication.config.augurworks.barchart.key)
	}

	Collection<Map> searchSymbol(String keyword) {
		return barchartClient.searchSymbol(keyword)
	}
}
