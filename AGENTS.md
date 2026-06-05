**AGENTS.md – Quick Reference for OpenCode Sessions**

- **Build & package**
  - `./mvnw clean package` – compiles, runs all tests (including H2‑based unit tests), and creates `target/backend-0.0.1-SNAPSHOT.jar`.
  - `./mvnw clean verify` – same as `package` but also runs integration‑test plugins if any.
- **Run the application**
  - `./mvnw spring-boot:run` – starts the dev server with Spring DevTools enabled (auto‑restart on classpath changes).
  - `java -jar target/backend-0.0.1-SNAPSHOT.jar` – runs the built jar.
- **Test execution**
  - `./mvnw test` – runs JUnit tests; the test classpath includes the embedded H2 database, so no external DB is required.
  - To run a single test class: `./mvnw -Dtest=CategoriaServiceTest test`.
- **Database**
  - Production uses **SQLite** (see `pom.xml` dependency `org.xerial:sqlite-jdbc`). The file `application.properties` holds the JDBC URL (default `jdbc:sqlite:./farmaros.db`).
  - Tests use an **in‑memory H2** database, automatically configured by Spring Boot’s `spring-boot-starter-data-jpa-test`.
- **OpenAPI / Swagger UI**
  - After the app starts, UI is served at `http://localhost:{port}/swagger-ui.html` (default port `8080`).
- **DevTools**
  - The `spring-boot-devtools` dependency is marked `runtime` and `optional`; it provides hot‑reload when source files change while running via `spring-boot:run`.
- **Java version**
  - Project targets **Java 25** (`<java.version>25</java.version>`). Ensure the JDK matches or set `JAVA_HOME` accordingly.
- **Common Maven shortcuts**
  - `./mvnw clean` – removes `target/`.
  - `./mvnw dependency:tree` – view resolved dependencies (useful to verify Lombok is on the classpath).
  - `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` – activates a `dev` profile if defined.
- **Lombok**
  - Lombok is optional (`<optional>true</optional>`). IDEs need Lombok plugin; otherwise, generated getters/setters are compiled at build time.
- **Generated code / source‑generation**
  - No explicit code‑gen plugins; all source lives under `src/main/java`.
- **Git workflow hints** (not enforced by tooling but common in this repo)
  - Feature work is done on a branch named `feature/<short‑desc>`.
  - Before opening a PR, run `./mvnw clean verify` to ensure build and tests pass.
  - Commit messages should follow the conventional‑commit style: `type(scope): description` (e.g., `feat(controller): add endpoint to list productos`).

*This file intentionally stays minimal – only the facts an OpenCode agent would otherwise have to infer by reading many files.*