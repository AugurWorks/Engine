package com.augurworks.engine.domains

import com.amazonaws.services.sns.AmazonSNSClient
import grails.util.Holders

class Product {

    String name
    Double volatilePercentLimit
    Double realTimeDiffUpper
    Double realTimeDiffLower

    static constraints = {
        name unique: true
        volatilePercentLimit(nullable: true)
        realTimeDiffUpper(nullable: true)
        realTimeDiffLower(nullable: true)
    }

    static mapping = {
        sort 'name'
    }

    String getSnsTopicName() {
        return Holders.config.logging.env + '-' + this.id
    }

    String getSnsTopicArn() {
        return Holders.config.sns.prefix + getSnsTopicName()
    }

    def afterInsert() {
        AmazonSNSClient snsClient = new AmazonSNSClient()
        try {
            snsClient.createTopic(getSnsTopicName())
        } catch (Exception e) {
            log.error('Unable to create SNS topic ' + getSnsTopicArn(), e)
        }
    }

    def beforeDelete() {
        AmazonSNSClient snsClient = new AmazonSNSClient()
        try {
            snsClient.deleteTopic(getSnsTopicArn())
        } catch (Exception e) {
            log.error('Unable to delete SNS topic ' + getSnsTopicArn(), e)
        }
    }
}
