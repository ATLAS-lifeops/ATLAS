create table routine_preferences (
    id uuid primary key,
    telegram_user_id uuid not null unique references telegram_users(id),
    checkin_time varchar(16) not null,
    evening_time varchar(16) not null,
    timezone varchar(128) not null,
    quiet_hours_start varchar(16) not null,
    quiet_hours_end varchar(16) not null,
    enabled boolean not null default false,
    updated_at timestamptz not null
);

create table weekly_focuses (
    id uuid primary key,
    telegram_user_id uuid not null references telegram_users(id),
    week_start date not null,
    focus text not null,
    created_at timestamptz not null
);

create unique index ux_weekly_focus_user_week on weekly_focuses(telegram_user_id, week_start);

create table report_archives (
    id uuid primary key,
    telegram_user_id uuid not null references telegram_users(id),
    week_start date not null,
    content text not null,
    created_at timestamptz not null
);

create index idx_report_archives_user_created on report_archives(telegram_user_id, created_at desc);
