package org.springboot.backend.repository;

import java.util.Optional;

import org.springboot.backend.entity.BuildingRule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRuleRepository extends JpaRepository<BuildingRule, Long> {

    @EntityGraph(attributePaths = "passages")
    Optional<BuildingRule> findByRuleNumberIgnoreCase(String ruleNumber);
}
