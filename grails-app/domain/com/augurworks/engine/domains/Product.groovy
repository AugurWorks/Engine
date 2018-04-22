package com.augurworks.engine.domains

import com.amazonaws.services.sns.AmazonSNSClient
import grails.util.Holders

class Product {

    String name
    Double volatilePercentLimit

    Double diffUpperThreshold
    Double diffLowerThreshold

    Double isRealTimePositiveThresholdPercent
    Double isRealTimeNegativeThresholdPercent
    Double isClosePositiveThresholdPercent
    Double isCloseNegativeThresholdPercent

    static constraints = {
        name unique: true
        volatilePercentLimit(nullable: true)

        diffUpperThreshold(nullable: true)
        diffLowerThreshold(nullable: true)

        isRealTimePositiveThresholdPercent(nullable: true)
        isRealTimeNegativeThresholdPercent(nullable: true)
        isClosePositiveThresholdPercent(nullable: true)
        isCloseNegativeThresholdPercent(nullable: true)
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
