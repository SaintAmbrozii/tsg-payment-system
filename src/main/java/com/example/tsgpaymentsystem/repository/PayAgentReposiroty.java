package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.PayAgent;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayAgentReposiroty extends JpaRepository<PayAgent,Long>, JpaSpecificationExecutor<PayAgent> {

    Optional<PayAgent> findByEmail(String email);

}
