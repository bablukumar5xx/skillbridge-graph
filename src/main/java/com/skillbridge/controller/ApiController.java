package com.skillbridge.controller;

import com.skillbridge.dto.*;
import com.skillbridge.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final GraphService graphService;

    public ApiController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        boolean connected = graphService.isDatabaseConnected();
        return Map.of(
                "status", connected ? "UP" : "DOWN",
                "database", connected ? "connected" : "unavailable"
        );
    }

    @GetMapping("/stats")
    public DashboardStats stats() {
        return graphService.getDashboardStats();
    }

    @GetMapping("/skills")
    public List<SkillDto> skills() {
        return graphService.getAllSkills();
    }

    @GetMapping("/roles")
    public List<RoleDto> roles() {
        return graphService.getAllRoles();
    }

    @GetMapping("/people")
    public List<PersonDto> people() {
        return graphService.getAllPeople();
    }

    @GetMapping("/companies")
    public List<CompanyDto> companies() {
        return graphService.getAllCompanies();
    }

    @GetMapping("/people/{personId}/skills")
    public List<PersonSkill> personSkills(@PathVariable String personId) {
        return graphService.getPersonSkills(personId);
    }

    @GetMapping("/roles/{roleId}/requirements")
    public List<RoleSkillRequirement> roleRequirements(@PathVariable String roleId) {
        return graphService.getRoleRequirements(roleId);
    }

    @GetMapping("/pathfinder/skill-path")
    public ResponseEntity<?> skillPath(
            @RequestParam String from,
            @RequestParam String to) {
        return graphService.findSkillLearningPath(from, to)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of(
                        "steps", List.of(),
                        "totalMonths", 0,
                        "message", "No learning path found between these skills"
                )));
    }

    @GetMapping("/pathfinder/bridge-people")
    public List<BridgePerson> bridgePeople(
            @RequestParam String roleA,
            @RequestParam String roleB) {
        return graphService.findBridgePeople(roleA, roleB);
    }

    @GetMapping("/pathfinder/skill-gap")
    public List<SkillGapItem> skillGap(
            @RequestParam String personId,
            @RequestParam String roleId) {
        return graphService.findSkillGap(personId, roleId);
    }
}
