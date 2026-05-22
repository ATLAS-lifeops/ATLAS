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
            "(energy|fatigue|sleep|stress|энергия|усталость|сон|стресс)\\s*[:=]?\\s*(10|[1-9])",
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
                parsed.sleepQuality(),
                parsed.stress(),
                parsed.painFlag(),
                text,
                Instant.now(clock)
        ));
    }

    ParsedCheckIn parse(String text) {
        String value = text == null ? "" : text;
        Integer energy = null;
        Integer fatigue = null;
        Integer sleepQuality = null;
        Integer stress = null;

        Matcher matcher = KEY_VALUE_PATTERN.matcher(value);
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase(Locale.ROOT);
            Integer number = Integer.valueOf(matcher.group(2));
            if (key.equals("energy") || key.equals("энергия")) {
                energy = number;
            } else if (key.equals("fatigue") || key.equals("усталость")) {
                fatigue = number;
            } else if (key.equals("sleep") || key.equals("сон")) {
                sleepQuality = number;
            } else if (key.equals("stress") || key.equals("стресс")) {
                stress = number;
            }
        }

        String lower = value.toLowerCase(Locale.ROOT);
        boolean painFlag = lower.contains("pain") || lower.contains("боль") || lower.contains("болит");
        return new ParsedCheckIn(energy, fatigue, sleepQuality, stress, painFlag);
    }

    record ParsedCheckIn(Integer energy, Integer fatigue, Integer sleepQuality, Integer stress, boolean painFlag) {
    }
}
