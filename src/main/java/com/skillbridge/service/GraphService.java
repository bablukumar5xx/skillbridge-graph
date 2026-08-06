package com.skillbridge.service;

import com.skillbridge.dto.*;
import com.skillbridge.repository.GraphRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GraphService {

    private final GraphRepository repository;

    public GraphService(GraphRepository repository) {
        this.repository = repository;
    }

    public boolean isDatabaseConnected() {
        return repository.isConnected();
    }

    public DashboardStats getDashboardStats() {
        return repository.getDashboardStats();
    }

    public List<SkillDto> getAllSkills() {
        return repository.findAllSkills();
    }

    public List<RoleDto> getAllRoles() {
        return repository.findAllRoles();
    }

    public List<PersonDto> getAllPeople() {
        return repository.findAllPeople();
    }

    public List<CompanyDto> getAllCompanies() {
        return repository.findAllCompanies();
    }

    public Optional<PersonDto> getPerson(String personId) {
        return repository.findPersonById(personId);
    }

    public List<PersonSkill> getPersonSkills(String personId) {
        return repository.findPersonSkills(personId);
    }

    public List<RoleSkillRequirement> getRoleRequirements(String roleId) {
        return repository.findRoleRequirements(roleId);
    }

    public Optional<SkillPathResult> findSkillLearningPath(String fromSkillId, String toSkillId) {
        if (fromSkillId.equals(toSkillId)) {
            throw new IllegalArgumentException("Source and target skills must be different");
        }
        return repository.findSkillLearningPath(fromSkillId, toSkillId);
    }

    public List<BridgePerson> findBridgePeople(String roleAId, String roleBId) {
        if (roleAId.equals(roleBId)) {
            throw new IllegalArgumentException("Select two different roles");
        }
        return repository.findBridgePeopleBetweenRoles(roleAId, roleBId);
    }

    public List<SkillGapItem> findSkillGap(String personId, String roleId) {
        return repository.findSkillGap(personId, roleId);
    }
}
