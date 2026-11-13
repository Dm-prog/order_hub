CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    quantity    INTEGER      NOT NULL CHECK (quantity >= 0),
    reserved    INTEGER      NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_sku ON products(sku);

CREATE TABLE inventory_reservations (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT     NOT NULL,
    product_sku    VARCHAR(64) NOT NULL,
    quantity       INTEGER    NOT NULL CHECK (quantity > 0),
    status         VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_reservations_order_id ON inventory_reservations(order_id);
