package com.skillbridge.seed;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeedDataProviderTest {

    private final SeedDataProvider provider = new SeedDataProvider();

    @Test
    void parsesAllStatementsFromCypherFile() {
        List<String> statements = provider.seedStatements();

        assertThat(statements).isNotEmpty();
        assertThat(statements).noneMatch(s -> s.contains(";"));
        assertThat(statements).noneMatch(s -> s.isBlank());
        assertThat(statements).allMatch(s -> s.contains("MATCH") || s.contains("CREATE"));

        long createStatements = statements.stream().filter(s -> s.startsWith("CREATE")).count();
        long matchStatements = statements.stream().filter(s -> s.startsWith("MATCH")).count();
        assertThat(createStatements).isGreaterThan(30);
        assertThat(matchStatements).isGreaterThan(40);
    }

    @Test
    void includesKeyRelationships() {
        List<String> statements = provider.seedStatements();
        String all = String.join("\n", statements);

        assertThat(all).contains("PREREQUISITE_FOR").contains("HAS_SKILL")
                .contains("REQUIRES").contains("WORKS_AS")
                .contains("EMPLOYS").contains("OFFERS_ROLE");
    }
}
