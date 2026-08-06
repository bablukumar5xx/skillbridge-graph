// ============================================================
// SkillBridge Graph - seed data for CognoDB
// Loaded automatically on startup when SEED_DATA=true.
// Statements are separated by ';'. Idempotent-friendly: run on
// an empty graph (the app skips seeding if data already exists).
// ============================================================

// ---------- Skills ----------
CREATE (java:Skill {id: 'java', name: 'Java', category: 'Programming', difficulty: 3});
CREATE (python:Skill {id: 'python', name: 'Python', category: 'Programming', difficulty: 2});
CREATE (sql:Skill {id: 'sql', name: 'SQL', category: 'Data', difficulty: 2});
CREATE (git:Skill {id: 'git', name: 'Git', category: 'DevOps', difficulty: 1});
CREATE (linux:Skill {id: 'linux', name: 'Linux', category: 'DevOps', difficulty: 2});
CREATE (oop:Skill {id: 'oop', name: 'OOP Design', category: 'Programming', difficulty: 3});
CREATE (spring:Skill {id: 'spring', name: 'Spring Boot', category: 'Backend', difficulty: 4});
CREATE (rest:Skill {id: 'rest', name: 'REST APIs', category: 'Backend', difficulty: 3});
CREATE (docker:Skill {id: 'docker', name: 'Docker', category: 'DevOps', difficulty: 3});
CREATE (k8s:Skill {id: 'k8s', name: 'Kubernetes', category: 'DevOps', difficulty: 5});
CREATE (aws:Skill {id: 'aws', name: 'AWS', category: 'Cloud', difficulty: 4});
CREATE (react:Skill {id: 'react', name: 'React', category: 'Frontend', difficulty: 3});
CREATE (typescript:Skill {id: 'typescript', name: 'TypeScript', category: 'Frontend', difficulty: 3});
CREATE (node:Skill {id: 'node', name: 'Node.js', category: 'Backend', difficulty: 3});
CREATE (graphql:Skill {id: 'graphql', name: 'GraphQL', category: 'Backend', difficulty: 4});
CREATE (kafka:Skill {id: 'kafka', name: 'Apache Kafka', category: 'Data', difficulty: 4});
CREATE (spark:Skill {id: 'spark', name: 'Apache Spark', category: 'Data', difficulty: 5});
CREATE (ml:Skill {id: 'ml', name: 'Machine Learning', category: 'AI/ML', difficulty: 5});
CREATE (dl:Skill {id: 'dl', name: 'Deep Learning', category: 'AI/ML', difficulty: 5});
CREATE (nlp:Skill {id: 'nlp', name: 'NLP', category: 'AI/ML', difficulty: 5});
CREATE (stats:Skill {id: 'stats', name: 'Statistics', category: 'Data', difficulty: 4});
CREATE (cypher:Skill {id: 'cypher', name: 'Cypher', category: 'Data', difficulty: 3});
CREATE (neo4j:Skill {id: 'neo4j', name: 'Neo4j', category: 'Data', difficulty: 4});
CREATE (sysdesign:Skill {id: 'sysdesign', name: 'System Design', category: 'Architecture', difficulty: 5});
CREATE (microservices:Skill {id: 'microservices', name: 'Microservices', category: 'Architecture', difficulty: 4});

