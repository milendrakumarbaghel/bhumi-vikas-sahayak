package org.springboot.backend.repository;

import java.util.List;

import org.springboot.backend.entity.RulePassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RulePassageRepository extends JpaRepository<RulePassage, Long> {

    @Query(value = """
            select p.id from rule_passage p join building_rule r on r.id=p.rule_id
            where p.search_vector @@ websearch_to_tsquery('english',:query)
            order by ts_rank_cd(p.search_vector,websearch_to_tsquery('english',:query)) desc,p.id
            limit 40
            """, nativeQuery = true)
    List<Long> rankedSearch(@Param("query") String query);

    @Query("select p from RulePassage p join fetch p.rule where p.id in :ids")
    List<RulePassage> fetchRanked(@Param("ids") List<Long> ids);

    @Query("select p from RulePassage p join fetch p.rule r where lower(r.ruleNumber)=lower(:rule)")
    List<RulePassage> forRule(@Param("rule") String rule);

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
