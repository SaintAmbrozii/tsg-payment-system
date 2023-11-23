package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.Service;
import com.example.tsgpaymentsystem.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends CrudRepository<Service,Long> {

    List<Service> findAllByUser(User user);

    Service findByUserAndService(User user, String service);

    List<Service> findAllByUserOrderByService(User user);
}

