package com.example.atlas.privacy;

import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.habit.repository.HabitCheckRepository;
import com.example.atlas.life.repository.LifeProfileRepository;
import com.example.atlas.memory.PersistentAgentMemoryService;
import com.example.atlas.reflection.repository.EveningReflectionRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.repository.TelegramUserRepository;
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

    public PrivacyService(
            TelegramUserRepository userRepository,
            LifeProfileRepository lifeProfileRepository,
            CheckInRepository checkInRepository,
            HabitCheckRepository habitCheckRepository,
            EveningReflectionRepository reflectionRepository,
            PersistentAgentMemoryService memoryService
    ) {
        this.userRepository = userRepository;
        this.lifeProfileRepository = lifeProfileRepository;
        this.checkInRepository = checkInRepository;
        this.habitCheckRepository = habitCheckRepository;
        this.reflectionRepository = reflectionRepository;
        this.memoryService = memoryService;
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
        String json = """
                {"userId":"%s","profileCount":%d,"checkInCount":%d,"habitCount":%d,"reflectionCount":%d,"memoryCount":%d}
                """.formatted(userId, panel.profileCount(), panel.checkInCount(), panel.habitCount(), panel.reflectionCount(), panel.memoryCount()).strip();
        String markdown = """
                # ATLAS export

                User: %s
                Profile records: %d
                Check-ins: %d
                Habits: %d
                Reflections: %d
                Memory records: %d
                """.formatted(userId, panel.profileCount(), panel.checkInCount(), panel.habitCount(), panel.reflectionCount(), panel.memoryCount());
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
}
