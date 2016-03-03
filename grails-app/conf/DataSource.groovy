dataSource {
	pooled = true
	jmxExport = true
	driverClassName = "org.h2.Driver"
	username = "sa"
	password = ""
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = false
	cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory'
	singleSession = true
	flush.mode = 'manual'
}

environments {
	development {
		dataSource {
			dbCreate = "create-drop"
			url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
		}
	}
	test {
		dataSource {
			dbCreate = "create-drop"
			url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
		}
	}
	production {
		dataSource {
			dbCreate = "create-drop"
			driverClassName = "com.mysql.jdbc.Driver"
			dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
			username = System.getProperty('RDS_USERNAME') ?: (System.getenv('RDS_USERNAME') ?: 'root')
			password = System.getProperty('RDS_PASSWORD') ?: System.getenv('RDS_PASSWORD')
			String host = System.getProperty('RDS_HOSTNAME') ?: System.getenv('RDS_HOSTNAME')
			String port = System.getProperty('RDS_PORT') ?: (System.getenv('RDS_PORT') ?: '3306')
			String dbName = System.getProperty('RDS_DB_NAME') ?: (System.getenv('RDS_DB_NAME') ?: 'engine')
			url = "jdbc:mysql://$host:$port/$dbName?useUnicode=true&autoReconnect=true"
			pooled = true
			properties {
				maxActive = -1
				minEvictableIdleTimeMillis=1800000
				timeBetweenEvictionRunsMillis=1800000
				numTestsPerEvictionRun=3
				testOnBorrow=true
				testWhileIdle=true
				testOnReturn=true
				validationQuery="SELECT 1"
			}
		}
	}
}
