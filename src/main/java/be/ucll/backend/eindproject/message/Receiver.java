package be.ucll.backend.eindproject.message;

import be.ucll.backend.eindproject.model.SentTicketNotification;
import be.ucll.backend.eindproject.model.Ticket;
import be.ucll.backend.eindproject.model.TicketAlert;
import be.ucll.backend.eindproject.repository.SentTicketNotificationRepository;
import be.ucll.backend.eindproject.repository.TicketAlertRepository;
import be.ucll.backend.eindproject.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    private final JavaMailSender mailSender;
    private final SentTicketNotificationRepository sentTicketNotificationRepository;
    private final TicketAlertRepository ticketAlertRepository;
    private final TicketRepository ticketRepository;

    public Receiver(JavaMailSender mailSender,
                    SentTicketNotificationRepository sentTicketNotificationRepository,
                    TicketAlertRepository ticketAlertRepository,
                    TicketRepository ticketRepository) {
        this.mailSender = mailSender;
        this.sentTicketNotificationRepository = sentTicketNotificationRepository;
        this.ticketAlertRepository = ticketAlertRepository;
        this.ticketRepository = ticketRepository;
    }

    @RabbitListener(queues = RabbitConfig.MESSAGE_QUEUE_NAME)
    public void receiveMessage(TicketAlertMessage message) {
        log.info("Received ticket alert message: {}", message);

        // Check of mail al verstuurd is
        if (sentTicketNotificationRepository.existsByTicketAlertIdAndTicketId(
                message.ticketAlertId(), message.ticketId())) {
            log.info("Notification already sent for ticketAlertId={} and ticketId={}",
                    message.ticketAlertId(), message.ticketId());
            return;
        }

        // Verstuur email
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(message.recipientEmail());
        email.setSubject("Nieuw ticket beschikbaar: " + message.eventName());
        email.setText(String.format(
                "Er is een nieuw ticket beschikbaar!\n\n" +
                "Evenement: %s\n" +
                "Ticket ID: %d\n" +
                "Prijs: %.2f EUR\n",
                message.eventName(),
                message.ticketId(),
                message.askingPrice()
        ));

        mailSender.send(email);
        log.info("Email sent to {}", message.recipientEmail());

        // Sla op dat mail verstuurd is
        TicketAlert ticketAlert = ticketAlertRepository.findById(message.ticketAlertId())
                .orElse(null);
        Ticket ticket = ticketRepository.findById(message.ticketId())
                .orElse(null);

        if (ticketAlert != null && ticket != null) {
            SentTicketNotification notification = new SentTicketNotification(ticketAlert, ticket);
            sentTicketNotificationRepository.save(notification);
        }
    }
}
