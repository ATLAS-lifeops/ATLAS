package com.example.atlas.checkin.service;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConditionalOnBean(CheckInRepository.class)
public class CheckInPersistenceService {

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
            "(energy|fatigue|focus|sleep|stress|mood|энергия|усталость|фокус|сон|стресс|настроение)\\s*[:=]?\\s*(10|[1-9])",
            Pattern.CASE_INSENSITIVE
    );

    private final CheckInRepository repository;
    private final Clock clock;

    public CheckInPersistenceService(CheckInRepository repository) {
        this(repository, Clock.systemUTC());
    }

    CheckInPersistenceService(CheckInRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public CheckInEntity record(TelegramUserEntity user, String text) {
        ParsedCheckIn parsed = parse(text);
        return repository.save(CheckInEntity.create(
                user,
                parsed.energy(),
                parsed.fatigue(),
                parsed.focus(),
                parsed.sleepQuality(),
                parsed.stress(),
                parsed.mood(),
                parsed.mainPriority(),
                parsed.overloadFlag(),
                parsed.painFlag(),
                text,
                Instant.now(clock)
        ));
    }

    public ParsedCheckIn parse(String text) {
        String value = text == null ? "" : text;
        Integer energy = null;
        Integer fatigue = null;
        Integer focus = null;
        Integer sleepQuality = null;
        Integer stress = null;
        Integer mood = null;

        Matcher matcher = KEY_VALUE_PATTERN.matcher(value);
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase(Locale.ROOT);
            Integer number = Integer.valueOf(matcher.group(2));
            if (key.equals("energy") || key.equals("энергия")) {
                energy = number;
            } else if (key.equals("fatigue") || key.equals("усталость")) {
                fatigue = number;
            } else if (key.equals("focus") || key.equals("фокус")) {
                focus = number;
            } else if (key.equals("sleep") || key.equals("сон")) {
                sleepQuality = number;
            } else if (key.equals("stress") || key.equals("стресс")) {
                stress = number;
            } else if (key.equals("mood") || key.equals("настроение")) {
                mood = number;
            }
        }

        String lower = value.toLowerCase(Locale.ROOT);
        boolean painFlag = lower.contains("pain") || lower.contains("боль") || lower.contains("болит");
        boolean overloadFlag = lower.contains("overload")
                || lower.contains("перегруз")
                || lower.contains("тревож")
                || lower.contains("worrying symptom");
        return new ParsedCheckIn(energy, fatigue, focus, sleepQuality, stress, mood, null, overloadFlag, painFlag);
    }

    public CheckInEntity recordFlow(
            TelegramUserEntity user,
            Integer energy,
            Integer focus,
            Integer stress,
            Integer sleepQuality,
            Integer mood,
            String mainPriority,
            boolean overloadFlag,
            boolean painFlag,
            String notes
    ) {
        return repository.save(CheckInEntity.create(
                user,
                energy,
                null,
                focus,
                sleepQuality,
                stress,
                mood,
                mainPriority,
                overloadFlag,
                painFlag,
                notes,
                Instant.now(clock)
        ));
    }

    public record ParsedCheckIn(
            Integer energy,
            Integer fatigue,
            Integer focus,
            Integer sleepQuality,
            Integer stress,
            Integer mood,
            String mainPriority,
            boolean overloadFlag,
            boolean painFlag
    ) {
    }
}
