CREATE TABLE outbox (
    id             BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(64)  NOT NULL,
    aggregate_id   VARCHAR(64)  NOT NULL,
    event_type     VARCHAR(128) NOT NULL,
    payload        JSONB        NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   TIMESTAMP    NULL
);

CREATE INDEX idx_outbox_published_at ON outbox(published_at) WHERE published_at IS NULL;
CREATE INDEX idx_outbox_created_at ON outbox(created_at);

CREATE TABLE processed_events (
    id             BIGSERIAL PRIMARY KEY,
    topic          VARCHAR(128) NOT NULL,
    event_key      VARCHAR(256) NOT NULL,
    processed_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_processed_events ON processed_events(topic, event_key);
