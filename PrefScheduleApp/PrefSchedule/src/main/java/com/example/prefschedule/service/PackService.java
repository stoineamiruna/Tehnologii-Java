package com.example.prefschedule.service;

import com.example.prefschedule.entity.Pack;
import com.example.prefschedule.repository.PackRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PackService {

    private final PackRepository packRepository;
    public PackService(PackRepository packRepository) {
        this.packRepository = packRepository;
    }

    public List<Pack> getAll() {
        return packRepository.findAll();
    }

    public Pack save(Pack pack) {
        return packRepository.save(pack);
    }

    public void updateName(Long id, String name) {
        packRepository.updateName(id, name);
    }
}
