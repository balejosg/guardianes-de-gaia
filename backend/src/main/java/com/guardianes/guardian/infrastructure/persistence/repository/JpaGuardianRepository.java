package com.guardianes.guardian.infrastructure.persistence.repository;

import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.model.GuardianLevel;
import com.guardianes.guardian.domain.repository.GuardianRepository;
import com.guardianes.guardian.infrastructure.persistence.entity.GuardianEntity;
import com.guardianes.guardian.infrastructure.persistence.mapper.GuardianMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaGuardianRepository implements GuardianRepository {
    
    private final GuardianJpaRepository jpaRepository;
    private final GuardianMapper mapper;
    
    public JpaGuardianRepository(GuardianJpaRepository jpaRepository, GuardianMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Guardian save(Guardian guardian) {
        GuardianEntity entity = mapper.toEntity(guardian);
        GuardianEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Guardian> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Guardian> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Guardian> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
    
    @Override
    public List<Guardian> findByActiveTrue() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Guardian> findByActiveFalse() {
        return jpaRepository.findByActiveFalse()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Guardian> findByLevel(GuardianLevel level) {
        return jpaRepository.findByLevel(level)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Guardian> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Guardian> findByLastActiveAtAfter(LocalDateTime cutoff) {
        return jpaRepository.findByLastActiveAtAfter(cutoff)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}