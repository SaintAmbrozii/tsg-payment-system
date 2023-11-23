package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.Building;
import com.example.tsgpaymentsystem.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends CrudRepository<Building,Long> {

    List<Building> findAllByUser(User owner);

    Building findByUserAndBuilding(User user, String building);
}
