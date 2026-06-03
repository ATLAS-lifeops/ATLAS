create table life_profiles (
    id uuid primary key,
    telegram_user_id uuid not null unique references telegram_users(id),
    primary_life_area varchar(64),
    current_focus text,
    planning_style varchar(64),
    preferred_checkin_time varchar(32),
    preferred_reflection_time varchar(32),
    timezone varchar(128),
    sleep_focus boolean not null default false,
    movement_focus boolean not null default false,
    nutrition_focus boolean not null default false,
    habit_focus boolean not null default false,
    stress_focus boolean not null default false,
    notes text,
    onboarding_completed boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table conversation_states (
    id uuid primary key,
    telegram_user_id uuid not null references telegram_users(id),
    flow_type varchar(64) not null,
    step varchar(128) not null,
    status varchar(32) not null,
    payload_json text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    completed_at timestamptz
);

create unique index ux_conversation_states_active_user
    on conversation_states(telegram_user_id)
    where status = 'ACTIVE';

alter table check_ins
    add column focus integer,
    add column mood integer,
    add column main_priority text,
    add column overload_flag boolean not null default false;

create table habit_checks (
    id uuid primary key,
    telegram_user_id uuid references telegram_users(id),
    habit_name text not null,
    minimum_version text,
    completed boolean not null default false,
    notes text,
    created_at timestamptz not null
);

create table evening_reflections (
    id uuid primary key,
    telegram_user_id uuid references telegram_users(id),
    main_result text,
    main_blocker text,
    tomorrow_focus text,
    created_at timestamptz not null
);

create index idx_life_profiles_telegram_user_id on life_profiles(telegram_user_id);
create index idx_conversation_states_telegram_user_id on conversation_states(telegram_user_id);
create index idx_conversation_states_status on conversation_states(status);
create index idx_habit_checks_telegram_user_id_created_at on habit_checks(telegram_user_id, created_at desc);
create index idx_evening_reflections_telegram_user_id_created_at on evening_reflections(telegram_user_id, created_at desc);
