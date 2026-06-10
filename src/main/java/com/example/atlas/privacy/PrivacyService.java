package com.example.atlas.privacy;

import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.conversation.repository.ConversationStateRepository;
import com.example.atlas.habit.repository.HabitCheckRepository;
import com.example.atlas.life.repository.LifeProfileRepository;
import com.example.atlas.integrations.repository.IntegrationSettingsRepository;
import com.example.atlas.memory.PersistentAgentMemoryService;
import com.example.atlas.message.repository.TelegramMessageRepository;
import com.example.atlas.planning.repository.WeeklyFocusRepository;
import com.example.atlas.reflection.repository.EveningReflectionRepository;
import com.example.atlas.reporting.repository.ReportArchiveRepository;
import com.example.atlas.routines.repository.RoutinePreferencesRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.repository.TelegramUserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@ConditionalOnBean({
        TelegramUserRepository.class,
        LifeProfileRepository.class,
        CheckInRepository.class,
        HabitCheckRepository.class,
        EveningReflectionRepository.class,
        PersistentAgentMemoryService.class
})
public class PrivacyService {

    private final TelegramUserRepository userRepository;
    private final LifeProfileRepository lifeProfileRepository;
    private final CheckInRepository checkInRepository;
    private final HabitCheckRepository habitCheckRepository;
    private final EveningReflectionRepository reflectionRepository;
    private final PersistentAgentMemoryService memoryService;
    private final ObjectProvider<TelegramMessageRepository> messageRepository;
    private final ObjectProvider<ConversationStateRepository> conversationStateRepository;
    private final ObjectProvider<RoutinePreferencesRepository> routinePreferencesRepository;
    private final ObjectProvider<WeeklyFocusRepository> weeklyFocusRepository;
    private final ObjectProvider<ReportArchiveRepository> reportArchiveRepository;
    private final ObjectProvider<IntegrationSettingsRepository> integrationSettingsRepository;

    public PrivacyService(
            TelegramUserRepository userRepository,
            LifeProfileRepository lifeProfileRepository,
            CheckInRepository checkInRepository,
            HabitCheckRepository habitCheckRepository,
            EveningReflectionRepository reflectionRepository,
            PersistentAgentMemoryService memoryService,
            ObjectProvider<TelegramMessageRepository> messageRepository,
            ObjectProvider<ConversationStateRepository> conversationStateRepository,
            ObjectProvider<RoutinePreferencesRepository> routinePreferencesRepository,
            ObjectProvider<WeeklyFocusRepository> weeklyFocusRepository,
            ObjectProvider<ReportArchiveRepository> reportArchiveRepository,
            ObjectProvider<IntegrationSettingsRepository> integrationSettingsRepository
    ) {
        this.userRepository = userRepository;
        this.lifeProfileRepository = lifeProfileRepository;
        this.checkInRepository = checkInRepository;
        this.habitCheckRepository = habitCheckRepository;
        this.reflectionRepository = reflectionRepository;
        this.memoryService = memoryService;
        this.messageRepository = messageRepository;
        this.conversationStateRepository = conversationStateRepository;
        this.routinePreferencesRepository = routinePreferencesRepository;
        this.weeklyFocusRepository = weeklyFocusRepository;
        this.reportArchiveRepository = reportArchiveRepository;
        this.integrationSettingsRepository = integrationSettingsRepository;
    }

    @Transactional(readOnly = true)
    public PrivacyPanel panel(UUID userId) {
        TelegramUserEntity user = userRepository.findById(userId).orElseThrow();
        return new PrivacyPanel(
                lifeProfileRepository.findByTelegramUser(user).isPresent() ? 1 : 0,
                checkInRepository.countByTelegramUser(user),
                habitCheckRepository.countByTelegramUser(user),
                reflectionRepository.countByTelegramUser(user),
                memoryService.countActive(userId),
                true
        );
    }

    @Transactional(readOnly = true)
    public PrivacyExport export(UUID userId) {
        PrivacyPanel panel = panel(userId);
        TelegramUserEntity user = userRepository.findById(userId).orElseThrow();
        String json = """
                {"userId":"%s","telegramUserId":%d,"profileCount":%d,"checkInCount":%d,"habitCount":%d,"reflectionCount":%d,"memoryCount":%d,"checkIns":%s,"habits":%s,"reflections":%s,"memory":%s}
                """.formatted(
                userId,
                user.getTelegramUserId(),
                panel.profileCount(),
                panel.checkInCount(),
                panel.habitCount(),
                panel.reflectionCount(),
                panel.memoryCount(),
                checkInsJson(user),
                habitsJson(user),
                reflectionsJson(user),
                memoryJson(userId)
        ).strip();
        String markdown = """
                # ATLAS export

                User: %s
                Telegram user id: %d
                Profile records: %d
                Check-ins: %d
                Habits: %d
                Reflections: %d
                Memory records: %d
                """.formatted(userId, user.getTelegramUserId(), panel.profileCount(), panel.checkInCount(), panel.habitCount(), panel.reflectionCount(), panel.memoryCount());
        return new PrivacyExport(json, markdown);
    }

