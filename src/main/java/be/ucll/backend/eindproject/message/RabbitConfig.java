package be.ucll.backend.eindproject.message;

import be.ucll.backend.eindproject.config.TicketValidationProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String MESSAGE_QUEUE_NAME = "messages";

    // Ticket validation job queue. Failed jobs are dead-lettered into the retry
    // queue; once the TTL there expires they fall back into this queue for
    // another attempt.
    public static final String TICKET_VALIDATION_QUEUE_NAME = "ticket-validation-queue";
    public static final String TICKET_VALIDATION_RETRY_QUEUE_NAME = "ticket-validation-retry-queue";
    public static final String TICKET_VALIDATION_FAILED_QUEUE_NAME = "ticket-validation-failed-queue";

    // Declare the RabbitMQ queue. This ensures the queue is created
    // if it does not exist yet
    @Bean
    public Queue queue() {
        return new Queue(MESSAGE_QUEUE_NAME);
    }

    @Bean
    public Queue ticketValidationQueue() {
        return QueueBuilder.durable(TICKET_VALIDATION_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TICKET_VALIDATION_RETRY_QUEUE_NAME)
                .build();
    }

    @Bean
    public Queue ticketValidationRetryQueue(TicketValidationProperties properties) {
        return QueueBuilder.durable(TICKET_VALIDATION_RETRY_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TICKET_VALIDATION_QUEUE_NAME)
                .withArgument("x-message-ttl", properties.retryDelayMs())
                .build();
    }

    @Bean
    public Queue ticketValidationFailedQueue() {
        return QueueBuilder.durable(TICKET_VALIDATION_FAILED_QUEUE_NAME).build();
    }

    // Register Jackson2JsonMessageConverter bean so that our messages are converted
    // to/from JSON using Jackson
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
