# Scripts

Here, I memo scripts that I have used during development.

## Postgres

### Run Postgres Container

```
--name : 프로세스 이름
-p : 맵핑할 포트 번호
-e : 환경변수 셋팅
-d : 데몬 모드로 실행
docker run --name test_postgres -p 5432:5432 -e POSTGRES_PASSWORD=pass -d postgres

```

This cmdlet will create Postgres instance so that you can connect to a database with:
* database: postgres
* username: postgres
* password: pass
* post: 5432

### Getting into the Postgres container

```
-i : interactive 모드 실행
-t : 타겟 지정(컨테이너)

docker exec -i -t test_postgres bash
```

Then you will see the containers bash as a root user.

### Connect to a database

```
psql -d postgres -U postgres
```

### Query Databases

```
\l
```

### Query Tables

```
\dt
```

### Quit

```
\q
```

## application.properties

### Datasource

```
spring.datasource.username=postgres
spring.datasource.password=pass
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Hibernate

```
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.properties.hibernate.format_sql=true

logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Test Database

```
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```