package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.references.DeviceType;
import com.defragler.fixiqo.entities.references.RequestStatus;

/**
 * Represents a repair request in the Fixiqo service center system.
 *
 * <p>This entity stores information about the device, client, current status, and timestamps.</p>
 */
@Table(name = "Requests")
@Indexes({
      @Index(name = "idx_requests_client_id", columns = {"client_id"}),
      @Index(name = "idx_requests_device_id", columns = {"device_id"}),
      @Index(name = "idx_requests_status_id", columns = {"status_id"})
})
public class Request {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "client_id", nullable = false)
    @ForeignKey(target = Client.class, column = "id")
    private long clientId;

    @Column(name = "device_id", nullable = false)
    @ForeignKey(target = Device.class, column = "id")
    private long deviceId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status_id", nullable = false)
    @ForeignKey(target = RequestStatus.class, column = "id")
    private int statusId;

    @Column(name = "date_received", nullable = false)
    private long dateReceived;

    @Column(name = "date_issued")
    private Long dateIssued;

    public Request() { }

    public Request(long clientId, long deviceId, String description, int statusId, long dateReceived, Long dateIssued) {
        this.clientId = clientId;
        this.deviceId = deviceId;
        this.description = description;
        this.statusId = statusId;
        this.dateReceived = dateReceived;
        this.dateIssued = dateIssued;
    }

    public long getId() { return id; }
    
    public long getClientId() { return clientId; }
    
    public long getDeviceId() { return deviceId; }
    public void setDeviceId(long deviceId) { this.deviceId = deviceId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public long getDateReceived() { return dateReceived; }
    public void setDateReceived(long dateReceived) { this.dateReceived = dateReceived; }

    public Long getDateIssued() { return dateIssued; }
    public void setDateIssued(Long dateIssued) { this.dateIssued = dateIssued; }
}