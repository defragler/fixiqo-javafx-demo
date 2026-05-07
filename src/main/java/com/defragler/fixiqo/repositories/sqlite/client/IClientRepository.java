package com.defragler.fixiqo.repositories.sqlite.client;

import com.defragler.fixiqo.entities.*;
import com.defragler.fixiqo.repositories.*;
import java.util.*;

public interface IClientRepository extends Repository<Client, Long> {
    List<Client> findByFullName(String fullName);
    Optional<Client> findByPhoneNumber(String phoneNumber);
    Optional<Client> findByFullNameAndPhone(String fullName, String phoneNumber);
}
