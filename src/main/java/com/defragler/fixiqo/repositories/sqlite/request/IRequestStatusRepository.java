package com.defragler.fixiqo.repositories.sqlite.request;

import com.defragler.fixiqo.entities.references.RequestStatus;
import com.defragler.fixiqo.repositories.*;

public interface IRequestStatusRepository extends Repository<RequestStatus, Integer> {
    RequestStatus findByCode(String code);
}
