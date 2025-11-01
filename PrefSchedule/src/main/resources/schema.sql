CREATE TABLE IF NOT EXISTS students (
                                        id BIGSERIAL PRIMARY KEY,
                                        code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    year INT NOT NULL
    );

CREATE TABLE IF NOT EXISTS instructors (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(255) NOT NULL,
    email VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS packs (
                                     id BIGSERIAL PRIMARY KEY,
                                     year INT NOT NULL,
                                     semester INT NOT NULL,
                                     name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS courses (
                                       id BIGSERIAL PRIMARY KEY,
                                       type VARCHAR(50) NOT NULL,
    code VARCHAR(50),
    abbr VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    instructor_id BIGINT REFERENCES instructors(id),
    pack_id BIGINT REFERENCES packs(id),
    group_count INT DEFAULT 1,
    description TEXT
    );
