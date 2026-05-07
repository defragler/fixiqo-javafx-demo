package com.defragler.fixiqo.repositories.sqlite.user;

import com.defragler.fixiqo.entities.references.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IUserRoleRepository extends Repository<UserRole, Integer> {
    Optional<UserRole> findByCode(String code);
}
