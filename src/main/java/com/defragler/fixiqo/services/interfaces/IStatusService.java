package com.defragler.fixiqo.services.interfaces;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.repositories.sqlite.request.*;

import java.util.*;

/**
 * Service responsible for managing request statuses and their history.
 *
 * <p>This service encapsulates all business logic related to:
 * <ul>
 *     <li>Status lookup (reference data)</li>
 *     <li>Status transitions</li>
 *     <li>Status history tracking (audit log)</li>
 * </ul></p>
 *
 * <p>It should be used instead of directly interacting with
 * {@link IRequestStatusRepository} and {@link IRequestStatusHistoryRepository}.</p>
 */
public interface IStatusService {

    List<RequestStatus> getAllStatuses();

    Optional<RequestStatus> getById(int id);

    RequestStatus getByCode(String code);

    /**
     * Changes status of a request and records history entry.
     */
    void changeStatus(long requestId, int statusId);

    /**
     * Changes initial status of request and records history entry.
     */
    void recordInitialStatus(long requestId, int statusId);

    /**
     * Returns full status history of request.
     */
    List<RequestStatusHistory> getHistory(long requestId);
    
    /**
     * Returns current (latest) status of request.
     *
     * <p>This is a convenience method for UI usage,
     * avoiding manual history lookup.</p>
     *
     * @param requestId request identifier
     * @return current {@link RequestStatus}
     */
    RequestStatus getCurrentStatus(long requestId);

    /**
     * Returns ID of ACCEPTED status.
     */
    int getAcceptedStatusId();

    /**
     * Returns ID of ISSUED status.
     */
    int getIssuedStatusId();
}