package com.skillbridge.seed;

import com.skillbridge.config.AppProperties;
import com.skillbridge.repository.GraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final GraphRepository repository;
    private final SeedDataProvider seedDataProvider;
    private final AppProperties appProperties;

    public DataSeeder(GraphRepository repository, SeedDataProvider seedDataProvider, AppProperties appProperties) {
        this.repository = repository;
        this.seedDataProvider = seedDataProvider;
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!appProperties.isSeedData()) {
            log.info("SEED_DATA=false - skipping database seeding");
            return;
        }

        if (repository.hasData()) {
            log.info("Database already contains data - skipping seed");
            return;
        }

        log.info("Seeding CognoDB with SkillBridge demo data...");
        repository.runSeedStatements(seedDataProvider.seedStatements());
        log.info("Seed complete");
    }
}
