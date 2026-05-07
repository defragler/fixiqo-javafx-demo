package com.defragler.fixiqo.repositories.sqlite.accessories;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;

import java.util.*;

public interface IAccessoriesRepository extends Repository<Accessories, Long> {

    List<Accessories> findByRequestId(long requestId);

    List<Accessories> findByAccessoriesTypeId(long accessoriesTypeId);

    void deleteByRequestId(long requestId);
}