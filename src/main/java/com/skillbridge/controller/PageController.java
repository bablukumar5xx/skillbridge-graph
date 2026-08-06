package com.skillbridge.controller;

import com.skillbridge.service.GraphService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final GraphService graphService;

    public PageController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("connected", graphService.isDatabaseConnected());
        model.addAttribute("stats", graphService.getDashboardStats());
        return "index";
    }

    @GetMapping("/skills")
    public String skills(Model model) {
        model.addAttribute("skills", graphService.getAllSkills());
        return "skills";
    }

    @GetMapping("/roles")
    public String roles(Model model) {
        model.addAttribute("roles", graphService.getAllRoles());
        return "roles";
    }

    @GetMapping("/people")
    public String people(Model model) {
        model.addAttribute("people", graphService.getAllPeople());
        return "people";
    }

    @GetMapping("/people/{personId}")
    public String personDetail(@PathVariable String personId, Model model) {
        return graphService.getPerson(personId)
                .map(person -> {
                    model.addAttribute("person", person);
                    model.addAttribute("skills", graphService.getPersonSkills(personId));
                    model.addAttribute("roles", graphService.getAllRoles());
                    return "person-detail";
                })
                .orElse("redirect:/people");
    }

    @GetMapping("/companies")
    public String companies(Model model) {
        model.addAttribute("companies", graphService.getAllCompanies());
        return "companies";
    }

    @GetMapping("/pathfinder")
    public String pathfinder(@RequestParam(required = false) String roleId, Model model) {
        model.addAttribute("skills", graphService.getAllSkills());
        model.addAttribute("roles", graphService.getAllRoles());
        model.addAttribute("people", graphService.getAllPeople());
        model.addAttribute("preselectedRole", roleId);
        return "pathfinder";
    }
}