    @Transactional
    public void forgetMemory(UUID userId, String confirmation) {
        requireConfirmation(confirmation);
        memoryService.archiveForUser(userId);
    }

    @Transactional
    public void deleteMyData(UUID userId, String confirmation) {
        requireConfirmation(confirmation);
        TelegramUserEntity user = userRepository.findById(userId).orElseThrow();
        memoryService.archiveForUser(userId);
        deleteIfAvailable(integrationSettingsRepository, repository -> repository.deleteByTelegramUser(user));
        deleteIfAvailable(reportArchiveRepository, repository -> repository.deleteByTelegramUser(user));
        deleteIfAvailable(weeklyFocusRepository, repository -> repository.deleteByTelegramUser(user));
        deleteIfAvailable(routinePreferencesRepository, repository -> repository.deleteByTelegramUser(user));
        deleteIfAvailable(conversationStateRepository, repository -> repository.deleteByTelegramUser(user));
        deleteIfAvailable(messageRepository, repository -> repository.deleteByTelegramUser(user));
        reflectionRepository.deleteByTelegramUser(user);
        habitCheckRepository.deleteByTelegramUser(user);
        checkInRepository.deleteByTelegramUser(user);
        lifeProfileRepository.deleteByTelegramUser(user);
        userRepository.delete(user);
    }

    private void requireConfirmation(String confirmation) {
        if (!"DELETE".equals(confirmation)) {
            throw new IllegalArgumentException("confirmation_required");
        }
    }

    private String checkInsJson(TelegramUserEntity user) {
        return checkInRepository.findByTelegramUserOrderByCreatedAtDesc(user).stream()
                .map(checkIn -> """
                        {"createdAt":"%s","energy":%s,"focus":%s,"stress":%s,"sleep":%s,"mood":%s,"overload":%s,"pain":%s,"priority":"%s","notes":"%s"}
                        """.formatted(
                        checkIn.getCreatedAt(),
                        number(checkIn.getEnergy()),
                        number(checkIn.getFocus()),
                        number(checkIn.getStress()),
                        number(checkIn.getSleepQuality()),
                        number(checkIn.getMood()),
                        checkIn.isOverloadFlag(),
                        checkIn.isPainFlag(),
                        escape(checkIn.getMainPriority()),
                        escape(checkIn.getNotes())
                ).strip())
                .reduce((left, right) -> left + "," + right)
                .map(value -> "[" + value + "]")
                .orElse("[]");
    }

    private String habitsJson(TelegramUserEntity user) {
        return habitCheckRepository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, java.time.Instant.EPOCH).stream()
                .map(habit -> """
                        {"name":"%s","minimum":"%s","completed":%s}
                        """.formatted(escape(habit.getHabitName()), escape(habit.getMinimumVersion()), habit.isCompleted()).strip())
                .reduce((left, right) -> left + "," + right)
                .map(value -> "[" + value + "]")
                .orElse("[]");
    }

    private String reflectionsJson(TelegramUserEntity user) {
        return reflectionRepository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, java.time.Instant.EPOCH).stream()
                .map(reflection -> """
                        {"result":"%s","blocker":"%s","tomorrowFocus":"%s"}
                        """.formatted(escape(reflection.getMainResult()), escape(reflection.getMainBlocker()), escape(reflection.getTomorrowFocus())).strip())
                .reduce((left, right) -> left + "," + right)
                .map(value -> "[" + value + "]")
                .orElse("[]");
    }

    private String memoryJson(UUID userId) {
        return memoryService.findRecent(userId, 100).stream()
                .map(memory -> """
                        {"agent":"%s","type":"%s","scope":"%s","title":"%s","content":"%s","confidence":"%s"}
                        """.formatted(memory.agentType(), memory.type(), memory.scope(), escape(memory.title()), escape(memory.content()), memory.confidence()).strip())
                .reduce((left, right) -> left + "," + right)
                .map(value -> "[" + value + "]")
                .orElse("[]");
    }

    private String number(Integer value) {
        return value == null ? "null" : value.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private <T> void deleteIfAvailable(ObjectProvider<T> provider, java.util.function.Consumer<T> deleteAction) {
        T repository = provider == null ? null : provider.getIfAvailable();
        if (repository != null) {
            deleteAction.accept(repository);
        }
    }
}
