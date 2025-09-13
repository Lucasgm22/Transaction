CREATE TABLE transaction (
    id UUID NOT NULL,
    description VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    purchase_amount NUMERIC(19, 2) NOT NULL,
    PRIMARY KEY (id)
);