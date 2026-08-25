package be.ucll.backend.eindproject.dto;

public class TicketRequest {
    private String code;
    private Long eventId;
    private Long ownerId;
    private float askingPrice;

    public TicketRequest() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public float getAskingPrice() {
        return askingPrice;
    }

    public void setAskingPrice(float askingPrice) {
        this.askingPrice = askingPrice;
    }
}
