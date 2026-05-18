package com.optic.console.config;

import com.optic.console.domain.auth.Role;
import com.optic.console.domain.auth.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findBySlug("owner").isPresent()) {
            return; // Already seeded
        }

        roleRepository.saveAll(List.of(
            Role.builder()
                .name("Owner")
                .slug("owner")
                .description("Full workspace access")
                .scope("workspace")
                .build(),
            Role.builder()
                .name("Member")
                .slug("member")
                .description("Basic workspace access")
                .scope("workspace")
                .build()
        ));
    }
}
