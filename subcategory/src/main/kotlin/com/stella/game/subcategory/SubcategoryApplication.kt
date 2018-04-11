package com.stella.game.subcategory

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication(scanBasePackages = arrayOf("com.stella.game.subcategory"))
@EnableEurekaClient
@RibbonClient(name = "category-server")
class Application : WebMvcConfigurerAdapter() {}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
