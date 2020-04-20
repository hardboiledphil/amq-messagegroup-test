# Getting Started


Application is stored in git repo

master branch was first attempt at basic dual connection scenario

story/wrap_xa was adding xa to the above example

story/add jms was adding jms message send to above example

story/add_listener is adding messageListener to pick up the message (NOT WORKING)

Expects Artemis to be running on 6116

DB's both need following table


  CREATE TABLE "OSOWNERBS1"."MESSAGESPJA" 
   (	"ID" NUMBER(*,0) NOT NULL ENABLE, 
	"MESSAGE" VARCHAR2(255 BYTE) NOT NULL ENABLE
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 524288 NEXT 524288 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "OSDATA" ;

  CREATE UNIQUE INDEX "OSOWNERBS1"."INDEX1" ON "OSOWNERBS1"."MESSAGESPJA" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 524288 NEXT 524288 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "OSDATA" ;
  
    CREATE TABLE "GPOWNERBS1"."MESSAGESPJA" 
   (	"ID" NUMBER(*,0) NOT NULL ENABLE, 
	"MESSAGE" VARCHAR2(255 BYTE) NOT NULL ENABLE
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 524288 NEXT 524288 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "GPDATA" ;

  CREATE UNIQUE INDEX "GPOWNERBS1"."INDEX1" ON "GPOWNERBS1"."MESSAGESPJA" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 524288 NEXT 524288 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "GPDATA" ;
  

### To trigger the db entry and message creation
curl -d'{ "ID" : "2", "MESSAGE" : "Hello world2" }' -H"content-type: application/json" http://localhost:8080\?rollback\=false

### To  trigger the db entry and message creation but throw an exception which will roll back the XA transaction
curl -d'{ "ID" : "2", "MESSAGE" : "Hello world2" }' -H"content-type: application/json" http://localhost:8080\?rollback\=true

### To shut down the application
curl -X POST localhost:8080/actuator/shutdown

### To spin up the application (using a settings file from normal repo)
 ./mvnw spring-boot:run -s /c/dev/azurerepo/mot/eodos/configuration/settings.xml
 
###