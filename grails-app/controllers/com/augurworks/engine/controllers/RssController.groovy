package com.augurworks.engine.controllers

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.ApiKey
import com.augurworks.engine.domains.Product
import com.rometools.rome.feed.synd.*
import com.rometools.rome.io.SyndFeedOutput
import grails.core.GrailsApplication

class RssController {

    GrailsApplication grailsApplication

    def index(String apiKey, String productName) {
        try {
            ApiKey key = ApiKey.findByApiKey(apiKey)
            Product product = Product.findByName(productName)
            if (!key || !product) {
                render(status: 404)
            } else {
                String serverUrl = grailsApplication.config.grails.serverURL

                SyndFeed feed = new SyndFeedImpl()
                feed.setFeedType('rss_2.0')
                feed.setTitle('AugurWorks - ' + product.getName())
                feed.setDescription(product.getName())
                feed.setLink(serverUrl + '/rss/' + apiKey + '/' + productName)

                List<AlgorithmRequest> algorithmRequests = AlgorithmRequest.findAllByProduct(product)

                List<AlgorithmResult> algorithmResults = AlgorithmResult.findAllByAlgorithmRequestInListAndComplete(algorithmRequests, true, [max: 10, sort: 'dateCreated', order: 'desc'])

                feed.setEntries(algorithmResults.collect { result ->
                    SyndEntry entry = new SyndEntryImpl()
                    entry.setTitle(result.algorithmRequest.name)
                    entry.setUpdatedDate(result.getDateCreated())
                    entry.setPublishedDate(result.getFutureValue().date)
                    SyndContent description = new SyndContentImpl()
                    description.setValue(result.actualValue == null ? 'N/A' : result.actualValue.toString())
                    entry.setDescription(description)
                    return entry
                })

                Writer writer = new StringWriter()
                SyndFeedOutput output = new SyndFeedOutput()
                output.output(feed, writer)
                writer.close()

                if (key.getLastUsed() == null || (new Date().getTime() - key.getLastUsed().getTime()) / 1000 >= 3600) {
                    key.lastUsed = new Date()
                    key.save()
                }

                render(text: writer.toString(), contentType: 'text/xml')
            }
        } catch (Exception e) {
            log.error('Unable to render RSS feed', e)
            render(status: 500)
        }
    }
}
