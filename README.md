## install postgresql docker
```bash
docker run --name postgres -e POSTGRES_PASSWORD=Postgres$ -p 5432:5432 -d postgres
```

## create user and database
```bash
docker exec -it postgres bash
psql -U postgres
CREATE DATABASE hdemodb;
CREATE USER hdemouser WITH ENCRYPTED PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE hdemodb TO hdemouser;
\q
```
