CREATE TABLE IF NOT EXISTS words (
    word VARCHAR(255) NOT NULL,
    uses INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (word),
    UNIQUE (word)
);
