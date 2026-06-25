--
-- PostgreSQL database dump
--

\restrict xgjador5fRscynxGbRmFzXsiYFmMsDJHzwLwpE91ArzpA9DO41Et5X21SnGLy5C

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_logs (
    id uuid NOT NULL,
    action character varying(50) NOT NULL,
    actor_id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    details text,
    target_id uuid,
    CONSTRAINT audit_logs_action_check CHECK (((action)::text = ANY ((ARRAY['DOCUMENT_UPLOAD'::character varying, 'DOCUMENT_DELETE'::character varying, 'USER_ROLE_CHANGE'::character varying, 'USER_DELETE'::character varying, 'COLLECTION_CREATE'::character varying, 'COLLECTION_DELETE'::character varying])::text[])))
);


ALTER TABLE public.audit_logs OWNER TO postgres;

--
-- Name: collections; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.collections (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    description text,
    name character varying(255) NOT NULL,
    owner_id uuid NOT NULL
);


ALTER TABLE public.collections OWNER TO postgres;

--
-- Name: document_chunks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.document_chunks (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    chunk_index integer NOT NULL,
    content text NOT NULL,
    token_count integer NOT NULL,
    document_id uuid NOT NULL
);


ALTER TABLE public.document_chunks OWNER TO postgres;

--
-- Name: document_tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.document_tags (
    document_id uuid NOT NULL,
    tag_id uuid NOT NULL
);


ALTER TABLE public.document_tags OWNER TO postgres;

--
-- Name: documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.documents (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    description text,
    file_name character varying(255) NOT NULL,
    file_size bigint NOT NULL,
    file_type character varying(255) NOT NULL,
    storage_path character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    visibility character varying(255) NOT NULL,
    collection_id uuid,
    owner_id uuid NOT NULL,
    error_message text,
    status character varying(255) NOT NULL,
    CONSTRAINT documents_visibility_check CHECK (((visibility)::text = ANY ((ARRAY['PRIVATE'::character varying, 'TEAM'::character varying, 'PUBLIC'::character varying])::text[])))
);


ALTER TABLE public.documents OWNER TO postgres;

