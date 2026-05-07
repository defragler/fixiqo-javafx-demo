package com.defragler.fixiqo.repositories.sqlite.request;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IRequestRepository extends Repository<Request, Long> {
    List<Request> findByClientId(long clientId);
    List<Request> findByDeviceId(long deviceId);
    List<Request> findByStatusId(long statusId);
}
