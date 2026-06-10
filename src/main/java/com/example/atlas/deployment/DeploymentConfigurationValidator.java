package com.example.atlas.deployment;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DeploymentConfigurationValidator implements ApplicationRunner {

    private final DeploymentModeService deploymentModeService;

    public DeploymentConfigurationValidator(DeploymentModeService deploymentModeService) {
        this.deploymentModeService = deploymentModeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        deploymentModeService.validate();
    }
}
