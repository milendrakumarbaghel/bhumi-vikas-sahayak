package org.springboot.backend.repository;

import java.util.List;

import org.springboot.backend.entity.RulePassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RulePassageRepository extends JpaRepository<RulePassage, Long> {

    @Query("""
            select p from RulePassage p
            join fetch p.rule r
            where lower(p.text) like lower(concat('%', :term, '%'))
               or lower(r.title) like lower(concat('%', :term, '%'))
               or lower(r.ruleNumber) = lower(:term)
            order by r.ruleNumber, p.id
            """)
    List<RulePassage> search(@Param("term") String term);
}
