package fr.florianpal.fmessage.core.util;

import fr.florianpal.fmessage.fakes.FakeProxyLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceExporterTest {

    @TempDir
    Path tempDir;

    private FakeProxyLogger logger;

    @BeforeEach
    void setUp() {
        logger = new FakeProxyLogger();
    }

    @Test
    void writes_a_bundled_resource_that_is_not_on_disk_yet() {
        File target = tempDir.resolve("config.yml").toFile();

        boolean written = ResourceExporter.copyIfAbsent("config.yml", target, logger);

        assertThat(written).isTrue();
        assertThat(target).exists().content().contains("senderChatFormat");
    }

    @Test
    void creates_the_missing_parent_folders() {
        File target = tempDir.resolve("nested/deeper/config.yml").toFile();

        assertThat(ResourceExporter.copyIfAbsent("config.yml", target, logger)).isTrue();
        assertThat(target).exists();
    }

    @Test
    void never_overwrites_an_existing_file() throws IOException {
        File target = tempDir.resolve("config.yml").toFile();
        Files.writeString(target.toPath(), "réglages de l'admin");

        boolean written = ResourceExporter.copyIfAbsent("config.yml", target, logger);

        assertThat(written).isFalse();
        assertThat(target).content().isEqualTo("réglages de l'admin");
    }

    @Test
    void reports_a_missing_bundled_resource_without_throwing() {
        File target = tempDir.resolve("absent.yml").toFile();

        boolean written = ResourceExporter.copyIfAbsent("absent.yml", target, logger);

        assertThat(written).isFalse();
        assertThat(target).doesNotExist();
        assertThat(logger.errors).anyMatch(message -> message.contains("absent.yml"));
    }

    @Test
    void logs_the_path_it_wrote() {
        File target = tempDir.resolve("lang_fr.yml").toFile();

        ResourceExporter.copyIfAbsent("lang_fr.yml", target, logger);

        assertThat(logger.info).anyMatch(message -> message.contains(target.getAbsolutePath()));
    }
}
