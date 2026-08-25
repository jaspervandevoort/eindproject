package be.ucll.backend.eindproject.message;

import be.ucll.backend.eindproject.model.Organizer;
import be.ucll.backend.eindproject.model.Ticket;
import be.ucll.backend.eindproject.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class TicketValidationReceiver {

    private static final Logger log = LoggerFactory.getLogger(TicketValidationReceiver.class);

    private final TicketRepository ticketRepository;
    private final RestClient ticketValidationRestClient;
    private final RabbitTemplate rabbitTemplate;
    private final int maxAttempts;

    public TicketValidationReceiver(TicketRepository ticketRepository,
                                     RestClient ticketValidationRestClient,
                                     RabbitTemplate rabbitTemplate,
                                     @Value("${ticket.validation.max-attempts:4}") int maxAttempts) {
        this.ticketRepository = ticketRepository;
        this.ticketValidationRestClient = ticketValidationRestClient;
        this.rabbitTemplate = rabbitTemplate;
        this.maxAttempts = maxAttempts;
    }

    @RabbitListener(queues = RabbitConfig.TICKET_VALIDATION_QUEUE_NAME)
    public void receive(TicketValidationMessage message,
                         @Header(name = "x-death", required = false) List<Map<String, Object>> xDeath) {
        long attempt = previousRejections(xDeath) + 1;

        Ticket ticket = ticketRepository.findById(message.ticketId()).orElse(null);
        if (ticket == null || ticket.isDeleted()) {
            log.info("Ticket {} no longer exists or is already deleted, skipping validation", message.ticketId());
            return;
        }

        Organizer organizer = ticket.getEvent().getOrganizer();
        String validationUrl = organizer.getValidationUrl();
        if (validationUrl == null || validationUrl.isBlank()) {
            log.info("Organizer {} has no validation URL configured, leaving ticket {} pending",
                    organizer.getId(), ticket.getId());
            return;
        }

        ValidationRequest request = new ValidationRequest(ticket.getEvent().getId(), ticket.getCode());
        ValidationResponse response;
        try {
            response = ticketValidationRestClient.post()
                    .uri(validationUrl)
                    .body(request)
                    .retrieve()
                    .body(ValidationResponse.class);
        } catch (HttpClientErrorException invalidRequest) {
            log.error("Validation service rejected the request for ticket {} (attempt {}): {}",
                    ticket.getId(), attempt, invalidRequest.getMessage());
            parkAsFailed(message);
            return;
        } catch (RestClientException technicalFailure) {
            if (attempt >= maxAttempts) {
                log.error("Validation of ticket {} failed after {} attempts, giving up: {}",
                        ticket.getId(), attempt, technicalFailure.getMessage());
                parkAsFailed(message);
                return;
            }
            log.warn("Validation call for ticket {} failed (attempt {}/{}), will retry: {}",
                    ticket.getId(), attempt, maxAttempts, technicalFailure.getMessage());
            throw new AmqpRejectAndDontRequeueException("Ticket validation call failed", technicalFailure);
        }

        if (response != null && response.valid()) {
            ticket.setApproved(true);
            ticketRepository.save(ticket);
            log.info("Ticket {} approved by validation service", ticket.getId());
        } else {
            ticket.setDeleted(true);
            ticketRepository.save(ticket);
            log.info("Ticket {} rejected by validation service and marked as deleted", ticket.getId());
        }
    }

    private long previousRejections(List<Map<String, Object>> xDeath) {
        if (xDeath == null) {
            return 0;
        }
        return xDeath.stream()
                .filter(death -> RabbitConfig.TICKET_VALIDATION_QUEUE_NAME.equals(death.get("queue"))
                        && "rejected".equals(death.get("reason")))
                .map(death -> (Number) death.get("count"))
                .filter(Objects::nonNull)
                .mapToLong(Number::longValue)
                .findFirst()
                .orElse(0);
    }

    private void parkAsFailed(TicketValidationMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.TICKET_VALIDATION_FAILED_QUEUE_NAME, message);
    }
}
