package be.ucll.backend.eindproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TicketAlertSender {

    private static final Logger log = LoggerFactory.getLogger(TicketAlertSender.class);

    private final RabbitTemplate rabbitTemplate;

    public TicketAlertSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(TicketAlertMessage message) {
        log.info("Sending ticket alert: {}", message);
        rabbitTemplate.convertAndSend(RabbitConfig.MESSAGE_QUEUE_NAME, message);
    }
}
