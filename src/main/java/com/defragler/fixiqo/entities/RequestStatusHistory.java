package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.references.*;

/**
 * Represents a single status change event for a repair request.
 *
 * <p>This entity acts as an audit log (event history) for {@link Request},
 * storing every transition between statuses over time.</p>
 *
 * <p>Each record corresponds to one status change.</p>
 */
@Table(name = "RequestStatusHistory")
@Indexes({
      @Index(name = "idx_rsh_request_id", columns = {"request_id"}),
      @Index(name = "idx_rsh_status_id", columns = {"status_id"})
})
public class RequestStatusHistory {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "request_id", nullable = false)
    @ForeignKey(target = Request.class, column = "id", onDeleteCascade = true)
    private long requestId;

    @Column(name = "status_id", nullable = false)
    @ForeignKey(target = RequestStatus.class, column = "id")
    private long statusId;

    @Column(name = "changed_at", nullable = false)
    private long changedAt;

    public RequestStatusHistory() { }

    public RequestStatusHistory(long requestId, long statusId, long changedAt) {
        this.requestId = requestId;
        this.statusId = statusId;
        this.changedAt = changedAt;
    }

    public long getId() { return id; }

    public long getRequestId() { return requestId; }

    public long getStatusId() { return statusId; }
    
    public long getChangedAt() { return changedAt; }
}