--
-- Name: tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tags (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    color character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.tags OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    email character varying(320) NOT NULL,
    full_name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(32) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'EMPLOYEE'::character varying, 'VIEWER'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.audit_logs (id, action, actor_id, created_at, details, target_id) FROM stdin;
1726483f-c298-415f-b2a6-569cd81eaa2b	DOCUMENT_UPLOAD	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 16:44:14.396883+05:30	Uploaded document: A Guide to Japanese Grammar - A Japanese Approach to Learning Japanese Grammar (Tae Kim) (z-library.sk, 1lib.sk, z-lib.sk).pdf	6eabad0c-be42-4a27-8090-2f868f39a15b
a943385b-21fe-4cfe-ad40-87e04812258e	DOCUMENT_UPLOAD	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 16:48:42.10745+05:30	Uploaded document: test.txt	b1f92569-2466-4824-8633-118a4978c7c8
e553277e-d29a-470d-bd42-2619c371e2d2	DOCUMENT_UPLOAD	00468d52-14f8-4f9f-8530-6bacf4915ef9	2026-06-20 16:58:25.544057+05:30	Uploaded document: test_doc.txt	b19b4f26-2988-4a0d-aec8-e191b6224fa6
3571faf5-80fe-42bb-8b68-3ad44cc4dd82	DOCUMENT_UPLOAD	00468d52-14f8-4f9f-8530-6bacf4915ef9	2026-06-20 16:59:43.303419+05:30	Uploaded document: test_doc.txt	3d79cad7-21c5-43bd-ba74-80479ce6c883
240d7f07-2862-4c08-b4d9-c80a72d5eaa3	DOCUMENT_DELETE	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 17:09:41.236333+05:30	Deleted document: test.txt	b1f92569-2466-4824-8633-118a4978c7c8
6534e164-c625-48b7-a631-64c9cdcb5809	DOCUMENT_DELETE	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 17:09:45.208348+05:30	Deleted document: Preeti DL.pdf	29f679e6-5f96-46cf-bb29-a8ed70e35a65
153fb84c-098b-47e9-8a18-7559d631077d	DOCUMENT_DELETE	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 17:09:47.930329+05:30	Deleted document: A Guide to Japanese Grammar - A Japanese Approach to Learning Japanese Grammar (Tae Kim) (z-library.sk, 1lib.sk, z-lib.sk).pdf	6eabad0c-be42-4a27-8090-2f868f39a15b
385b41e0-2223-48e4-bb0f-84385fa100af	DOCUMENT_UPLOAD	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 17:11:03.329357+05:30	Uploaded document: AI-Powered Knowledge Base System.pdf	12810c13-7302-474d-aac5-18bf878403c0
965f4fe9-271c-436a-b8ab-0eece31ebcc4	DOCUMENT_DELETE	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 17:33:45.40735+05:30	Deleted document: A Guide to Learning Hiragana  Katakana First steps to reading and writing Japanese (Kenneth G. Henshall, Tetsuo Takagaki) (z-library.sk, 1lib.sk, z-lib.sk).pdf	9c853e57-4322-49d7-9bdf-d648006793cc
9514209b-90fe-4cfe-88a5-affac9f56c8d	USER_ROLE_CHANGE	e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-20 20:58:55.144916+05:30	Changed role to: EMPLOYEE	00468d52-14f8-4f9f-8530-6bacf4915ef9
\.


--
-- Data for Name: collections; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.collections (id, created_at, updated_at, description, name, owner_id) FROM stdin;
f55107ed-b3df-433a-9c11-2ed5454d4bc3	2026-06-20 14:42:30.557377+05:30	2026-06-20 14:42:30.557377+05:30	\N	kb	e1e8a75c-3450-4576-a69f-20ecd5db8639
\.


--
-- Data for Name: document_chunks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.document_chunks (id, created_at, updated_at, chunk_index, content, token_count, document_id) FROM stdin;
592d98b2-e5e2-4672-bf85-209070ceaca7	2026-06-20 16:59:44.139777+05:30	2026-06-20 16:59:44.139777+05:30	0	This is a test document for the knowledge base. It contains some sample text that will be extracted by Apache Tika and chunked into smaller pieces.	36	3d79cad7-21c5-43bd-ba74-80479ce6c883
4e26f722-895c-4d85-bd36-4a9209a7bbc5	2026-06-20 17:11:03.459144+05:30	2026-06-20 17:11:03.459144+05:30	0	AI-Powered Knowledge Base System Project Overview An AI-Powered Knowledge Base allows users to upload documents and ask questions in natural language. The system retrieves relevant information from company documents and uses an LLM to generate accurate answers. Example: User: "How many annual leaves can employees take?" AI: "According to the HR policy document, employees are entitled to 24 annual paid leaves per year." Enterprise Use Cases HR Assistant Leave policies Company guidelines Employee handbook IT Support Assistant Troubleshooting guides Internal documentation Infrastructure manuals Legal Assistant Contracts Compliance documents Regulations Customer Support Assistant Product documentation FAQs Support manuals • • • • • • • • • • • • 1 System Architecture Frontend (React/Angular) ↓ Spring Boot REST API ↓ Spring Security (JWT) ↓ Document Service ↓ Vector Database ↓ Spring AI ↓ LLM (OpenAI/Gemini/Ollama) Core Features Authentication Register Login JWT Security Role-based access	249	12810c13-7302-474d-aac5-18bf878403c0
31292c47-0670-411a-b138-cc4c30fa555e	2026-06-20 17:11:03.46175+05:30	2026-06-20 17:11:03.46175+05:30	1	↓ Spring Boot REST API ↓ Spring Security (JWT) ↓ Document Service ↓ Vector Database ↓ Spring AI ↓ LLM (OpenAI/Gemini/Ollama) Core Features Authentication Register Login JWT Security Role-based access Roles: Admin Employee Viewer • • • • • • • 2 Document Management Users can: Upload PDFs Upload DOCX files Upload TXT files Delete documents View uploaded documents Metadata: File name Upload date Owner Department Document Processing When a document is uploaded: Extract text Split text into chunks Generate embeddings Store embeddings in vector database Example: PDF ↓ Text Extraction ↓ Chunking ↓ Embeddings ↓ Vector Storage • • • • • • • • • 1. 2. 3. 4. 3 AI Chat User asks: "What is the refund policy?" System: Generate embedding of question Search vector database Retrieve relevant chunks Send context to LLM Generate answer Search Support: Semantic Search Keyword Search Hybrid Search Example: Query: "Vacation policy" Results: Leave Policy.pdf Employee Handbook.pdf Chat History Store: User	249	12810c13-7302-474d-aac5-18bf878403c0
f00e86e2-c62d-422d-ad92-ccf3163bd175	2026-06-20 17:11:03.463148+05:30	2026-06-20 17:11:03.463148+05:30	2	d context to LLM Generate answer Search Support: Semantic Search Keyword Search Hybrid Search Example: Query: "Vacation policy" Results: Leave Policy.pdf Employee Handbook.pdf Chat History Store: User messages AI responses Timestamps Features: Previous conversations 1. 2. 3. 4. 5. • • • • • • • • • 4 Search chats Export history Citations AI should provide sources. Example: Answer: "Employees are eligible for 24 annual leaves." Sources: HR_Policy.pdf Page 12 Database Design Users id name email password role Documents id file_name owner_id upload_date Embeddings id document_id chunk_text vector Chats id user_id • • • • • • • • • • • • • • • • • • • 5 question answer timestamp Technology Stack Backend: Java 21 Spring Boot Spring Security Spring Data JPA AI: Spring AI OpenAI / Gemini / Ollama Database: PostgreSQL Vector Database: pgvector Qdrant Weaviate Infrastructure: Docker Docker Compose Monitoring: Prometheus Grafana Advanced Enterprise Features Department Isolation HR employees can	249	12810c13-7302-474d-aac5-18bf878403c0
84f24cf0-34e2-4bd9-bea7-c29e6e2cc388	2026-06-20 17:11:03.463148+05:30	2026-06-20 17:11:03.463148+05:30	3	a Database: PostgreSQL Vector Database: pgvector Qdrant Weaviate Infrastructure: Docker Docker Compose Monitoring: Prometheus Grafana Advanced Enterprise Features Department Isolation HR employees can only access HR documents. • • • • • • • • • • • • • • • • • 6 Finance employees can only access Finance documents. Audit Logs Track: Uploads Deletions Searches AI queries AI Summarization Generate: Document summaries Key points Action items AI Document Comparison Compare: Contract versions Policy updates Technical documents Multi-Document Chat User: "Compare leave policy and contractor policy." AI searches multiple documents and produces a combined answer. Resume Description Developed an enterprise-grade AI Knowledge Base using Spring Boot, Spring Security, PostgreSQL, pgvector, and Spring AI. Implemented Retrieval-Augmented Generation (RAG), document ingestion, • • • • • • • • • • 7 semantic search, JWT authentication, role-based access control, source citations, and AI-powered document	249	12810c13-7302-474d-aac5-18bf878403c0
c8f9e218-61ef-407d-a965-ba0b73e0fb00	2026-06-20 17:11:03.463148+05:30	2026-06-20 17:11:03.463148+05:30	4	I. Implemented Retrieval-Augmented Generation (RAG), document ingestion, • • • • • • • • • • 7 semantic search, JWT authentication, role-based access control, source citations, and AI-powered document question answering. 8 AI-Powered Knowledge Base System Project Overview Enterprise Use Cases HR Assistant IT Support Assistant Legal Assistant Customer Support Assistant System Architecture Core Features Authentication Document Management Document Processing AI Chat Search Chat History Citations Database Design Technology Stack Advanced Enterprise Features Department Isolation Audit Logs AI Summarization AI Document Comparison Multi-Document Chat Resume Description	167	12810c13-7302-474d-aac5-18bf878403c0
dea92de4-a3cd-4623-b37f-6a8aac1b2557	2026-06-20 17:45:27.229236+05:30	2026-06-20 17:45:27.229236+05:30	0	This is a test document for the knowledge base. It contains some sample text that will be extracted by Apache Tika and chunked into smaller pieces.	36	b19b4f26-2988-4a0d-aec8-e191b6224fa6
\.


--
-- Data for Name: document_tags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.document_tags (document_id, tag_id) FROM stdin;
12810c13-7302-474d-aac5-18bf878403c0	a9efe8d4-0df4-430a-a8e1-c945ea7403e2
\.


--
-- Data for Name: documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.documents (id, created_at, updated_at, description, file_name, file_size, file_type, storage_path, title, visibility, collection_id, owner_id, error_message, status) FROM stdin;
3d79cad7-21c5-43bd-ba74-80479ce6c883	2026-06-20 16:59:43.274166+05:30	2026-06-20 16:59:44.143405+05:30	Testing async chunks	test_doc.txt	149	text/plain	dca2ad5c-75ad-4692-a236-35c6b57f4650.txt	Test Document	PRIVATE	\N	00468d52-14f8-4f9f-8530-6bacf4915ef9	\N	READY
12810c13-7302-474d-aac5-18bf878403c0	2026-06-20 17:11:03.3247+05:30	2026-06-20 17:11:03.464562+05:30	kb	AI-Powered Knowledge Base System.pdf	37100	application/pdf	b083e0df-5ccd-4d01-84a4-6803d77376fe.pdf	AI	PRIVATE	\N	e1e8a75c-3450-4576-a69f-20ecd5db8639	\N	READY
b19b4f26-2988-4a0d-aec8-e191b6224fa6	2026-06-20 16:58:25.504668+05:30	2026-06-20 17:45:27.236504+05:30	Testing async chunks	test_doc.txt	149	text/plain	36898d33-4ee7-466d-a942-5b0d17d1c0d8.txt	Test Document	PRIVATE	\N	00468d52-14f8-4f9f-8530-6bacf4915ef9	\N	READY
\.


--
-- Data for Name: tags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tags (id, created_at, updated_at, color, name) FROM stdin;
fb6f75ee-7175-4b75-991c-26ea2d559c96	2026-06-20 14:42:19.502386+05:30	2026-06-20 14:42:19.502386+05:30	#22C55E	knowledgebase
a9efe8d4-0df4-430a-a8e1-c945ea7403e2	2026-06-20 16:12:18.343645+05:30	2026-06-20 16:12:18.343645+05:30	#EF4444	draft
4d9d5c60-c4d8-42a5-b138-689934ec0d01	2026-06-20 16:20:20.94171+05:30	2026-06-20 16:20:20.94171+05:30	#3B82F6	eng
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, created_at, updated_at, email, full_name, password, role) FROM stdin;
e1e8a75c-3450-4576-a69f-20ecd5db8639	2026-06-19 16:16:11.219567+05:30	2026-06-19 16:16:11.219567+05:30	friendisrude@gmail.com	Nilesh Kumar	$2a$10$beBT3SDBJPqfdMzjtPQwZeAOg.5pIqog2HUDJnDwmM84HGJWE9Hyu	ADMIN
d4b62421-2855-453f-b5c3-8e6288b684f0	2026-06-20 15:30:52.639868+05:30	2026-06-20 15:30:52.639868+05:30	nileshkumar25777@gmail.com	Dumboz	$2a$10$t6yXNW/Zj6jVt01P.rTdQ.Y0z1Vhm5Kukh/bn4lN6tCqKzTJ8D1Hm	VIEWER
00468d52-14f8-4f9f-8530-6bacf4915ef9	2026-06-20 16:15:37.103392+05:30	2026-06-20 20:58:55.136803+05:30	test@example.com	Test User	$2a$10$9hBVOXUspo.X5PbIYnrBxerLBh3UBE00y8Z/7P8GGRfAXGP25jWui	EMPLOYEE
\.


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: collections collections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_pkey PRIMARY KEY (id);


