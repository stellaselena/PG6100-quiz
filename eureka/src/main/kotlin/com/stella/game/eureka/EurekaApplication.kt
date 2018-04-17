package com.stella.game.eureka

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class EurekaServerApplication

fun main(args: Array<String>) {
    SpringApplication.run(EurekaServerApplication::class.java, *args)
}