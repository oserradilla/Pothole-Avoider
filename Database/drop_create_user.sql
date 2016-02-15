/*DROP ROLE potholeavoider;*/

CREATE USER potholeavoider WITH ENCRYPTED PASSWORD 'ritDUG7QIsQ3IDtUcgdXnvXJt0CHgf5I';
GRANT USAGE ON SCHEMA public to potholeavoider;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO potholeavoider;

GRANT CONNECT ON DATABASE potholeavoider to potholeavoider;
\c potholeavoider
GRANT USAGE ON SCHEMA public to potholeavoider; 
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL SEQUENCES IN SCHEMA public TO potholeavoider;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO potholeavoider;