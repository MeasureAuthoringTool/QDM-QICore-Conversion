package gov.cms.mat.patients.conversion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.BuildInfoContributor;
import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Configuration
@Slf4j
public class BuildInfo {
    private final BuildProperties buildProperties;
    private final GitProperties gitProperties;

    public BuildInfo(BuildProperties buildProperties, GitProperties gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @PostConstruct
    public void logDetails() {
        log.info("BuildProperties: artifact: {}, version: {}, time: {}",
                buildProperties.getArtifact(),
                buildProperties.getVersion(),
                buildProperties.getTime());

        log.info("GitProperties Commit: branch: {}, id: {}, time: {}, user: {}, message: {} ",
                gitProperties.getBranch(),
                gitProperties.getShortCommitId(),
                gitProperties.getCommitTime(),
                gitProperties.get("commit.user.name"),
                gitProperties.get("commit.message.short"));
    }

    @Bean
    public InfoEndpoint infoEndpoint() {
        BuildInfoContributor buildInfoContributor = new BuildInfoContributor(buildProperties);
        GitInfoContributorExtra gitInfoContributor = new GitInfoContributorExtra(gitProperties);
        return new InfoEndpoint(List.of(buildInfoContributor, gitInfoContributor));
    }

    private static class GitInfoContributorExtra extends GitInfoContributor {
        public GitInfoContributorExtra(GitProperties properties) {
            super(properties);
        }

        @Override
        protected PropertySource<?> toSimplePropertySource() {
            Properties props = new Properties();
            copyIfSet(props, "branch");
            String commitId = getProperties().getShortCommitId();
            if (commitId != null) {
                props.put("commit.id", commitId);
            }
            copyIfSet(props, "commit.time");

            // these the extra bits
            copyIfSet(props, "commit.user.name");
            copyIfSet(props, "commit.message.short");

            return new PropertiesPropertySource("git", props);
        }
    }
}
