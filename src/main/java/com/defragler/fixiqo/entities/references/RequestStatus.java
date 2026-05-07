package com.defragler.fixiqo.entities.references;

import com.defragler.fixiqo.annotaions.*;
import com.defragler.fixiqo.entities.enums.*;

/**
 * Represents a request status in the system.
 *
 * <p>Reference table storing request status identifiers and codes.</p>
 */
@ReferenceTable(values = RequestStatusEnum.class)
@Table(name = "RequestStatuses")
@Indexes({
      @Index(name = "idx_request_statuses_code", columns = {"code"}, unique = true)
})
public class RequestStatus {
    @Id(autoIncrement = false)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    public RequestStatus() { }

    public RequestStatus(int id, String code) {
        this.id = id;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}