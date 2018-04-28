package com.stella.game.gamelogic.services

import com.stella.game.schema.RoundDto
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AmqpService{
    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var fanout: FanoutExchange

    fun sendRoundCreated(round: RoundDto){
        rabbitTemplate.convertAndSend(fanout.name, "", round)
    }
}