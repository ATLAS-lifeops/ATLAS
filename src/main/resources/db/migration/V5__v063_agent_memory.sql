create table agent_memory_records (
    id uuid primary key,
    telegram_user_id uuid not null references telegram_users(id),
    internal_user_id uuid not null,
    agent_type varchar(64) not null,
    memory_type varchar(64) not null,
    memory_scope varchar(64) not null,
    title text,
    content text not null,
    confidence varchar(32) not null,
    tags text,
    source varchar(64) not null,
    deduplication_key text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    expires_at timestamptz,
    archived boolean not null default false
);

create index idx_agent_memory_user_updated on agent_memory_records(internal_user_id, updated_at desc);
create index idx_agent_memory_user_agent on agent_memory_records(internal_user_id, agent_type, updated_at desc);
create index idx_agent_memory_user_scope on agent_memory_records(internal_user_id, memory_scope, updated_at desc);
create unique index ux_agent_memory_active_dedup
    on agent_memory_records(internal_user_id, deduplication_key)
    where archived = false;