// ---------- Skill prerequisites (multi-hop paths) ----------
MATCH (a:Skill {id: 'java'}), (b:Skill {id: 'oop'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'oop'}), (b:Skill {id: 'spring'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'spring'}), (b:Skill {id: 'rest'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'spring'}), (b:Skill {id: 'microservices'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'microservices'}), (b:Skill {id: 'sysdesign'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 4}]->(b);
MATCH (a:Skill {id: 'linux'}), (b:Skill {id: 'docker'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'docker'}), (b:Skill {id: 'k8s'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 4}]->(b);
MATCH (a:Skill {id: 'docker'}), (b:Skill {id: 'aws'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'python'}), (b:Skill {id: 'stats'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'stats'}), (b:Skill {id: 'ml'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 4}]->(b);
MATCH (a:Skill {id: 'ml'}), (b:Skill {id: 'dl'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 5}]->(b);
MATCH (a:Skill {id: 'dl'}), (b:Skill {id: 'nlp'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 4}]->(b);
MATCH (a:Skill {id: 'sql'}), (b:Skill {id: 'spark'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'python'}), (b:Skill {id: 'spark'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'sql'}), (b:Skill {id: 'cypher'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'cypher'}), (b:Skill {id: 'neo4j'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'git'}), (b:Skill {id: 'linux'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 1}]->(b);
MATCH (a:Skill {id: 'typescript'}), (b:Skill {id: 'react'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'node'}), (b:Skill {id: 'graphql'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);
MATCH (a:Skill {id: 'rest'}), (b:Skill {id: 'graphql'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 2}]->(b);
MATCH (a:Skill {id: 'java'}), (b:Skill {id: 'kafka'}) CREATE (a)-[:PREREQUISITE_FOR {monthsToLearn: 3}]->(b);

// ---------- Roles ----------
CREATE (be:Role {id: 'backend-eng', title: 'Backend Engineer', level: 'Mid', domain: 'Engineering'});
CREATE (fe:Role {id: 'frontend-eng', title: 'Frontend Engineer', level: 'Mid', domain: 'Engineering'});
CREATE (fs:Role {id: 'fullstack-eng', title: 'Full Stack Engineer', level: 'Senior', domain: 'Engineering'});
CREATE (devops:Role {id: 'devops-eng', title: 'DevOps Engineer', level: 'Mid', domain: 'Infrastructure'});
CREATE (sre:Role {id: 'sre', title: 'Site Reliability Engineer', level: 'Senior', domain: 'Infrastructure'});
CREATE (mleng:Role {id: 'ml-engineer', title: 'ML Engineer', level: 'Senior', domain: 'AI/ML'});
CREATE (dataeng:Role {id: 'data-engineer', title: 'Data Engineer', level: 'Mid', domain: 'Data'});
CREATE (graphdev:Role {id: 'graph-dev', title: 'Graph Database Developer', level: 'Mid', domain: 'Data'});
CREATE (arch:Role {id: 'architect', title: 'Software Architect', level: 'Principal', domain: 'Engineering'});
CREATE (em:Role {id: 'eng-manager', title: 'Engineering Manager', level: 'Lead', domain: 'Management'});

// ---------- Role skill requirements ----------
MATCH (r:Role {id: 'backend-eng'}) MATCH (s:Skill) WHERE s.id IN ['java', 'spring', 'rest', 'sql', 'git'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'frontend-eng'}) MATCH (s:Skill) WHERE s.id IN ['typescript', 'react', 'git', 'rest'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'fullstack-eng'}) MATCH (s:Skill) WHERE s.id IN ['java', 'spring', 'react', 'typescript', 'rest', 'sql', 'docker'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'devops-eng'}) MATCH (s:Skill) WHERE s.id IN ['linux', 'docker', 'k8s', 'aws', 'git'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'sre'}) MATCH (s:Skill) WHERE s.id IN ['linux', 'docker', 'k8s', 'aws', 'sysdesign', 'kafka'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'ml-engineer'}) MATCH (s:Skill) WHERE s.id IN ['python', 'ml', 'dl', 'stats', 'sql', 'spark'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'data-engineer'}) MATCH (s:Skill) WHERE s.id IN ['python', 'sql', 'spark', 'kafka', 'aws'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'graph-dev'}) MATCH (s:Skill) WHERE s.id IN ['cypher', 'neo4j', 'java', 'rest', 'sql'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'architect'}) MATCH (s:Skill) WHERE s.id IN ['sysdesign', 'microservices', 'java', 'aws', 'k8s'] CREATE (r)-[:REQUIRES {proficiency: 'advanced'}]->(s);
MATCH (r:Role {id: 'eng-manager'}) MATCH (s:Skill) WHERE s.id IN ['sysdesign', 'git'] CREATE (r)-[:REQUIRES {proficiency: 'intermediate'}]->(s);

