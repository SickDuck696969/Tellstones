package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.stoneskin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoneskinRepository extends JpaRepository<stoneskin, Long> {
    List<stoneskin> findByBelong(Account belong);
}
