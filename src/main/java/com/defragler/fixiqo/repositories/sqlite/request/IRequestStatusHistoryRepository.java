package com.defragler.fixiqo.repositories.sqlite.request;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IRequestStatusHistoryRepository extends Repository<RequestStatusHistory, Long> {

    List<RequestStatusHistory> findByRequestId(long requestId);
    
    Optional<RequestStatusHistory> findLatestByRequestId(long requestId);
}
