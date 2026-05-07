package com.defragler.fixiqo.repositories.sqlite.user;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IUserRepository extends Repository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
