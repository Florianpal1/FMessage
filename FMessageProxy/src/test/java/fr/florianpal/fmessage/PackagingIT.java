package fr.florianpal.fmessage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Inspects the jar that will actually be shipped.
 *
 * <p>Runs after {@code package}, because everything it checks — the two descriptors living
 * side by side, the platform APIs staying out, ACF being merged into a single relocated
 * copy — only exists once the shade plugin has run.</p>
 */
class PackagingIT {

    private static List<String> entries;
    private static JarFile jar;

    @BeforeAll
    static void openJar() throws IOException {
        String path = System.getProperty("fmessage.jar");
        assertThat(path).as("propriété système fmessage.jar").isNotBlank();

        File file = new File(path);
        assertThat(file).as("jar shadé").exists();

        jar = new JarFile(file);
        entries = new ArrayList<>();
        Enumeration<JarEntry> enumeration = jar.entries();
        while (enumeration.hasMoreElements()) {
            entries.add(enumeration.nextElement().getName());
        }
    }

    private static String read(String entry) throws IOException {
        try (InputStream input = jar.getInputStream(jar.getEntry(entry))) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Nested
    @DisplayName("les deux descripteurs cohabitent")
    class Descriptors {

        @Test
        void the_jar_carries_both_plugin_descriptors() {
            assertThat(entries).contains("plugin.yml", "velocity-plugin.json");
        }

        @Test
        void the_bungee_descriptor_points_at_the_bungee_bootstrap() throws IOException {
            assertThat(read("plugin.yml"))
                    .contains("main: fr.florianpal.fmessage.bungee.BungeeBootstrap");
        }

        @Test
        void the_velocity_descriptor_points_at_the_velocity_bootstrap() throws IOException {
            assertThat(read("velocity-plugin.json"))
                    .contains("\"main\":\"fr.florianpal.fmessage.velocity.VelocityBootstrap\"");
        }

        @Test
        void the_bungee_plugin_name_is_unchanged_so_existing_data_folders_stay_visible()
                throws IOException {
            assertThat(read("plugin.yml")).contains("name: FMessageBungee");
        }

        @Test
        void the_velocity_plugin_id_is_unchanged_for_the_same_reason() throws IOException {
            assertThat(read("velocity-plugin.json")).contains("\"id\":\"fmessage\"");
        }
    }

    @Nested
    @DisplayName("les API de plateforme restent dehors")
    class ProvidedApis {

        @Test
        void bungeecord_is_not_bundled() {
            assertThat(entries).noneMatch(entry -> entry.startsWith("net/md_5/"));
        }

        @Test
        void velocity_is_not_bundled() {
            assertThat(entries).noneMatch(entry -> entry.startsWith("com/velocitypowered/"));
        }

        @Test
        void adventure_is_not_bundled() {
            // Le cœur travaille en texte legacy justement pour ne pas avoir à l'embarquer :
            // une copie shadée d'Adventure ferait un ClassCastException sur Velocity.
            assertThat(entries).noneMatch(entry -> entry.startsWith("net/kyori/"));
        }
    }

    @Nested
    @DisplayName("ACF fusionné et relocalisé")
    class Acf {

        @Test
        void nothing_remains_under_the_original_aikar_package() {
            assertThat(entries).noneMatch(entry -> entry.startsWith("co/aikar/"));
        }

        @Test
        void both_platform_command_managers_are_present() {
            assertThat(entries).contains(
                    "fr/florianpal/fmessage/acf/BungeeCommandManager.class",
                    "fr/florianpal/fmessage/acf/VelocityCommandManager.class");
        }

        @Test
        void the_shared_acf_core_is_present_exactly_once() {
            assertThat(entries)
                    .filteredOn(entry -> entry.equals("fr/florianpal/fmessage/acf/BaseCommand.class"))
                    .hasSize(1);
        }

        @Test
        void no_class_entry_is_duplicated() {
            List<String> classes = entries.stream().filter(entry -> entry.endsWith(".class")).toList();
            assertThat(classes).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("bStats")
    class BStats {

        @Test
        void both_platform_metrics_are_present() {
            assertThat(entries).contains(
                    "fr/florianpal/fmessage/bstats/bungeecord/Metrics.class",
                    "fr/florianpal/fmessage/bstats/velocity/Metrics.class");
        }
    }

    @Nested
    @DisplayName("le plugin lui-même")
    class Plugin {

        @Test
        void carries_both_bootstraps() {
            assertThat(entries).contains(
                    "fr/florianpal/fmessage/bungee/BungeeBootstrap.class",
                    "fr/florianpal/fmessage/velocity/VelocityBootstrap.class");
        }

        @Test
        void carries_the_default_configuration_and_both_languages() {
            assertThat(entries).contains("config.yml", "database.yml", "lang_en.yml", "lang_fr.yml");
        }

        @Test
        void bundles_the_database_driver_relocated() {
            assertThat(entries).anyMatch(entry -> entry.startsWith("fr/florianpal/fmessage/mysql/"));
            assertThat(entries).noneMatch(entry -> entry.startsWith("org/mariadb/"));
        }
    }
}
