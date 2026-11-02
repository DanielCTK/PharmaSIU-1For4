-- Run this ONCE in the admin connection to an existing PostgreSQL database,
-- e.g. 'postgres'; DO NOT run inside a BEGIN/COMMIT transaction.
-- Then switch Navicat's active connection/database to pharmasiu_demo
-- and run 01_schema.sql through 05_queries.sql in that order.
CREATE DATABASE pharmasiu_demo WITH ENCODING 'UTF8' TEMPLATE template0;
