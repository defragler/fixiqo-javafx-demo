package com.defragler.fixiqo.repositories.sqlite.option;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IOptionRepository extends Repository<Option, Long> {
    List<Option> findByRequestId(long requestId);
}
