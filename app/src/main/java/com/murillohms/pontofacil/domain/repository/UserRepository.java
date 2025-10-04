package com.murillohms.pontofacil.domain.repository;

import com.murillohms.pontofacil.domain.model.User;
import java.util.List;

public interface UserRepository {

    void save(User user);
    User findById(String id);
    List<User> findAll();
}
