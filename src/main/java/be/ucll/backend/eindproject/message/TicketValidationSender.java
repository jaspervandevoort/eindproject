package be.ucll.backend.eindproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TicketValidationSender {

    private static final Logger log = LoggerFactory.getLogger(TicketValidationSender.class);

    private final RabbitTemplate rabbitTemplate;

    public TicketValidationSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(TicketValidationMessage message) {
        log.info("Queuing ticket validation job: {}", message);
        rabbitTemplate.convertAndSend(RabbitConfig.TICKET_VALIDATION_QUEUE_NAME, message);
    }
}
