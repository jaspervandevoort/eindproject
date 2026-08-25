package be.ucll.backend.eindproject.message;

/**
 * Request body expected by the external ticket validation service
 * (ghcr.io/ucll-backend2-2526/ticketvalidator), POSTed to {@code /api/v1/validations}.
 */
public record ValidationRequest(Long eventId, String ticketCode) {}
