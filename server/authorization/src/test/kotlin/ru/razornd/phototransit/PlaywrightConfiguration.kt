package ru.razornd.phototransit

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.springframework.beans.factory.FactoryBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.test.context.event.AfterTestExecutionEvent
import java.nio.file.Paths
import java.time.Instant

@TestConfiguration(proxyBeanMethods = false)
open class PlaywrightConfiguration {

    var page: Page? = null

    @Bean(destroyMethod = "close")
    open fun playwright(): Playwright = Playwright.create()

    @Bean
    open fun pageFactory(playwright: Playwright, env: Environment): FactoryBean<Page> {
        val browser = playwright.chromium().launch()

        return object : FactoryBean<Page> {
            override fun getObject(): Page {
                val port = env.getProperty("local.server.port", "8080")

                return browser.newPage(Browser.NewPageOptions().setBaseURL("http://localhost:$port/")).apply {
                    setDefaultTimeout(2_000.0)
                }.also { page = it }
            }

            override fun getObjectType() = Page::class.java

            override fun isSingleton() = false
        }
    }

    @EventListener(condition = "#event.testContext.testException != null")
    fun screenshotListener(event: AfterTestExecutionEvent) {
        val page = this.page ?: return

        event.source.testClass
        event.source.testMethod

        val screenshotPath = Paths.get("build/reports/screenshots")
            .resolve(event.source.testClass.name)
            .resolve(event.source.testMethod.name)
            .resolve("${Instant.now().toEpochMilli()}.png")

        page.screenshot(Page.ScreenshotOptions().setPath(screenshotPath))
    }

}
