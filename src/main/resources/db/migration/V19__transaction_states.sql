ALTER TABLE transactions ALTER COLUMN bank_account_issuer_id DROP NOT NULL;
ALTER TABLE transactions ALTER COLUMN bank_account_receiver_id DROP NOT NULL;

CREATE TABLE transaction_states
(
    id             INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description    VARCHAR(255),
    create_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    transaction_id INTEGER NOT NULL,
    state_type_id  INTEGER NOT NULL,
    CONSTRAINT pk_transaction_states PRIMARY KEY (id)
);

ALTER TABLE transaction_states
    ADD CONSTRAINT FK_TRANSACTION_STATES_ON_STATE_TYPE FOREIGN KEY (state_type_id) REFERENCES state_types (id);

ALTER TABLE transaction_states
    ADD CONSTRAINT FK_TRANSACTION_STATES_ON_TRANSACTION FOREIGN KEY (transaction_id) REFERENCES transactions (id);