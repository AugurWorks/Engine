package com.augurworks.engine.controllers

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.ApiKey
import com.augurworks.engine.domains.Product
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.io.SyndFeedOutput
import grails.core.GrailsApplication

class RssController {

    GrailsApplication grailsApplication

    def index(String apiKey, String productName) {
        ApiKey key = ApiKey.findByKey(apiKey)
        Product product = Product.findByName(productName)
        if (!key || !product) {
            render(status: 404)
        } else {
            String serverUrl = grailsApplication.config.grails.serverURL

            SyndFeed feed = new SyndFeedImpl()
            feed.setFeedType('rss_2.0')
            feed.setTitle(product.getName())
            feed.setDescription(product.getName())
            feed.setLink(serverUrl + '/rss/' + apiKey + '/' + productName)

            List<AlgorithmRequest> algorithmRequests = AlgorithmRequest.findAllByProduct(product)

            List<AlgorithmResult> algorithmResults = AlgorithmResult.findAllByAlgorithmRequestInListAndComplete(algorithmRequests, true, [max: 20, sort: 'dateCreated', order: 'desc'])

            feed.setEntries(algorithmResults.collect { result ->
                SyndEntry entry = new SyndEntryImpl()
                entry.setTitle(result.algorithmRequest.name)
                entry.setUpdatedDate(result.getDateCreated())
                entry.setPublishedDate(result.getFutureValue().date)
                SyndContent description = new SyndContentImpl()
                description.setValue(result.getFutureValue().value?.round(3)?.toString())
                entry.setDescription(description)
                return entry
            })

            Writer writer = new StringWriter()
            SyndFeedOutput output = new SyndFeedOutput()
            output.output(feed, writer)
            writer.close()

            render(text: writer.toString(), contentType: 'text/xml')
        }
    }
}
