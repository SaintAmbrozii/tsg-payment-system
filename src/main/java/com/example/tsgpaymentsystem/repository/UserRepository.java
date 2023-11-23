package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.User;
import org.hibernate.query.criteria.JpaSelectCriteria;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User>
{
    Optional<User>findByEmail(String email);
    Optional<User>findByName(String name);
}