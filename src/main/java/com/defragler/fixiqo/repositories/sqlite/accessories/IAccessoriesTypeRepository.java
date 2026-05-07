package com.defragler.fixiqo.repositories.sqlite.accessories;

import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.repositories.*;

public interface IAccessoriesTypeRepository extends Repository<AccessoriesType, Integer> {

    AccessoriesType findByCode(String code);
}