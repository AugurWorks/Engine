package com.augurworks.engine.jobs

import com.augurworks.engine.domains.Product
import com.augurworks.engine.helper.TradingHours
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WelcomeJob {

	private static final Logger log = LoggerFactory.getLogger(WelcomeJob)

	static triggers = {
		cron name: 'nightly', startDelay: 10000, cronExpression: '0 0 9 * * ?'
	}

	void execute() {
		log.debug("Kicking off the Welcome job")
		if (!TradingHours.isMarketOpen(new Date())) {
			log.debug("Today is not a trading day, exiting")
			return
		}
		List<Product> products = Product.list()
		products.forEach({ product ->
			log.debug("Sending out welcome message for product " + product.id)
			product.sendSnsMessage("Good morning from AugurWorks. We'll be trading " + product.name + " today.")
		})
	}
}
