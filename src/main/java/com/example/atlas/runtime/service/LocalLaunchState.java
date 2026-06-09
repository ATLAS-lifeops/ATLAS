package com.example.atlas.runtime.service;

public enum LocalLaunchState {
    SETUP_REQUIRED,
    SETUP_ERROR,
    TELEGRAM_DISABLED,
    TELEGRAM_POLLING_ACTIVE,
    TELEGRAM_WEBHOOK_ACTIVE,
    READY
}
