CREATE SCHEMA if NOT EXISTS chat;

CREATE TABLE if NOT EXISTS chat.users (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
);

CREATE TABLE if NOT EXISTS chat.rooms (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    owner INT REFERENCES chat.users(id) NOT NULL
);

CREATE TABLE IF NOT EXISTS chat.messages (
    id SERIAL PRIMARY KEY,
    sender INT REFERENCES chat.users(id),
    room INT REFERENCES chat.rooms(id),
    text TEXT,
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);