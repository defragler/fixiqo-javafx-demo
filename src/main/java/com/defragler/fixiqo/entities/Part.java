package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;

/**
 * Represents a spare part or material used in a repair request.
 *
 * <p>Examples: "Battery", "Screen replacement", "Charging port".</p>
 *
 * <p>Each part is linked to a specific repair request and has a description and price.</p>
 */
@Table(name = "Parts")
@Indexes({
      @Index(name = "idx_parts_request_id", columns = {"request_id"})
})
public class Part {
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

    public Part() { }

    public Part(long requestId, String description, double price) {
        this.requestId = requestId;
        this.description = description;
        this.price = price;
    }

    /**
     * Returns the unique identifier of the part.
     *
     * @return part ID (primary key)
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the repair request ID this part is associated with.
     *
     * @return request ID (foreign key)
     */
    public long getRequestId() {
        return requestId;
    }

    /**
     * Returns the textual description of the part.
     *
     * @return description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets or updates the description of the part.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the price of the part.
     *
     * @return price value
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets or updates the price of the part.
     *
     * @param price new price
     */
    public void setPrice(double price) {
        this.price = price;
    }
}