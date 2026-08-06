package com.skillbridge.repository;

import com.skillbridge.dto.*;
import com.skillbridge.exception.DatabaseUnavailableException;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class GraphRepository {

    private final Driver driver;

    public GraphRepository(Driver driver) {
        this.driver = driver;
    }

    public boolean isConnected() {
        try {
            driver.verifyConnectivity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public DashboardStats getDashboardStats() {
        return execute(session -> {
            Record record = session.run("""
                    MATCH (s:Skill)
                    WITH count(s) AS skillCount
                    MATCH (r:Role)
                    WITH skillCount, count(r) AS roleCount
                    MATCH (p:Person)
                    WITH skillCount, roleCount, count(p) AS personCount
                    MATCH (c:Company)
                    WITH skillCount, roleCount, personCount, count(c) AS companyCount
                    MATCH ()-[rel]->()
                    RETURN skillCount, roleCount, personCount, companyCount, count(rel) AS relationshipCount
                    """).single();

            return new DashboardStats(
                    record.get("skillCount").asLong(),
                    record.get("roleCount").asLong(),
                    record.get("personCount").asLong(),
                    record.get("companyCount").asLong(),
                    record.get("relationshipCount").asLong()
            );
        });
    }

    public List<SkillDto> findAllSkills() {
        return execute(session -> session.run("""
                MATCH (s:Skill)
                RETURN s.id AS id, s.name AS name, s.category AS category, s.difficulty AS difficulty
                ORDER BY s.category, s.name
                """).list(this::mapSkill));
    }

    public List<RoleDto> findAllRoles() {
        return execute(session -> session.run("""
                MATCH (r:Role)
                RETURN r.id AS id, r.title AS title, r.level AS level, r.domain AS domain
                ORDER BY r.domain, r.title
                """).list(this::mapRole));
    }

    public List<PersonDto> findAllPeople() {
        return execute(session -> session.run("""
                MATCH (p:Person)
                RETURN p.id AS id, p.name AS name, p.bio AS bio, p.yearsExperience AS yearsExperience
                ORDER BY p.name
                """).list(this::mapPerson));
    }

    public List<CompanyDto> findAllCompanies() {
        return execute(session -> session.run("""
                MATCH (c:Company)
                RETURN c.id AS id, c.name AS name, c.industry AS industry, c.location AS location
                ORDER BY c.name
                """).list(this::mapCompany));
    }

    public Optional<PersonDto> findPersonById(String personId) {
        return execute(session -> session.run("""
                MATCH (p:Person {id: $personId})
                RETURN p.id AS id, p.name AS name, p.bio AS bio, p.yearsExperience AS yearsExperience
                """, Map.of("personId", personId))
                .stream()
                .map(this::mapPerson)
                .findFirst());
    }

    public List<PersonSkill> findPersonSkills(String personId) {
        return execute(session -> session.run("""
                MATCH (p:Person {id: $personId})-[hs:HAS_SKILL]->(s:Skill)
                RETURN s.id AS skillId, s.name AS skillName, s.category AS category,
                       hs.proficiency AS proficiency, hs.years AS years
                ORDER BY s.category, s.name
                """, Map.of("personId", personId)).list(record -> new PersonSkill(
                record.get("skillId").asString(),
                record.get("skillName").asString(),
                record.get("category").asString(),
                record.get("proficiency").asString(),
                record.get("years").asInt()
        )));
    }

    public List<RoleSkillRequirement> findRoleRequirements(String roleId) {
        return execute(session -> session.run("""
                MATCH (r:Role {id: $roleId})-[req:REQUIRES]->(s:Skill)
                RETURN s.id AS skillId, s.name AS skillName, s.category AS category,
                       req.proficiency AS proficiency
                ORDER BY s.category, s.name
                """, Map.of("roleId", roleId)).list(record -> new RoleSkillRequirement(
                record.get("skillId").asString(),
                record.get("skillName").asString(),
                record.get("category").asString(),
                record.get("proficiency").asString()
        )));
    }

    /**
     * Multi-hop traversal: shortest learning path between two skills via PREREQUISITE_FOR (2+ hops).
     */
    public Optional<SkillPathResult> findSkillLearningPath(String fromSkillId, String toSkillId) {
        return execute(session -> {
            List<Record> records = session.run("""
                    MATCH path = shortestPath(
                        (start:Skill {id: $fromSkillId})-[:PREREQUISITE_FOR*..8]->(end:Skill {id: $toSkillId})
                    )
                    WITH nodes(path) AS skillNodes, relationships(path) AS rels
                    UNWIND range(0, size(skillNodes) - 1) AS idx
                    WITH idx, skillNodes[idx] AS skill,
                         CASE WHEN idx = 0 THEN 0 ELSE rels[idx - 1].monthsToLearn END AS months
                    RETURN idx + 1 AS step, skill.id AS skillId, skill.name AS skillName,
                           skill.category AS category, months AS monthsToLearn
                    ORDER BY step
                    """, Map.of("fromSkillId", fromSkillId, "toSkillId", toSkillId)).list();

            if (records.isEmpty()) {
                return Optional.empty();
            }

            List<SkillPathStep> steps = records.stream()
                    .map(record -> new SkillPathStep(
                            record.get("step").asInt(),
                            record.get("skillId").asString(),
                            record.get("skillName").asString(),
                            record.get("category").asString(),
                            record.get("monthsToLearn").asInt()
                    ))
                    .toList();
            int totalMonths = steps.stream().mapToInt(SkillPathStep::monthsToLearn).sum();
            return Optional.of(new SkillPathResult(steps, totalMonths));
        });
    }

    /**
     * Relational-awkward query: find people who bridge two roles through shared skill networks (3+ hops).
     *
     * <p>The pattern walk (roleA -REQUIRES-> skill <-HAS_SKILL- person -HAS_SKILL-> skill <-REQUIRES- roleB)
     * is expressed as a single Cypher traversal; the "not already in either role" exclusion is applied in
     * Java because CognoDB currently mis-evaluates {@code NOT (person)-[:WORKS_AS]->(role)} predicates.</p>
     */
    public List<BridgePerson> findBridgePeopleBetweenRoles(String roleAId, String roleBId) {
        List<BridgeRow> rows = execute(session -> session.run("""
                MATCH (roleA:Role {id: $roleAId})-[:REQUIRES]->(skillA:Skill)<-[:HAS_SKILL]-(person:Person)
                      -[:HAS_SKILL]->(skillB:Skill)<-[:REQUIRES]-(roleB:Role {id: $roleBId})
                WHERE skillA <> skillB
                RETURN DISTINCT person.id AS personId, person.name AS personName,
                       skillA.name AS skillAName, skillB.name AS skillBName,
                       roleA.title AS roleATitle, roleB.title AS roleBTitle
                """, Map.of("roleAId", roleAId, "roleBId", roleBId))
                .list(record -> new BridgeRow(
                        record.get("personId").asString(),
                        record.get("personName").asString(),
                        record.get("skillAName").asString(),
                        record.get("skillBName").asString(),
                        record.get("roleATitle").asString(),
                        record.get("roleBTitle").asString()
                )));

        Set<String> excluded = new HashSet<>();
        excluded.addAll(peopleInRole(roleAId));
        excluded.addAll(peopleInRole(roleBId));

        LinkedHashMap<String, BridgePerson> grouped = new LinkedHashMap<>();
        for (BridgeRow row : rows) {
            if (excluded.contains(row.personId)) {
                continue;
            }
            BridgePerson existing = grouped.get(row.personId);
            String connection = row.skillAName.compareTo(row.skillBName) < 0
                    ? row.skillAName + " + " + row.skillBName
                    : row.skillBName + " + " + row.skillAName;
            if (existing == null) {
                grouped.put(row.personId, new BridgePerson(
                        row.personId, row.personName, connection, row.roleATitle, row.roleBTitle));
            } else {
                grouped.put(row.personId, new BridgePerson(
                        existing.personId(), existing.personName(),
                        existing.connectingSkill() + "; " + connection,
                        existing.roleATitle(), existing.roleBTitle()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    private Set<String> peopleInRole(String roleId) {
        return execute(session -> session.run("""
                MATCH (p:Person)-[:WORKS_AS]->(r:Role {id: $roleId})
                RETURN p.id AS personId
                """, Map.of("roleId", roleId))
                .list(record -> record.get("personId").asString()))
                .stream().collect(Collectors.toSet());
    }

    /**
     * Skill gap analysis: skills required for a role that a person lacks or is under-qualified for.
     *
     * <p>The role requirements and the person's proficiency profile are fetched as two simple,
     * single-pattern queries and joined in Java. CognoDB currently drops node filters inside
     * {@code OPTIONAL MATCH} clauses, so the join cannot be done reliably in a single Cypher query.</p>
     */
    public List<SkillGapItem> findSkillGap(String personId, String roleId) {
        List<RoleSkillRequirement> requirements = findRoleRequirements(roleId);

        Map<String, String> personProficiency = execute(session -> session.run("""
                MATCH (p:Person {id: $personId})-[hs:HAS_SKILL]->(s:Skill)
                RETURN s.id AS skillId, hs.proficiency AS proficiency
                """, Map.of("personId", personId))
                .list(record -> Map.entry(
                        record.get("skillId").asString(),
                        record.get("proficiency").asString())))
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<SkillGapItem> gaps = new ArrayList<>();
        for (RoleSkillRequirement req : requirements) {
            String owned = personProficiency.get(req.skillId());
            if (owned == null) {
                gaps.add(new SkillGapItem(req.skillId(), req.skillName(), req.category(), req.proficiency()));
            } else if (proficiencyLevel(owned) < proficiencyLevel(req.proficiency())) {
                gaps.add(new SkillGapItem(req.skillId(), req.skillName(), req.category(), req.proficiency()));
            }
        }
        return gaps;
    }

    private int proficiencyLevel(String proficiency) {
        return switch (proficiency) {
            case "advanced" -> 3;
            case "intermediate" -> 2;
            default -> 1;
        };
    }

    public void clearDatabase() {
        executeVoid(session -> session.run("MATCH (n) DETACH DELETE n").consume());
    }

    public boolean hasData() {
        return execute(session -> session.run("MATCH (n) RETURN count(n) AS c").single().get("c").asLong() > 0);
    }

    public void runSeedStatements(List<String> statements) {
        executeVoid(session -> {
            for (String statement : statements) {
                session.run(statement).consume();
            }
        });
    }

    private SkillDto mapSkill(Record record) {
        return new SkillDto(
                record.get("id").asString(),
                record.get("name").asString(),
                record.get("category").asString(),
                record.get("difficulty").asInt()
        );
    }

    private RoleDto mapRole(Record record) {
        return new RoleDto(
                record.get("id").asString(),
                record.get("title").asString(),
                record.get("level").asString(),
                record.get("domain").asString()
        );
    }

    private PersonDto mapPerson(Record record) {
        return new PersonDto(
                record.get("id").asString(),
                record.get("name").asString(),
                record.get("bio").asString(),
                record.get("yearsExperience").asInt()
        );
    }

    private CompanyDto mapCompany(Record record) {
        return new CompanyDto(
                record.get("id").asString(),
                record.get("name").asString(),
                record.get("industry").asString(),
                record.get("location").asString()
        );
    }

    private <T> T execute(QueryCallback<T> callback) {
        try (Session session = driver.session()) {
            return callback.run(session);
        } catch (ServiceUnavailableException ex) {
            throw new DatabaseUnavailableException("CognoDB is unreachable. Verify COGNODB_URI and credentials.", ex);
        } catch (Neo4jException ex) {
            throw new DatabaseUnavailableException("Database query failed: " + ex.getMessage(), ex);
        }
    }

    private void executeVoid(VoidQueryCallback callback) {
        try (Session session = driver.session()) {
            callback.run(session);
        } catch (ServiceUnavailableException ex) {
            throw new DatabaseUnavailableException("CognoDB is unreachable. Verify COGNODB_URI and credentials.", ex);
        } catch (Neo4jException ex) {
            throw new DatabaseUnavailableException("Database query failed: " + ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    private interface QueryCallback<T> {
        T run(Session session);
    }

    private record BridgeRow(String personId, String personName, String skillAName,
                             String skillBName, String roleATitle, String roleBTitle) {
    }

    @FunctionalInterface
    private interface VoidQueryCallback {
        void run(Session session);
    }
}
