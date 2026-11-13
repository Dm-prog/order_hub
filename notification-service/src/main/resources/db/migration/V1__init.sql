CREATE TABLE notifications (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT      NOT NULL,
    event_type     VARCHAR(64) NOT NULL,
    recipient      VARCHAR(255),
    content        TEXT,
    status         VARCHAR(32) NOT NULL DEFAULT 'SENT',
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_event_type ON notifications(event_type);
