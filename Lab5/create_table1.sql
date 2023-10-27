CREATE TABLE test_table (
    name VARCHAR(50),
    code INT,
    flag BOOLEAN,
    time TIME,
    value INT,
    PRIMARY KEY (name,code, time)
);