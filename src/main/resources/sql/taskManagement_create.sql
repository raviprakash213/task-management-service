CREATE TABLE IF NOT EXISTS public.taskmanagement
(
    id bigint NOT NULL DEFAULT nextval('taskmanagement_id_seq'::regclass),
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    payload text COLLATE pg_catalog."default" NOT NULL,
    status character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT taskmanagement_pkey PRIMARY KEY (id)
)