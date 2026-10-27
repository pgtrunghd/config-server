package com.example.configserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerIntegrationTest {
    @TempDir
    static Path repository;

    static String commitId;

    @DynamicPropertySource
    static void configureGitRepository(DynamicPropertyRegistry registry) throws Exception {
        Files.copy(Path.of("config-repo/patient-service.properties"),
                repository.resolve("patient-service.properties"));
        try (Git git = Git.init().setDirectory(repository.toFile()).setInitialBranch("main").call()) {
            git.add().addFilepattern("patient-service.properties").call();
            commitId = git.commit().setMessage("Add test configuration")
                    .setAuthor("Config Test", "config-test@example.com")
                    .setCommitter("Config Test", "config-test@example.com")
                    .call().getName();
        }
        registry.add("spring.cloud.config.server.git.uri", () -> repository.toUri().toString());
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void servesPatientPropertiesFromCommittedMainBranch() {
        var response = rest.getForEntity("/patient-service/default", Environment.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Environment body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getName()).isEqualTo("patient-service");
        assertThat(body.getProfiles()).containsExactly("default");
        assertThat(body.getVersion()).isEqualTo(commitId);
        assertThat(body.getPropertySources()).hasSize(1);
        Map<?, ?> source = body.getPropertySources().get(0).getSource();
        assertThat(source.get("server.port")).isEqualTo("8081");
        assertThat(source.get("message")).isEqualTo("Chao mung den voi Patient Service tu Config Server");
        assertThat(source.get("db.url")).isEqualTo("jdbc:mysql://localhost:3306/patient_db");
        assertThat(source.get("db.username")).isEqualTo("root");
    }
}