// ---------- Companies ----------
CREATE (google:Company {id: 'google', name: 'Google', industry: 'Technology', location: 'Mountain View, CA'});
CREATE (meta:Company {id: 'meta', name: 'Meta', industry: 'Technology', location: 'Menlo Park, CA'});
CREATE (stripe:Company {id: 'stripe', name: 'Stripe', industry: 'Fintech', location: 'San Francisco, CA'});
CREATE (netflix:Company {id: 'netflix', name: 'Netflix', industry: 'Streaming', location: 'Los Gatos, CA'});
CREATE (wexa:Company {id: 'wexa', name: 'Wexa AI', industry: 'AI/Database', location: 'Remote'});
CREATE (databricks:Company {id: 'databricks', name: 'Databricks', industry: 'Data/AI', location: 'San Francisco, CA'});

// ---------- People ----------
CREATE (p1:Person {id: 'priya', name: 'Priya Sharma', bio: 'Backend specialist with 6 years in Java ecosystems.', yearsExperience: 6});
CREATE (p2:Person {id: 'alex', name: 'Alex Chen', bio: 'Full stack developer passionate about React and Node.', yearsExperience: 5});
CREATE (p3:Person {id: 'maria', name: 'Maria Garcia', bio: 'DevOps engineer focused on cloud-native infrastructure.', yearsExperience: 7});
CREATE (p4:Person {id: 'james', name: 'James Okafor', bio: 'ML engineer building production NLP pipelines.', yearsExperience: 4});
CREATE (p5:Person {id: 'sarah', name: 'Sarah Kim', bio: 'Data engineer with Spark and Kafka expertise.', yearsExperience: 5});
CREATE (p6:Person {id: 'david', name: 'David Patel', bio: 'Graph database enthusiast and Cypher expert.', yearsExperience: 3});
CREATE (p7:Person {id: 'emma', name: 'Emma Wilson', bio: 'Software architect designing distributed systems.', yearsExperience: 10});
CREATE (p8:Person {id: 'raj', name: 'Raj Mehta', bio: 'Junior developer transitioning from frontend to full stack.', yearsExperience: 2});
CREATE (p9:Person {id: 'lisa', name: 'Lisa Nguyen', bio: 'Engineering manager with strong system design background.', yearsExperience: 12});
CREATE (p10:Person {id: 'bablu', name: 'Bablu Kumar', bio: 'Senior Java developer exploring graph databases.', yearsExperience: 8});

// ---------- Person skills ----------
MATCH (p:Person {id: 'priya'}) MATCH (s:Skill) WHERE s.id IN ['java', 'spring', 'rest', 'sql', 'git', 'kafka'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 5}]->(s);
MATCH (p:Person {id: 'alex'}) MATCH (s:Skill) WHERE s.id IN ['typescript', 'react', 'node', 'git', 'rest'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 4}]->(s);
MATCH (p:Person {id: 'maria'}) MATCH (s:Skill) WHERE s.id IN ['linux', 'docker', 'k8s', 'aws', 'git'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 6}]->(s);
MATCH (p:Person {id: 'james'}) MATCH (s:Skill) WHERE s.id IN ['python', 'ml', 'stats', 'sql'] CREATE (p)-[:HAS_SKILL {proficiency: 'intermediate', years: 3}]->(s);
MATCH (p:Person {id: 'sarah'}) MATCH (s:Skill) WHERE s.id IN ['python', 'sql', 'spark', 'kafka', 'aws'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 4}]->(s);
MATCH (p:Person {id: 'david'}) MATCH (s:Skill) WHERE s.id IN ['cypher', 'neo4j', 'java', 'sql', 'rest'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 2}]->(s);
MATCH (p:Person {id: 'emma'}) MATCH (s:Skill) WHERE s.id IN ['sysdesign', 'microservices', 'java', 'aws', 'k8s', 'docker'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 8}]->(s);
MATCH (p:Person {id: 'raj'}) MATCH (s:Skill) WHERE s.id IN ['typescript', 'react', 'git'] CREATE (p)-[:HAS_SKILL {proficiency: 'intermediate', years: 2}]->(s);
MATCH (p:Person {id: 'lisa'}) MATCH (s:Skill) WHERE s.id IN ['sysdesign', 'git', 'java', 'rest'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 10}]->(s);
MATCH (p:Person {id: 'bablu'}) MATCH (s:Skill) WHERE s.id IN ['java', 'spring', 'sql', 'git', 'rest', 'cypher'] CREATE (p)-[:HAS_SKILL {proficiency: 'advanced', years: 7}]->(s);

