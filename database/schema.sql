create table if not exists users (
	user_id serial primary key,
	name text not null
);

drop type if exists pass_type;
create type pass_type as enum ('enter', 'exit');

create table if not exists passes (
	event_id serial primary key,
	user_id int not null references users(user_id),
	pass_time timestamp not null,
	pass_type pass_type not null
);

create table if not exists renews (
	event_id serial primary key,
	user_id int not null references users(user_id),
	valid_until timestamp
);