--
-- Name: document_chunks document_chunks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.document_chunks
    ADD CONSTRAINT document_chunks_pkey PRIMARY KEY (id);


--
-- Name: document_tags document_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.document_tags
    ADD CONSTRAINT document_tags_pkey PRIMARY KEY (document_id, tag_id);


--
-- Name: documents documents_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pkey PRIMARY KEY (id);


--
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: tags ukt48xdq560gs3gap9g7jg36kgc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT ukt48xdq560gs3gap9g7jg36kgc UNIQUE (name);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_document_chunks_document_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_document_chunks_document_id ON public.document_chunks USING btree (document_id);


--
-- Name: document_tags fkaurbdl9yo1wsoereckcwejrxs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.document_tags
    ADD CONSTRAINT fkaurbdl9yo1wsoereckcwejrxs FOREIGN KEY (tag_id) REFERENCES public.tags(id);


--
-- Name: document_tags fkc99c5qjulwx9gru07yrhicgd2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.document_tags
    ADD CONSTRAINT fkc99c5qjulwx9gru07yrhicgd2 FOREIGN KEY (document_id) REFERENCES public.documents(id);


--
-- Name: documents fkisfxu0tmysel9d9dklmupqlke; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT fkisfxu0tmysel9d9dklmupqlke FOREIGN KEY (collection_id) REFERENCES public.collections(id);


--
-- Name: collections fkjw635i2ihg19u3sq3lkvyrioy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT fkjw635i2ihg19u3sq3lkvyrioy FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: document_chunks fkks8knsiau23lcmv9mydqjmj84; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.document_chunks
    ADD CONSTRAINT fkks8knsiau23lcmv9mydqjmj84 FOREIGN KEY (document_id) REFERENCES public.documents(id);


--
-- Name: documents fkoduxo6gl9tkyx39jo5kue60bq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT fkoduxo6gl9tkyx39jo5kue60bq FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

\unrestrict xgjador5fRscynxGbRmFzXsiYFmMsDJHzwLwpE91ArzpA9DO41Et5X21SnGLy5C

