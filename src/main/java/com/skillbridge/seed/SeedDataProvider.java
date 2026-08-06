package com.skillbridge.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the seed graph from {@code classpath:seed-data.cypher}.
 *
 * <p>The Cypher file is the single source of truth for the demo data set. Each
 * statement is terminated by {@code ;} and executed individually through the
 * official Neo4j driver. Comments and blank lines are ignored.</p>
 */
@Component
public class SeedDataProvider {

    private static final Logger log = LoggerFactory.getLogger(SeedDataProvider.class);
    private static final String SEED_FILE = "seed-data.cypher";

    public List<String> seedStatements() {
        try {
            String cypher = new ClassPathResource(SEED_FILE)
                    .getContentAsString(StandardCharsets.UTF_8);
            return splitStatements(cypher);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load seed data from " + SEED_FILE, ex);
        }
    }

    private List<String> splitStatements(String cypher) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : cypher.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("//")) {
                continue;
            }
            int semicolon = trimmed.indexOf(';');
            if (semicolon >= 0) {
                current.append(trimmed, 0, semicolon);
                if (current.toString().isBlank()) {
                    current.setLength(0);
                    continue;
                }
                statements.add(current.toString().trim());
                current.setLength(0);
                String rest = trimmed.substring(semicolon + 1).trim();
                if (!rest.isEmpty()) {
                    current.append(rest);
                }
            } else {
                current.append(trimmed).append(' ');
            }
        }
        if (!current.toString().isBlank()) {
            statements.add(current.toString().trim());
        }
        log.info("Parsed {} Cypher statements from {}", statements.size(), SEED_FILE);
        return statements;
    }
}
