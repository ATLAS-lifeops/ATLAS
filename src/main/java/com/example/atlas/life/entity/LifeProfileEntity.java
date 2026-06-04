package com.example.atlas.life.entity;

import com.example.atlas.life.LifeArea;
import com.example.atlas.life.PlanningStyle;
import com.example.atlas.user.entity.TelegramUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "life_profiles")
public class LifeProfileEntity {

    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_life_area", length = 64)
    private LifeArea primaryLifeArea;

    @Column(name = "current_focus", columnDefinition = "text")
    private String currentFocus;

    @Enumerated(EnumType.STRING)
    @Column(name = "planning_style", length = 64)
    private PlanningStyle planningStyle;

    @Column(name = "preferred_checkin_time", length = 32)
    private String preferredCheckinTime;

    @Column(name = "preferred_reflection_time", length = 32)
    private String preferredReflectionTime;

    @Column(length = 128)
    private String timezone;

    @Column(name = "sleep_focus", nullable = false)
    private boolean sleepFocus;

    @Column(name = "movement_focus", nullable = false)
    private boolean movementFocus;

    @Column(name = "nutrition_focus", nullable = false)
    private boolean nutritionFocus;

    @Column(name = "habit_focus", nullable = false)
    private boolean habitFocus;

    @Column(name = "stress_focus", nullable = false)
    private boolean stressFocus;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LifeProfileEntity() {
    }

    public LifeProfileEntity(UUID id, TelegramUserEntity telegramUser, Instant now) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.planningStyle = PlanningStyle.BALANCED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static LifeProfileEntity create(TelegramUserEntity telegramUser, Instant now) {
        return new LifeProfileEntity(UUID.randomUUID(), telegramUser, now);
    }

    public void updatePrimaryLifeArea(LifeArea primaryLifeArea, Instant now) {
        this.primaryLifeArea = primaryLifeArea;
        touch(now);
    }

    public void updateCurrentFocus(String currentFocus, Instant now) {
        this.currentFocus = currentFocus;
        touch(now);
    }

    public void updatePlanningStyle(PlanningStyle planningStyle, Instant now) {
        this.planningStyle = planningStyle;
        touch(now);
    }

    public void updateLifeLoops(
            boolean sleepFocus,
            boolean stressFocus,
            boolean habitFocus,
            boolean nutritionFocus,
            boolean movementFocus,
            boolean focusTasks,
            Instant now
    ) {
        this.sleepFocus = sleepFocus;
        this.stressFocus = stressFocus;
        this.habitFocus = habitFocus;
        this.nutritionFocus = nutritionFocus;
        this.movementFocus = movementFocus;
        this.notes = focusTasks ? appendNote(notes, "Focus and tasks") : notes;
        touch(now);
    }

    public void completeOnboarding(Instant now) {
        this.onboardingCompleted = true;
        touch(now);
    }

    private String appendNote(String existing, String note) {
        if (existing == null || existing.isBlank()) {
            return note;
        }
        if (existing.contains(note)) {
            return existing;
        }
        return existing + "; " + note;
    }

    private void touch(Instant now) {
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public LifeArea getPrimaryLifeArea() {
        return primaryLifeArea;
    }

    public String getCurrentFocus() {
        return currentFocus;
    }

    public PlanningStyle getPlanningStyle() {
        return planningStyle;
    }

    public boolean isSleepFocus() {
        return sleepFocus;
    }

    public boolean isMovementFocus() {
        return movementFocus;
    }

    public boolean isNutritionFocus() {
        return nutritionFocus;
    }

    public boolean isHabitFocus() {
        return habitFocus;
    }

    public boolean isStressFocus() {
        return stressFocus;
    }

    public boolean isFocusTasks() {
        return notes != null && notes.contains("Focus and tasks");
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }
}
