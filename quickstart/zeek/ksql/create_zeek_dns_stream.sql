CREATE STREAM dns_stream ( 
ts DOUBLE(16,6), 
uid STRING, 
"id.orig_h" VARCHAR, 
"id.orig_p" INTEGER, 
"id.resp_h" VARCHAR, 
"id.resp_p" INTEGER, 
proto STRING, 
trans_id INTEGER, 
"query" VARCHAR, 
qclass INTEGER, 
qclass_name VARCHAR, 
qtype INTEGER, 
qtype_name STRING, 
rcode INTEGER, 
rcode_name STRING, 
AA BOOLEAN, 
TC BOOLEAN, 
RD BOOLEAN, 
RA BOOLEAN, 
Z INTEGER, 
answers array<VARCHAR>, 
TTLs array<DOUBLE(5,1)>, 
rejected BOOLEAN) 
WITH (KAFKA_TOPIC='dns', VALUE_FORMAT='JSON');
