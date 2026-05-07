package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.references.*;

/**
 * Represents an accessory assigned to a repair request.
 *
 * <p>This entity defines a many-to-one relationship between
 * {@link Request} and {@link AccessoriesType}, allowing tracking
 * of additional items provided with a device (e.g. charger, case).</p>
 */
@Table(name = "Accessories")
@Indexes({
      @Index(name = "idx_accessories_request_id", columns = {"request_id"}),
      @Index(name = "idx_accessories_accessory_id", columns = {"accessories_id"})
})
public class Accessories {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "request_id", nullable = false)
    @ForeignKey(target = Request.class, column = "id", onDeleteCascade = true)
    private long requestId;

    @Column(name = "accessories_id", nullable = false)
    @ForeignKey(target = AccessoriesType.class, column = "id")
    private long accessoriesId;

    public Accessories() { }

    public Accessories(long requestId, long accessoriesId) {
        this.requestId = requestId;
        this.accessoriesId = accessoriesId;
    }

    public long getId() { return id; }

    public long getRequestId() { return requestId; }

    public long getAccessoriesId() { return accessoriesId; }
}
