package com.stella.game.quiz

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.netflix.config.ConfigurationManager
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.client.RestTemplate
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
@EnableJpaRepositories(basePackages = arrayOf("com.stella.game.quiz"))
@EntityScan(basePackages = arrayOf("com.stella.game.quiz"))
@ComponentScan(basePackages = arrayOf("com.stella.game.quiz"))
class QuizApplicationConfig {

    init {
        //Hystrix configuration
        val conf = ConfigurationManager.getConfigInstance()
        // how long to wait before giving up a request?
        conf.setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", 1000) //default is 1000
        // how many failures before activating the CB?
        conf.setProperty("hystrix.command.default.circuitBreaker.requestVolumeThreshold", 20) //default 20
        conf.setProperty("hystrix.command.default.circuitBreaker.errorThresholdPercentage", 50)
        //for how long should the CB stop requests? after this, 1 single request will try to check if remote server is ok
        conf.setProperty("hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds", 5000)
    }

    @Bean
    fun swaggerApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.any())
                .build()
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
                .title("API for entity quiz")
                .description("Micro-service for Quiz entity.")
                .version("1.0")
                .build()
    }

    @Bean(name = arrayOf("OBJECT_MAPPER_BEAN"))
    fun jsonObjectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(JavaTimeModule())
                .build()
    }

    @LoadBalanced
    @Bean
    @Profile("docker")
    fun restTemplateBalancer(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    @Profile("!docker")
    fun restTemplate() : RestTemplate {
        return RestTemplate()
    }

}