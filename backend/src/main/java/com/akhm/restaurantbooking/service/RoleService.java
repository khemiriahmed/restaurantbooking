package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Role;
import com.akhm.restaurantbooking.enums.RoleName;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {

        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: " + id
                        )
                );
    }

    public Role findByName(RoleName name) {

        return roleRepository.findByName(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + name
                        )
                );
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public void delete(Long id) {

        Role role = findById(id);

        roleRepository.delete(role);
    }
}