// ---------- Works as ----------
MATCH (p:Person {id: 'priya'}), (r:Role {id: 'backend-eng'}) CREATE (p)-[:WORKS_AS {since: 2020}]->(r);
MATCH (p:Person {id: 'alex'}), (r:Role {id: 'frontend-eng'}) CREATE (p)-[:WORKS_AS {since: 2021}]->(r);
MATCH (p:Person {id: 'maria'}), (r:Role {id: 'devops-eng'}) CREATE (p)-[:WORKS_AS {since: 2019}]->(r);
MATCH (p:Person {id: 'james'}), (r:Role {id: 'ml-engineer'}) CREATE (p)-[:WORKS_AS {since: 2022}]->(r);
MATCH (p:Person {id: 'sarah'}), (r:Role {id: 'data-engineer'}) CREATE (p)-[:WORKS_AS {since: 2021}]->(r);
MATCH (p:Person {id: 'david'}), (r:Role {id: 'graph-dev'}) CREATE (p)-[:WORKS_AS {since: 2023}]->(r);
MATCH (p:Person {id: 'emma'}), (r:Role {id: 'architect'}) CREATE (p)-[:WORKS_AS {since: 2018}]->(r);
MATCH (p:Person {id: 'raj'}), (r:Role {id: 'frontend-eng'}) CREATE (p)-[:WORKS_AS {since: 2024}]->(r);
MATCH (p:Person {id: 'lisa'}), (r:Role {id: 'eng-manager'}) CREATE (p)-[:WORKS_AS {since: 2017}]->(r);
MATCH (p:Person {id: 'bablu'}), (r:Role {id: 'backend-eng'}) CREATE (p)-[:WORKS_AS {since: 2019}]->(r);

// ---------- Employment ----------
MATCH (c:Company {id: 'google'}), (p:Person) WHERE p.id IN ['priya', 'james'] CREATE (c)-[:EMPLOYS {since: 2020}]->(p);
MATCH (c:Company {id: 'meta'}), (p:Person) WHERE p.id IN ['alex', 'emma'] CREATE (c)-[:EMPLOYS {since: 2019}]->(p);
MATCH (c:Company {id: 'stripe'}), (p:Person) WHERE p.id IN ['maria', 'sarah'] CREATE (c)-[:EMPLOYS {since: 2021}]->(p);
MATCH (c:Company {id: 'netflix'}), (p:Person) WHERE p.id IN ['raj', 'lisa'] CREATE (c)-[:EMPLOYS {since: 2022}]->(p);
MATCH (c:Company {id: 'wexa'}), (p:Person) WHERE p.id IN ['david', 'bablu'] CREATE (c)-[:EMPLOYS {since: 2024}]->(p);
MATCH (c:Company {id: 'databricks'}), (p:Person) WHERE p.id = 'sarah' CREATE (c)-[:EMPLOYS {since: 2019}]->(p);

// ---------- Companies offer roles ----------
MATCH (c:Company {id: 'google'}) MATCH (r:Role) WHERE r.id IN ['backend-eng', 'ml-engineer', 'architect'] CREATE (c)-[:OFFERS_ROLE]->(r);
MATCH (c:Company {id: 'meta'}) MATCH (r:Role) WHERE r.id IN ['frontend-eng', 'fullstack-eng', 'architect'] CREATE (c)-[:OFFERS_ROLE]->(r);
MATCH (c:Company {id: 'stripe'}) MATCH (r:Role) WHERE r.id IN ['backend-eng', 'devops-eng', 'data-engineer'] CREATE (c)-[:OFFERS_ROLE]->(r);
MATCH (c:Company {id: 'wexa'}) MATCH (r:Role) WHERE r.id IN ['graph-dev', 'backend-eng', 'ml-engineer'] CREATE (c)-[:OFFERS_ROLE]->(r);
