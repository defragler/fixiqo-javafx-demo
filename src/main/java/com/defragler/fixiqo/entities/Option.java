package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;

/**
 * Represents an additional service or option linked to a repair request.
 *
 * <p>Examples: "Replace screen protector", "Express repair", "Software update".</p>
 *
 * <p>Each option belongs to a specific repair request and has a description and price.</p>
 */
@Table(name = "Options")
@Indexes({
      @Index(name = "idx_options_request_id", columns = {"request_id"})
})
public class Option {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "request_id", nullable = false)
    @ForeignKey(target = Request.class, column = "id", onDeleteCascade = true)
    private long requestId;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private double price;

    public Option() { }

    public Option(long requestId, String description, double price) {
        this.requestId = requestId;
        this.description = description;
        this.price = price;
    }

    /**
     * Returns the unique identifier of this option.
     *
     * @return option ID (primary key)
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the repair request ID this option is associated with.
     *
     * @return request ID (foreign key)
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * Returns the textual description of the option/service.
     *
     * @return description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets or updates the description of the option.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the price of this option.
     *
     * @return price value
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets or updates the price of this option.
     *
     * @param price new price value
     */
    public void setPrice(double price) {
        this.price = price;
    }
}