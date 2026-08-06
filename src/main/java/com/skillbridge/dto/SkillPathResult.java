package com.skillbridge.dto;

import java.util.List;

public record SkillPathResult(List<SkillPathStep> steps, int totalMonths) {}
