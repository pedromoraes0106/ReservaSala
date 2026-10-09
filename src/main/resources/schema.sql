CREATE TABLE IF NOT EXISTS app_user (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    lastname TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS sala (
    id TEXT PRIMARY KEY,
    nome TEXT NOT NULL UNIQUE,
    capacidade INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS reserva (
    id TEXT PRIMARY KEY,
    sala_id TEXT NOT NULL,
    solicitante TEXT NOT NULL,
    inicio TEXT NOT NULL,
    fim TEXT NOT NULL,
    status TEXT NOT NULL,
    FOREIGN KEY (sala_id) REFERENCES sala (id)
);

CREATE TABLE IF NOT EXISTS reserva_participante (
    reserva_id TEXT NOT NULL,
    nome TEXT NOT NULL,
    PRIMARY KEY (reserva_id, nome),
    FOREIGN KEY (reserva_id) REFERENCES reserva (id) ON DELETE CASCADE
);
