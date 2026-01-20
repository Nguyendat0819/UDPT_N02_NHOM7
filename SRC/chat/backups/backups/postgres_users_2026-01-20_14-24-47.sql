--
-- PostgreSQL database dump
--

\restrict Eo1BJiHVR6ndbPwaswXPt1AOoi43vur8x1ZrqbUeS0BEuxcr3L6MO5kjV7XH7ed

-- Dumped from database version 15.15 (Debian 15.15-1.pgdg13+1)
-- Dumped by pg_dump version 15.15 (Debian 15.15-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: friendships; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.friendships (
    target_user_id uuid NOT NULL,
    user_id uuid NOT NULL,
    status character varying(255) NOT NULL
);


ALTER TABLE public.friendships OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    avatar_url character varying(255),
    created_at timestamp(6) without time zone,
    email character varying(255),
    password character varying(255),
    username character varying(255),
    version bigint
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: friendships; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.friendships (target_user_id, user_id, status) FROM stdin;
48c22e5a-11bc-4949-a6f0-0e182bf56428	05d76771-994b-41ca-b2ef-bb84006821e4	bạn
05d76771-994b-41ca-b2ef-bb84006821e4	48c22e5a-11bc-4949-a6f0-0e182bf56428	bạn
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, avatar_url, created_at, email, password, username, version) FROM stdin;
a826c067-783a-4b82-b319-41e0ef93fd06	/uploads/ae2dabc8-0f62-48e1-b9ff-3b7e3981e908_Gemini_Generated_Image_bxv0izbxv0izbxv0.png	2026-01-17 10:03:21.424716	nguyendinhdat801@gmail.com	$2a$10$Zs0iS2PSIDo9J6uswybqxe6rNwXaxGiMb1iwdKo5XseRih67njpP6	boikill09	\N
48c22e5a-11bc-4949-a6f0-0e182bf56428	/uploads/2e4cdbe6-7a08-4bf6-919a-e00c89c06062_Gemini_Generated_Image_q4ke0qq4ke0qq4ke.png	2026-01-17 21:29:04.128097	23010282@gmail.com	$2a$10$m5j5mhrndf02bvT9XnGfY.Taig.u31t9KzfU3.RjtPSq0.Ge6g3aa	Mex	\N
05d76771-994b-41ca-b2ef-bb84006821e4	4a6fa174-48ad-4c0c-9d04-e5b07656768c_Đầm tay dài, Dáng ôm 25ADKE097F.jpg	2026-01-17 23:44:41.850898	admin809@gmail.com	$2a$10$Fwmg3J5EX5HOxWCCeQbCJ.JEyKzzH5LGXp9VimvY8AqrNQRq0Pk4q	boikill10	\N
\.


--
-- Name: friendships friendships_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendships
    ADD CONSTRAINT friendships_pkey PRIMARY KEY (target_user_id, user_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict Eo1BJiHVR6ndbPwaswXPt1AOoi43vur8x1ZrqbUeS0BEuxcr3L6MO5kjV7XH7ed

