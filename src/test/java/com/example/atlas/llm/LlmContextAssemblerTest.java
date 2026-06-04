package com.example.atlas.llm;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.life.LifeArea;
import com.example.atlas.life.PlanningStyle;
import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.life.service.LifeProfileService;
import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LlmContextAssemblerTest {

    @Test
    void assemblesScopedContextWithoutSecretsOrInternalIds() {
        TelegramUserEntity user = TelegramUserEntity.create(7L, 42L, "user", "User", Instant.parse("2026-06-01T08:00:00Z"));
        user.updateLanguage(UserLanguage.EN);
        TelegramUserEntity otherUser = TelegramUserEntity.create(8L, 43L, "other", "Other", Instant.parse("2026-06-01T08:00:00Z"));
        LifeProfileEntity profile = LifeProfileEntity.create(user, Instant.parse("2026-06-01T08:00:00Z"));
        profile.updatePrimaryLifeArea(LifeArea.FOCUS, Instant.parse("2026-06-01T08:01:00Z"));
        profile.updateCurrentFocus("Ship focused work", Instant.parse("2026-06-01T08:02:00Z"));
        profile.updatePlanningStyle(PlanningStyle.MINIMAL, Instant.parse("2026-06-01T08:03:00Z"));
        profile.updateLifeLoops(true, true, true, false, false, true, Instant.parse("2026-06-01T08:04:00Z"));
        profile.completeOnboarding(Instant.parse("2026-06-01T08:05:00Z"));

        CheckInEntity ownCheckIn = CheckInEntity.create(user, 6, null, 5, 7, 4, 6, "Deep work", true, false, "Too many meetings", Instant.parse("2026-06-02T08:00:00Z"));
        CheckInEntity otherCheckIn = CheckInEntity.create(otherUser, 1, null, 1, 1, 10, 1, "Other user priority", false, false, "other", Instant.parse("2026-06-02T08:00:00Z"));
        HabitCheckEntity habit = HabitCheckEntity.create(user, "Read", "2 pages", true, "", Instant.parse("2026-06-02T20:00:00Z"));
        EveningReflectionEntity reflection = EveningReflectionEntity.create(user, "Finished draft", "Context switching", "Review", Instant.parse("2026-06-02T21:00:00Z"));

        FakeLifeProfileService lifeProfileService = new FakeLifeProfileService(profile);
        ScopedCheckInRepository checkInRepository = new ScopedCheckInRepository(user, ownCheckIn, otherUser, otherCheckIn);
        FakeHabitService habitService = new FakeHabitService(user, habit);
        FakeEveningReflectionService reflectionService = new FakeEveningReflectionService(user, reflection);

        LlmContextAssembler assembler = new LlmContextAssembler(
                lifeProfileService,
                checkInRepository.proxy,
                habitService,
                reflectionService,
                new SafetyGuard(),
                Clock.fixed(Instant.parse("2026-06-04T08:00:00Z"), ZoneOffset.UTC)
        );

        PromptContext context = assembler.assemble(user, PromptPurpose.DAY_PLAN, "Plan my focus day");

        assertThat(context.structuredContext())
                .contains("Ship focused work")
                .contains("Deep work")
                .contains("check_ins=1")
                .contains("Read")
                .contains("Finished draft")
                .doesNotContain("Other user priority")
                .doesNotContain(user.getId().toString())
                .doesNotContain("telegram")
                .doesNotContain("webhook")
                .doesNotContain("api_key");
        assertThat(context.safetyRisk()).isTrue();
        assertThat(checkInRepository.scopedRecentCalled).isTrue();
    }

    private static class FakeLifeProfileService extends LifeProfileService {
        private final LifeProfileEntity profile;

        FakeLifeProfileService(LifeProfileEntity profile) {
            super(null);
            this.profile = profile;
        }

        @Override
        public Optional<LifeProfileEntity> find(TelegramUserEntity user) {
            return Optional.of(profile);
        }
    }

    private static class FakeHabitService extends HabitService {
        private final TelegramUserEntity user;
        private final HabitCheckEntity habit;

        FakeHabitService(TelegramUserEntity user, HabitCheckEntity habit) {
            super(null);
            this.user = user;
            this.habit = habit;
        }

        @Override
        public List<HabitCheckEntity> recent(TelegramUserEntity user, Instant since) {
            return this.user == user ? List.of(habit) : List.of();
        }
    }

    private static class FakeEveningReflectionService extends EveningReflectionService {
        private final TelegramUserEntity user;
        private final EveningReflectionEntity reflection;

        FakeEveningReflectionService(TelegramUserEntity user, EveningReflectionEntity reflection) {
            super(null);
            this.user = user;
            this.reflection = reflection;
        }

        @Override
        public List<EveningReflectionEntity> recent(TelegramUserEntity user, Instant since) {
            return this.user == user ? List.of(reflection) : List.of();
        }
    }

    private static class ScopedCheckInRepository {
        private final CheckInRepository proxy;
        private final TelegramUserEntity user;
        private final CheckInEntity checkIn;
        private final TelegramUserEntity otherUser;
        private final CheckInEntity otherCheckIn;
        private boolean scopedRecentCalled;

        ScopedCheckInRepository(TelegramUserEntity user, CheckInEntity checkIn, TelegramUserEntity otherUser, CheckInEntity otherCheckIn) {
            this.user = user;
            this.checkIn = checkIn;
            this.otherUser = otherUser;
            this.otherCheckIn = otherCheckIn;
            this.proxy = (CheckInRepository) Proxy.newProxyInstance(
                    CheckInRepository.class.getClassLoader(),
                    new Class<?>[]{CheckInRepository.class},
                    (target, method, args) -> {
                        if ("findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc".equals(method.getName())) {
                            TelegramUserEntity requested = (TelegramUserEntity) args[0];
                            if (requested == this.user) {
                                scopedRecentCalled = true;
                                return List.of(this.checkIn);
                            }
                            if (requested == this.otherUser) {
                                return List.of(this.otherCheckIn);
                            }
                            return List.of();
                        }
                        if ("findByTelegramUserOrderByCreatedAtDesc".equals(method.getName())) {
                            return args[0] == this.user ? List.of(this.checkIn) : List.of();
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
