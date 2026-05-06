create table users (
    id bigserial primary key,
    telegram_user_id bigint not null unique,
    username varchar(128),
    created_at timestamptz not null default now()
);

create table user_profiles (
    user_id bigint primary key references users(id) on delete cascade,
    goal varchar(64),
    timezone varchar(64),
    created_at timestamptz not null default now()
);

create table messages (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    direction varchar(16) not null,
    content text not null,
    request_type varchar(32),
    created_at timestamptz not null default now()
);

create table checkins (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    energy integer,
    fatigue integer,
    notes varchar(500),
    created_at timestamptz not null default now()
);

create table plans (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    plan_type varchar(32) not null,
    content text not null,
    created_at timestamptz not null default now()
);

create index idx_messages_user_id_created_at on messages(user_id, created_at desc);
create index idx_checkins_user_id_created_at on checkins(user_id, created_at desc);
create index idx_plans_user_id_created_at on plans(user_id, created_at desc);
