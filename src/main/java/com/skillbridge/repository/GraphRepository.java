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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        } catch (ServiceUnavailableException ex) {
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
     */
    public List<BridgePerson> findBridgePeopleBetweenRoles(String roleAId, String roleBId) {
        return execute(session -> session.run("""
                MATCH (roleA:Role {id: $roleAId})-[:REQUIRES]->(skillA:Skill)<-[:HAS_SKILL]-(person:Person)
                      -[:HAS_SKILL]->(skillB:Skill)<-[:REQUIRES]-(roleB:Role {id: $roleBId})
                WHERE skillA <> skillB
                  AND NOT (person)-[:WORKS_AS]->(roleA)
                  AND NOT (person)-[:WORKS_AS]->(roleB)
                WITH person, skillA, skillB, roleA, roleB,
                     CASE WHEN skillA.name < skillB.name
                          THEN skillA.name + ' ? ' + skillB.name
                          ELSE skillB.name + ' ? ' + skillA.name END AS connection
                RETURN DISTINCT person.id AS personId, person.name AS personName,
                       connection AS connectingSkill,
                       roleA.title AS roleATitle, roleB.title AS roleBTitle
                ORDER BY personName
                LIMIT 20
                """, Map.of("roleAId", roleAId, "roleBId", roleBId))
                .list(record -> new BridgePerson(
                        record.get("personId").asString(),
                        record.get("personName").asString(),
                        record.get("connectingSkill").asString(),
                        record.get("roleATitle").asString(),
                        record.get("roleBTitle").asString()
                )));
    }

    /**
     * Skill gap analysis: skills required for a role that a person lacks or is under-qualified for.
     */
    public List<SkillGapItem> findSkillGap(String personId, String roleId) {
        return execute(session -> session.run("""
                MATCH (r:Role {id: $roleId})-[req:REQUIRES]->(s:Skill)
                OPTIONAL MATCH (p:Person {id: $personId})-[hs:HAS_SKILL]->(s)
                WITH s, req, hs,
                     CASE
                         WHEN hs IS NULL THEN 'missing'
                         WHEN hs.proficiency = 'beginner' AND req.proficiency IN ['intermediate', 'advanced'] THEN 'underqualified'
                         WHEN hs.proficiency = 'intermediate' AND req.proficiency = 'advanced' THEN 'underqualified'
                         ELSE 'met'
                     END AS status
                WHERE status <> 'met'
                RETURN s.id AS skillId, s.name AS skillName, s.category AS category,
                       req.proficiency AS requiredProficiency
                ORDER BY s.category, s.name
                """, Map.of("personId", personId, "roleId", roleId))
                .list(record -> new SkillGapItem(
                        record.get("skillId").asString(),
                        record.get("skillName").asString(),
                        record.get("category").asString(),
                        record.get("requiredProficiency").asString()
                )));
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

    @FunctionalInterface
    private interface VoidQueryCallback {
        void run(Session session);
    }
}
