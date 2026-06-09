create table integration_settings (
    id uuid primary key,
    telegram_user_id uuid not null references telegram_users(id),
    integration_type varchar(64) not null,
    status varchar(32) not null,
    safe_metadata_json text not null,
    updated_at timestamptz not null
);

create unique index ux_integration_settings_user_type on integration_settings(telegram_user_id, integration_type);
