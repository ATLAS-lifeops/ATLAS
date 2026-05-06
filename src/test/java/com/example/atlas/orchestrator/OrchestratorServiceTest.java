package com.example.atlas.orchestrator;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.coach.CoachAgent;
import com.example.atlas.agent.core.CoreAgent;
import com.example.atlas.agent.fuel.FuelAgent;
import com.example.atlas.agent.habits.HabitsAgent;
import com.example.atlas.agent.planner.PlannerAgent;
import com.example.atlas.agent.recovery.RecoveryAgent;
import com.example.atlas.agent.report.ReportAgent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrchestratorServiceTest {

    private final OrchestratorService orchestratorService = new OrchestratorService(List.of(
            new CoreAgent(),
            new CoachAgent(),
            new PlannerAgent(),
            new RecoveryAgent(),
            new HabitsAgent(),
            new FuelAgent(),
            new ReportAgent()
    ));

    @Test
    void resolvesKnownCommands() {
        assertThat(orchestratorService.resolveRequestType("/start")).isEqualTo(RequestType.START);
        assertThat(orchestratorService.resolveRequestType("/day")).isEqualTo(RequestType.DAY_PLAN);
        assertThat(orchestratorService.resolveRequestType("/week")).isEqualTo(RequestType.WEEK_PLAN);
        assertThat(orchestratorService.resolveRequestType("/workout")).isEqualTo(RequestType.WORKOUT);
        assertThat(orchestratorService.resolveRequestType("/checkin")).isEqualTo(RequestType.CHECKIN);
        assertThat(orchestratorService.resolveRequestType("/recovery")).isEqualTo(RequestType.RECOVERY);
        assertThat(orchestratorService.resolveRequestType("/habits")).isEqualTo(RequestType.HABITS);
        assertThat(orchestratorService.resolveRequestType("/food")).isEqualTo(RequestType.FOOD);
        assertThat(orchestratorService.resolveRequestType("/report")).isEqualTo(RequestType.REPORT);
        assertThat(orchestratorService.resolveRequestType("/emergency")).isEqualTo(RequestType.EMERGENCY);
    }

    @Test
    void treatsUnknownTextAsGeneralRequest() {
        assertThat(orchestratorService.resolveRequestType("как вернуться в режим?"))
                .isEqualTo(RequestType.GENERAL);
    }

    @Test
    void routesDayPlanToPlanner() {
        assertThat(orchestratorService.route("/day").handledBy())
                .containsExactly("ATLAS Planner");
    }

    @Test
    void routesCheckinToCoachAndRecovery() {
        assertThat(orchestratorService.route("/checkin").handledBy())
                .containsExactlyInAnyOrder("ATLAS Coach", "ATLAS Recovery");
    }

    @Test
    void routesEmergencyToHabitsAndRecovery() {
        assertThat(orchestratorService.route("/emergency").handledBy())
                .containsExactlyInAnyOrder("ATLAS Habits", "ATLAS Recovery");
    }

    @Test
    void allAgentsHaveNames() {
        assertThat(orchestratorService.route("/report").handledBy())
                .allSatisfy(name -> assertThat(name).startsWith("ATLAS "));
    }
}
