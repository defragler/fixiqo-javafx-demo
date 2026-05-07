package com.defragler.fixiqo.repositories.sqlite.part;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IPartRepository extends Repository<Part, Long> {
    List<Part> findByRequestId(long requestId);
}
