package fr.florianpal.fmessage;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The guard rail of the whole single-jar design.
 *
 * <p>Both platform APIs sit on the compile classpath, so a stray
 * {@code import com.velocitypowered...} inside the core compiles happily, passes every
 * other test, and then blows up with a {@code NoClassDefFoundError} the first time the jar
 * starts on BungeeCord. Nothing else in the build catches that. These rules do.</p>
 */
@AnalyzeClasses(
        packages = "fr.florianpal.fmessage",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule the_core_never_touches_a_platform_api =
            noClasses()
                    .that().resideInAnyPackage("..core..", "..platform..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("net.md_5..", "com.velocitypowered..", "net.kyori..")
                    .because("le cœur doit être chargeable sur BungeeCord comme sur Velocity ; "
                            + "un seul import de plateforme le casse silencieusement sur l'autre");

    @ArchTest
    static final ArchRule the_core_never_touches_a_platform_specific_acf_class =
            noClasses()
                    .that().resideInAnyPackage("..core..", "..platform..")
                    .should().dependOnClassesThat()
                    .haveNameMatching("co\\.aikar\\.commands\\.(Bungee|Velocity).*")
                    .because("seules les classes partagées d'acf-core sont utilisables depuis le cœur");

    @ArchTest
    static final ArchRule the_bungee_adapter_ignores_velocity =
            noClasses()
                    .that().resideInAPackage("..fmessage.bungee..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..fmessage.velocity..", "com.velocitypowered..", "net.kyori..")
                    .because("charger l'adaptateur Bungee ne doit jamais entraîner une classe Velocity");

    @ArchTest
    static final ArchRule the_velocity_adapter_ignores_bungee =
            noClasses()
                    .that().resideInAPackage("..fmessage.velocity..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..fmessage.bungee..", "net.md_5..")
                    .because("charger l'adaptateur Velocity ne doit jamais entraîner une classe Bungee");

    @ArchTest
    static final ArchRule the_platform_layer_stays_free_of_the_core =
            noClasses()
                    .that().resideInAPackage("..fmessage.platform..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..fmessage.core..", "..fmessage.bungee..", "..fmessage.velocity..")
                    .because("le contrat d'abstraction ne doit dépendre de rien : c'est ce qui le rend testable");

    @ArchTest
    static final ArchRule nothing_reaches_back_into_an_adapter =
            noClasses()
                    .that().resideInAnyPackage("..fmessage.core..", "..fmessage.platform..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..fmessage.bungee..", "..fmessage.velocity..")
                    .because("les adaptateurs branchent le cœur, jamais l'inverse");
}
