CREATE TABLE processed_events (
    id             BIGSERIAL PRIMARY KEY,
    topic          VARCHAR(128) NOT NULL,
    event_key      VARCHAR(256) NOT NULL,
    processed_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_processed_events ON processed_events(topic, event_key);
