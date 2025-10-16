package ru.razornd.phototransit.storage

import org.assertj.db.type.AssertDbConnection
import org.assertj.db.type.AssertDbConnectionFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource

@TestConfiguration(proxyBeanMethods = false)
class AssertjDbConfiguration {
    @Bean
    fun assertJDbConnection(dataSource: DataSource): AssertDbConnection {
        val sourceProxy = TransactionAwareDataSourceProxy(dataSource)

        return AssertDbConnectionFactory.of(sourceProxy).create()
    }
}
