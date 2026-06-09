# Memory

ATLAS memory has two layers:

- Memory write contract: agents can propose user-scoped memories with type, scope, confidence, source and tags.
- Persistent memory: accepted records are stored in PostgreSQL and can optionally be mirrored as runtime Markdown snapshots.

Memory writes are validated before storage. The policy rejects secrets, missing user scope, unsafe medical claims and low-value one-off noise. Retrieval is always scoped by the internal user id.

Markdown snapshots are disabled by default. If enabled, they are written under `ATLAS_MEMORY_SNAPSHOT_PATH` and must stay in ignored runtime storage.

No embeddings or vector search are part of this release line.
