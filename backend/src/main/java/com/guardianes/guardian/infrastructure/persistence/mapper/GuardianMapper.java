package com.guardianes.guardian.infrastructure.persistence.mapper;

import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.model.GuardianLevel;
import com.guardianes.guardian.infrastructure.persistence.entity.GuardianEntity;
import com.guardianes.guardian.infrastructure.persistence.entity.GuardianLevelEntity;
import org.springframework.stereotype.Component;

@Component
public class GuardianMapper {
    
    public Guardian toDomain(GuardianEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Guardian(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getName(),
            entity.getBirthDate(),
            mapLevelToDomain(entity.getLevel()),
            entity.getExperiencePoints(),
            entity.getTotalSteps(),
            entity.getTotalEnergyGenerated(),
            entity.getCreatedAt(),
            entity.getLastActiveAt(),
            entity.getActive()
        );
    }
    
    public GuardianEntity toEntity(Guardian domain) {
        if (domain == null) {
            return null;
        }
        
        GuardianEntity entity = new GuardianEntity();
        entity.setId(domain.getId());
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setName(domain.getName());
        entity.setBirthDate(domain.getBirthDate());
        entity.setLevel(mapLevelToEntity(domain.getLevel()));
        entity.setExperiencePoints(domain.getExperiencePoints());
        entity.setTotalSteps(domain.getTotalSteps());
        entity.setTotalEnergyGenerated(domain.getTotalEnergyGenerated());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastActiveAt(domain.getLastActiveAt());
        entity.setActive(domain.isActive());
        
        return entity;
    }
    
    private GuardianLevel mapLevelToDomain(GuardianLevelEntity entityLevel) {
        if (entityLevel == null) {
            return GuardianLevel.INITIATE;
        }
        
        return switch (entityLevel) {
            case INITIATE -> GuardianLevel.INITIATE;
            case APPRENTICE -> GuardianLevel.APPRENTICE;
            case PROTECTOR -> GuardianLevel.PROTECTOR;
            case KEEPER -> GuardianLevel.KEEPER;
            case GUARDIAN -> GuardianLevel.GUARDIAN;
            case ELDER -> GuardianLevel.ELDER;
            case SAGE -> GuardianLevel.SAGE;
            case MASTER -> GuardianLevel.MASTER;
            case LEGEND -> GuardianLevel.LEGEND;
            case CHAMPION -> GuardianLevel.CHAMPION;
        };
    }
    
    private GuardianLevelEntity mapLevelToEntity(GuardianLevel domainLevel) {
        if (domainLevel == null) {
            return GuardianLevelEntity.INITIATE;
        }
        
        return switch (domainLevel) {
            case INITIATE -> GuardianLevelEntity.INITIATE;
            case APPRENTICE -> GuardianLevelEntity.APPRENTICE;
            case PROTECTOR -> GuardianLevelEntity.PROTECTOR;
            case KEEPER -> GuardianLevelEntity.KEEPER;
            case GUARDIAN -> GuardianLevelEntity.GUARDIAN;
            case ELDER -> GuardianLevelEntity.ELDER;
            case SAGE -> GuardianLevelEntity.SAGE;
            case MASTER -> GuardianLevelEntity.MASTER;
            case LEGEND -> GuardianLevelEntity.LEGEND;
            case CHAMPION -> GuardianLevelEntity.CHAMPION;
        };
    }
}