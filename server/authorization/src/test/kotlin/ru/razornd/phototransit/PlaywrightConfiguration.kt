package ru.razornd.phototransit

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.springframework.beans.factory.FactoryBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.test.context.event.AfterTestExecutionEvent
import java.nio.file.Paths
import java.time.Instant
import kotlin.io.path.createTempDirectory

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

                val context = browser.newContext(
                    Browser.NewContextOptions()
                        .setBaseURL("http://localhost:$port/")
                        .setRecordVideoDir(createTempDirectory())
                        .setRecordVideoSize(640, 480)
                        .setViewportSize(640, 480)
                )

                return context.newPage().apply {
                    setDefaultTimeout(2_000.0)
                }.also { page = it }
            }

            override fun getObjectType() = Page::class.java

            override fun isSingleton() = false
        }
    }

    @Order(0)
    @EventListener(condition = "#event.testContext.testException != null")
    fun screenshotListener(event: AfterTestExecutionEvent) {
        val page = this.page ?: return

        val screenshotPath = Paths.get("build/reports/screenshots")
            .resolve(event.source.testClass.name)
            .resolve(event.source.testMethod.name)
            .resolve("${Instant.now().toEpochMilli()}.png")

        page.screenshot(Page.ScreenshotOptions().setPath(screenshotPath))
    }

    @Order(10)
    @EventListener(condition = "#event.testContext.testException != null")
    fun videoListener(event: AfterTestExecutionEvent) {
        val page = this.page ?: return

        val videoPath = Paths.get("build/reports/videos")
            .resolve(event.source.testClass.name)
            .resolve(event.source.testMethod.name)
            .resolve("${Instant.now().toEpochMilli()}.webm")

        page.video().saveAs(videoPath)
    }

    @Order(5)
    @EventListener
    fun closePageListener(event: AfterTestExecutionEvent) {
        page?.close()
    }

}
