CREATE ROLE collect WITH PASSWORD 'collect123';
CREATE DATABASE collect OWNER collect;
\connect collect
CREATE SCHEMA collect AUTHORIZATION collect;
