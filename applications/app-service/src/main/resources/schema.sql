CREATE TABLE IF NOT EXISTS productos (
   id          UUID             PRIMARY KEY  ,
   name        VARCHAR(255)     NOT NULL UNIQUE,
   description TEXT,
   price       DOUBLE PRECISION CHECK (price >= 0)
);