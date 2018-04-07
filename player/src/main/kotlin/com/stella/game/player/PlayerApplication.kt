package com.stella.game.player

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication(scanBasePackages = arrayOf("com.stella.game.player"))
@EnableEurekaClient
@RibbonClient(name = "quiz-server")
class PlayerApplication : WebMvcConfigurerAdapter() {}

fun main(args: Array<String>) {
    SpringApplication.run(PlayerApplication::class.java, *args)
}
