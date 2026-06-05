package com.example.atlas.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.example.atlas");

    @Test
    void domainPackagesDoNotDependOnSpringJpaHibernateOrTelegramDto() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "com.example.atlas.telegram.."
                )
                .check(productionClasses);
    }

    @Test
    void applicationPackagesDoNotDependOnModuleInfrastructurePackages() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(productionClasses);
    }

    @Test
    void trackingProfilePlanningAndReportingDomainDoNotUseTelegramAdapter() {
        noClasses()
                .that().resideInAnyPackage(
                        "com.example.atlas.tracking.domain..",
                        "com.example.atlas.profile.domain..",
                        "com.example.atlas.planning.domain..",
                        "com.example.atlas.reporting.domain.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.example.atlas.telegram..")
                .check(productionClasses);
    }

    @Test
    void telegramHandlersDoNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("com.example.atlas.telegram..")
                .and().haveSimpleNameEndingWith("Handler")
                .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
                .check(productionClasses);
    }
}
