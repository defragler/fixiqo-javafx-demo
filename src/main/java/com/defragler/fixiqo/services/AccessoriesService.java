package com.defragler.fixiqo.services;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.exceptions.*;
import com.defragler.fixiqo.exceptions.enums.*;
import com.defragler.fixiqo.repositories.sqlite.accessories.*;
import com.defragler.fixiqo.repositories.sqlite.request.*;
import com.defragler.fixiqo.services.interfaces.*;

import java.util.*;

/**
 * Implementation of {@link IAccessoriesService}.
 *
 * <p>Handles business logic for assigning accessories to repair requests.</p>
 */
public class AccessoriesService implements IAccessoriesService {

    private final IAccessoriesRepository accessoriesRepository;
    private final IRequestRepository requestRepository;

    public AccessoriesService(
          IAccessoriesRepository accessoriesRepository,
          IRequestRepository requestRepository
    ) {
        this.accessoriesRepository = accessoriesRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public List<Accessories> getByRequestId(long requestId) {
        return accessoriesRepository.findByRequestId(requestId);
    }

    @Override
    public void addAccessories(long requestId, List<Integer> accessoriesTypeIds) {

        validateRequest(requestId);

        for (Integer typeId : accessoriesTypeIds) {
            accessoriesRepository.create(
                  new Accessories(requestId, typeId)
            );
        }
    }

    @Override
    public void replaceAccessories(long requestId, List<Integer> accessoriesTypeIds) {

        validateRequest(requestId);

        accessoriesRepository.deleteByRequestId(requestId);

        addAccessories(requestId, accessoriesTypeIds);
    }

    @Override
    public void removeAll(long requestId) {
        accessoriesRepository.deleteByRequestId(requestId);
    }

    private void validateRequest(long requestId) {
        requestRepository.findById(requestId)
              .orElseThrow(() ->
                    new ServiceException(ExceptionLevel.ERROR,
                          "Request with id " + requestId + " not found"));
    }
}