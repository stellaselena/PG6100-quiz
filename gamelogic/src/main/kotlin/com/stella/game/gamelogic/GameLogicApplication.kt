package com.stella.game.gamelogic

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.cloud.netflix.ribbon.RibbonClients
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication(scanBasePackages = arrayOf("com.stella.game.gamelogic"))
@EnableEurekaClient
@RibbonClients(
        RibbonClient(name = "quiz-server"),
        RibbonClient (name = "player-server"),
        RibbonClient (name = "round-server")
)
class GameLogicApplication : WebMvcConfigurerAdapter() {}

fun main(args: Array<String>) {
    SpringApplication.run(GameLogicApplication::class.java, *args)
}