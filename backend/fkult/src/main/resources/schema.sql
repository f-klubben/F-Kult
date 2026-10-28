PRAGMA foreign_keys = ON;

-- Users
CREATE TABLE IF NOT EXISTS user (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  name       TEXT,
  username   TEXT NOT NULL UNIQUE COLLATE NOCASE,
  is_banned  INTEGER NOT NULL DEFAULT 0 CHECK (is_banned IN (0,1)),
  is_admin   INTEGER NOT NULL DEFAULT 0 CHECK (is_admin  IN (0,1))
);

-- Movies
CREATE TABLE IF NOT EXISTS movie (
  id                  INTEGER PRIMARY KEY AUTOINCREMENT,
  tconst              TEXT UNIQUE,
  movie_name          TEXT NOT NULL CHECK (length(trim(movie_name)) > 0),
  original_movie_name TEXT,
  year                INTEGER,
  runtime_minutes     INTEGER CHECK (runtime_minutes IS NULL OR runtime_minutes > 0),
  is_active           INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0,1)),
  is_series           INTEGER NOT NULL DEFAULT 0 CHECK (is_series IN (0,1)),
  is_shorts           INTEGER NOT NULL DEFAULT 0 CHECK (is_shorts IN (0,1)),
  poster_url          TEXT,
  poster_status       TEXT NOT NULL DEFAULT 'PENDING' CHECK (poster_status IN ('PENDING', 'FOUND', 'NOT_FOUND')),
  poster_checked_at   TEXT,
  rating              TEXT,
  last_seen           DATE DEFAULT NULL
);

-- Themes
CREATE TABLE IF NOT EXISTS theme (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name         TEXT NOT NULL CHECK (length(trim(name)) > 0),
  user_id      INTEGER,
  timestamp    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  vote_count   INTEGER,
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Movies in Themes
CREATE TABLE IF NOT EXISTS theme_movie (
  theme_id  INTEGER NOT NULL,
  movie_id  INTEGER NOT NULL,
  PRIMARY KEY (theme_id, movie_id),
  FOREIGN KEY (theme_id) REFERENCES theme(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (movie_id) REFERENCES movie(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Drinking rules
CREATE TABLE IF NOT EXISTS drinking_rule (
  id        INTEGER PRIMARY KEY AUTOINCREMENT,
  theme_id  INTEGER NOT NULL,
  rule_text TEXT NOT NULL CHECK (length(trim(rule_text)) > 0),
  FOREIGN KEY (theme_id) REFERENCES theme(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Soundsamples
CREATE TABLE IF NOT EXISTS sound_samples (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  link TEXT,
  file_path TEXT,
  user_id INTEGER,
  CHECK (
    (link IS NOT NULL AND file_path IS NULL)
    OR
    (link IS NULL AND file_path IS NOT NULL)
  ),
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
);

--event
CREATE TABLE IF NOT EXISTS event (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  event_date DATETIME,
  theme_id INTEGER,
  FOREIGN KEY (theme_id) REFERENCES theme(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE VIRTUAL TABLE IF NOT EXISTS movie_fts
    USING fts5(movie_name, original_movie_name, content='movie', content_rowid='id');
-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_movie_year            ON movie(year);
CREATE INDEX IF NOT EXISTS idx_movie_name_nocase     ON movie(movie_name COLLATE NOCASE);
CREATE INDEX IF NOT EXISTS idx_theme_name_nocase     ON theme(name COLLATE NOCASE);
CREATE INDEX IF NOT EXISTS idx_theme_user_id         ON theme(user_id);
CREATE INDEX IF NOT EXISTS idx_rule_theme_id         ON drinking_rule(theme_id);
CREATE INDEX IF NOT EXISTS idx_theme_movie_movie_id  ON theme_movie(movie_id);
CREATE INDEX IF NOT EXISTS idx_event_theme_id ON event(theme_id);


