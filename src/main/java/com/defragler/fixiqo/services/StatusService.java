package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.request.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.time.*;
import java.util.*;

/**
 * Implementation of {@link IStatusService}.
 *
 * <p>Handles:
 * <ul>
 *     <li>Status lookup</li>
 *     <li>Status transitions</li>
 *     <li>Audit history</li>
 * </ul></p>
 */
public class StatusService implements IStatusService {

    private final IRequestStatusRepository statusRepository;
    private final IRequestStatusHistoryRepository historyRepository;
    private final IRequestRepository requestRepository;

    public StatusService(
          IRequestStatusRepository statusRepository,
          IRequestStatusHistoryRepository historyRepository,
          IRequestRepository requestRepository
    ) {
        this.statusRepository = statusRepository;
        this.historyRepository = historyRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public List<RequestStatus> getAllStatuses() {
        return statusRepository.findAll();
    }

    @Override
    public Optional<RequestStatus> getById(int id) {
        return statusRepository.findById(id);
    }

    @Override
    public RequestStatus getByCode(String code) {
        return statusRepository.findByCode(code);
    }

    @Override
    public void changeStatus(long requestId, int statusId) {

        Request request = requestRepository.findById(requestId)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,
                          "Request with id " + requestId + " not found"));

        request.setStatusId(statusId);

        if (statusId == getIssuedStatusId()) {
            request.setDateIssued(Instant.now().getEpochSecond());
        }

        requestRepository.update(request);

        historyRepository.create(
              new RequestStatusHistory(
                    requestId,
                    statusId,
                    Instant.now().getEpochSecond()
              )
        );
    }

    @Override
    public void recordInitialStatus(long requestId, int statusId) {
        historyRepository.create(
              new RequestStatusHistory(
                    requestId,
                    statusId,
                    Instant.now().getEpochSecond()
              )
        );
    }

    @Override
    public List<RequestStatusHistory> getHistory(long requestId) {
        return historyRepository.findByRequestId(requestId);
    }
    
    @Override
    public RequestStatus getCurrentStatus(long requestId) {
        RequestStatusHistory latest = historyRepository
              .findLatestByRequestId(requestId)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.WARNING,
                          "No status history found for request " + requestId));

        return statusRepository.findById((int) latest.getStatusId())
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,
                          "Status not found for id " + latest.getStatusId()));
    }

    @Override
    public int getAcceptedStatusId() {
        return statusRepository.findByCode("ACCEPTED").getId();
    }

    @Override
    public int getIssuedStatusId() {
        return statusRepository.findByCode("ISSUED").getId();
    }
}