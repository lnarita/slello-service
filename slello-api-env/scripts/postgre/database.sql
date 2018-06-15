CREATE SCHEMA AUTHORIZATION slello;

CREATE TABLE slello.visibility (
      id SERIAL PRIMARY KEY,
      name VARCHAR(30) NOT NULL
);

INSERT INTO slello.visibility(name) VALUES ('OPEN');
INSERT INTO slello.visibility(name) VALUES ('CLOSED');
INSERT INTO slello.visibility(name) VALUES ('SECRET');

CREATE TABLE slello.authority (
      id SERIAL PRIMARY KEY,
      name VARCHAR(30) NOT NULL
);

INSERT INTO slello.authority(name) VALUES ('EXT');
INSERT INTO slello.authority(name) VALUES ('USR');
INSERT INTO slello.authority(name) VALUES ('ADM');

CREATE TABLE slello.account (
      username VARCHAR(20) PRIMARY KEY,
      email VARCHAR(60) NOT NULL,
      password VARCHAR(128) NOT NULL,
      authority INT NOT NULL REFERENCES authority(id) DEFAULT(1),
      enabled BOOLEAN NOT NULL DEFAULT(TRUE)
);

INSERT INTO slello.account(username, email, password, authority, enabled) VALUES ('rasmus.lerdorf', 'rasmus.lerdorf@php.mito', 'php4life', 1, true);
INSERT INTO slello.account(username, email, password, authority, enabled) VALUES ('admin', 'admin@admin.com', 'secret', 3, true);
UPDATE slello.account SET password = '$2a$10$0.V/SZJHoTVQEgD1CGXTtuSou5UMhtcziaqkz5mPT8kq7TAF4m.x6' WHERE username = 'admin';

-- INSERT into slello.account(username, email, password, authority, enabled) VALUES ('lnarita', 'l182851@g.unicamp.br', 'atiranl', 3, true);
-- INSERT into slello.account(username, email, password, authority, enabled) VALUES ('jovit', 'joao.goncalves123456789@gmail.com', 'tivoj', 2, true);

CREATE TABLE slello.community(
      id SERIAL PRIMARY KEY,
      name VARCHAR(60) NOT NULL,
      uri VARCHAR (20) UNIQUE,
      visibility INT REFERENCES slello.visibility(id) DEFAULT (1),
      read_only BOOLEAN NOT NULL DEFAULT(FALSE)
);
CREATE UNIQUE INDEX community_uri_idx ON slello.community (LOWER(uri));

INSERT INTO slello.community(name, uri, visibility, read_only) VALUES ('Software Engineers', 'fy-mc426', 1, FALSE);

CREATE TABLE slello.community_member(
      community_id INT NOT NULL REFERENCES community(id),
      username VARCHAR(20) NOT NULL REFERENCES account(username),
      joined_on TIMESTAMP NOT NULL DEFAULT NOW(),
      PRIMARY KEY (community_id, username)
);
CREATE INDEX community_member_joined_idx ON slello.community_member(joined_on DESC);

INSERT INTO slello.community_member(community_id, username, joined_on) VALUES (1, 'rasmus.lerdorf', NOW());
INSERT INTO slello.community_member(community_id, username, joined_on) VALUES (1, 'admin', NOW());

CREATE TABLE slello.topic(
      id SERIAL PRIMARY KEY,
      community INT NOT NULL REFERENCES community(id),
      headline VARCHAR(120) NOT NULL,
      posted_at TIMESTAMP NOT NULL,
      description TEXT,
      op VARCHAR(20) NOT NULL REFERENCES account(username),
      votes INT NOT NULL DEFAULT(0),
      read_only BOOLEAN NOT NULL DEFAULT(FALSE)
);
CREATE INDEX topic_op_idx ON slello.topic (op);
CREATE INDEX topic_posted_at_idx ON slello.topic (posted_at DESC);
CREATE INDEX topic_votes_idx ON slello.topic (votes);

INSERT INTO slello.topic(community, headline, posted_at, description, op, votes) VALUES (1, 'What do you think about PHP and why is it the best PL ever?', NOW(), 'PHP is the BEST PL ever', 'rasmus.lerdorf', 999);

CREATE TABLE slello.comment(
      id SERIAL PRIMARY KEY,
      topic INT NOT NULL REFERENCES topic(id),
      parent INT REFERENCES comment(id) DEFAULT(NULL),
      body TEXT NOT NULL,
      posted_at TIMESTAMP NOT NULL,
      username VARCHAR(20) NOT NULL REFERENCES account(username),
      votes INT NOT NULL DEFAULT(0),
      deleted BOOLEAN NOT NULL DEFAULT(FALSE)
);
CREATE INDEX comment_user_idx ON slello.comment (username);
CREATE INDEX comment_posted_at_idx ON slello.comment (posted_at DESC);
CREATE INDEX comment_votes_idx ON slello.comment (votes);
CREATE INDEX comment_parent_idx ON slello.comment (parent);
CREATE INDEX comment_topic_idx ON slello.comment (topic);

INSERT INTO slello.comment(topic, parent, body, posted_at, username, votes) VALUES (1, NULL, 'FUCK YOU', NOW(), 'admin', 9999);
INSERT INTO slello.comment(topic, parent, body, posted_at, username, votes) VALUES (1, 1, 'well, no need to be rude', NOW(), 'rasmus.lerdorf', -12);