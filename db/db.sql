-- Criação do banco de dados - Postgres
create database "quarkus-social";

-- Criação da tabela Users
create table "users"(
	id bigserial not null primary key,
	name varchar(100) not null,
	age integer not null
);

-- Criação da tabela Posts
create table "posts"(
	id bigserial not null primary key,
	post_text varchar(150) not null,
	datetime timestamp not null,
	user_id bigint not null references users(id)
);

-- Criação da tabela Followers
create table "followers"(
	id bigserial not null primary key,
	user_id bigint not null references users(id),
	follower_id bigint not null references users(id)
);

-- Criação do banco de dados - MySQL
--create database "quarkus-social";
--
---- Criação da tabela Users
--create table "users"(
--	id autoincrement not null primary key,
--	name varchar(100) not null,
--	age integer not null
--);