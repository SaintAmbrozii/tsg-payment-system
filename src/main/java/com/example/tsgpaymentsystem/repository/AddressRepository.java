package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.Account;
import com.example.tsgpaymentsystem.domain.Address;
import com.example.tsgpaymentsystem.domain.Building;
import com.example.tsgpaymentsystem.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends CrudRepository<Address,Long> {
    List<Address> findAllByUser(User owner);

    List<Address> findByUserAndAccount(User user, Account account);

    List<Address> findByUserAndAccountAndApartmentAndBuilding(User user, Account account, String apartment, Building building);
}
