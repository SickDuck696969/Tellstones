package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.stoneskin;
import com.example.demo.repository.StoneskinRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class stoneskinService {

    private final StoneskinRepository stoneskinRepository;

    public stoneskinService(StoneskinRepository stoneskinRepository) {
        this.stoneskinRepository = stoneskinRepository;
    }

    public List<stoneskin> findAll() {
        return stoneskinRepository.findAll();
    }

    public Optional<stoneskin> findById(Long id) {
        return stoneskinRepository.findById(id);
    }

    public List<stoneskin> findByBelong(Account belong) {
        return stoneskinRepository.findByBelong(belong);
    }

    public stoneskin save(stoneskin skin) {
        return stoneskinRepository.save(skin);
    }

    public void deleteById(Long id) {
        stoneskinRepository.deleteById(id);
    }
}

