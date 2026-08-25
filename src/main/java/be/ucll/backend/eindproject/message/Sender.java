package be.ucll.backend.eindproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("sender")
public class Sender {

    private static final Logger log = LoggerFactory.getLogger(Sender.class);

    private final RabbitTemplate rabbitTemplate;
    private final Queue queue;

    private long i = 0;

    public Sender(RabbitTemplate rabbitTemplate,
                  Queue queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 500)
    public void sendMessage() {
        final var message = new Message(("  " + i).getBytes());
        log.info("Sending message: {}", message);
        rabbitTemplate.convertAndSend(
                queue.getName(),
                message
        );
        i++;
    }

}