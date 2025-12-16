-- schema.sql
-- Kör: CREATE DATABASE personalfinance; CONNECT TO personalfinance; (eller använd pgAdmin)

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Exempel (VG-kravet): SELECT med JOIN
-- SELECT t.*, u.username FROM transactions t
-- JOIN users u ON t.user_id = u.id;
