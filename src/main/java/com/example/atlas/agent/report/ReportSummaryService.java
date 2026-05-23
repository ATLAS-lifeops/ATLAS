package com.example.atlas.agent.report;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.message.entity.TelegramMessageDirection;
import com.example.atlas.message.repository.TelegramMessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;

@Service
@ConditionalOnBean({CheckInRepository.class, TelegramMessageRepository.class})
public class ReportSummaryService {

    private final CheckInRepository checkInRepository;
    private final TelegramMessageRepository messageRepository;
    private final Clock clock;

    public ReportSummaryService(CheckInRepository checkInRepository, TelegramMessageRepository messageRepository) {
        this(checkInRepository, messageRepository, Clock.systemUTC());
    }

    ReportSummaryService(
            CheckInRepository checkInRepository,
            TelegramMessageRepository messageRepository,
            Clock clock
    ) {
        this.checkInRepository = checkInRepository;
        this.messageRepository = messageRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReportSummary weeklySummary() {
        Instant since = Instant.now(clock).minus(7, ChronoUnit.DAYS);
        long checkIns = checkInRepository.countByCreatedAtAfter(since);
        long inboundMessages = messageRepository.countByDirectionAndCreatedAtAfter(TelegramMessageDirection.INBOUND, since);
        List<CheckInEntity> recentCheckIns = checkInRepository.findTop20ByOrderByCreatedAtDesc();

        return new ReportSummary(
                checkIns,
                inboundMessages,
                average(recentCheckIns.stream().map(CheckInEntity::getEnergy).toList()),
                average(recentCheckIns.stream().map(CheckInEntity::getFatigue).toList()),
                recentCheckIns.stream().anyMatch(CheckInEntity::isPainFlag)
        );
    }

    private Double average(List<Integer> values) {
        OptionalDouble average = values.stream()
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average();
        return average.isPresent() ? average.getAsDouble() : null;
    }

    public record ReportSummary(
            long checkIns,
            long inboundMessages,
            Double averageEnergy,
            Double averageFatigue,
            boolean painMentioned
    ) {

        boolean hasData() {
            return checkIns > 0 || inboundMessages > 0;
        }
    }
}
