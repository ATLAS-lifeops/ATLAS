create table telegram_users (
    id uuid primary key,
    telegram_user_id bigint not null unique,
    chat_id bigint not null,
    username varchar(255),
    first_name varchar(255),
    created_at timestamptz not null,
    last_seen_at timestamptz not null
);

create table telegram_messages (
    id uuid primary key,
    telegram_user_id uuid references telegram_users(id),
    chat_id bigint not null,
    direction varchar(32) not null,
    request_type varchar(64),
    text text,
    created_at timestamptz not null
);

create table check_ins (
    id uuid primary key,
    telegram_user_id uuid references telegram_users(id),
    energy integer,
    fatigue integer,
    sleep_quality integer,
    stress integer,
    pain_flag boolean not null default false,
    notes text,
    created_at timestamptz not null
);

create table atlas_runtime_settings (
    id uuid primary key,
    telegram_bot_token text,
    telegram_bot_username varchar(255),
    telegram_mode varchar(32),
    telegram_public_base_url text,
    telegram_webhook_secret text,
    telegram_polling_offset bigint not null default 0,
    setup_completed boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_telegram_messages_telegram_user_id on telegram_messages(telegram_user_id);
create index idx_telegram_messages_created_at on telegram_messages(created_at);
create index idx_check_ins_telegram_user_id on check_ins(telegram_user_id);
create index idx_check_ins_created_at on check_ins(created_at);
