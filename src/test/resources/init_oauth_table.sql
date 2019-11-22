CREATE TABLE oauth_access_token
(
    token_id          varchar(255) DEFAULT NULL,
    token             mediumblob,
    authentication_id varchar(255) NOT NULL,
    user_name         varchar(255) DEFAULT NULL,
    client_id         varchar(255) DEFAULT NULL,
    authentication    mediumblob,
    refresh_token     varchar(255) DEFAULT NULL,
    PRIMARY KEY (authentication_id)
);
CREATE TABLE oauth_refresh_token
(
    token_id       varchar(255) DEFAULT NULL,
    token          mediumblob,
    authentication mediumblob
);