package be.ucll.backend.eindproject.model;
import jakarta.persistence.*;

@Entity
@Table(name = "TICKET", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "code"})
})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "is_approved", nullable = false)
    private boolean isApproved;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "is_for_sale", nullable = false)
    private boolean isForSale;

    @Column(name = "asking_price")
    private float askingPrice;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Ticket() { }
    public Ticket(String code, Event event, boolean isApproved, boolean isDeleted, boolean isForSale, float askingPrice, User owner) {
        this.code = code;
        this.event = event;
        this.isApproved = isApproved;
        this.isDeleted = isDeleted;
        this.isForSale = isForSale;
        this.askingPrice = askingPrice;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public float getAskingPrice() {
        return askingPrice;
    }

    public void setAskingPrice(float askingPrice) {
        this.askingPrice = askingPrice;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isForSale() {
        return isForSale;
    }

    public void setForSale(boolean forSale) {
        isForSale = forSale;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setId(long id) {
        this.id = id;
    }
}