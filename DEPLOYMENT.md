# TodoRails deployment to Railway

## Architecture

Deploy this Spring Boot 3.5.5 application as a Dockerfile based Railway web service and connect it to a Railway MySQL service named **MySQL** over Railway's private network. The app uses Java 17, Spring Data JPA, Thymeleaf, and MySQL. Maven packages templates and static assets from `src/main/resources` into the executable JAR.

There is no Flyway/Liquibase migration tool or SQL dump in this repository. The production profile uses Hibernate `update`: for a new database this creates the mapped tables; for an existing database it applies non-destructive schema updates and does not drop rows. Review entity changes and back up production data before future schema changes. No database is initialized or modified until the app connects to Railway.

New todo datetimes are accepted as Asia/Kolkata local values and stored as UTC in the existing `LocalDateTime` column. Existing rows are not bulk-converted. Rows created before this convention may represent local time rather than UTC and can display with a 5 hour 30 minute offset; review or manually migrate legacy data only after taking a verified backup.

## Railway setup

1. Create or select a Railway project and environment. Add the Railway MySQL template service and name the service **MySQL**. Keep it private; the app connects through the internal service network, so do not enable a public TCP proxy.
2. Add this repository as a web service and select Dockerfile deployment (Railway detects the repository `Dockerfile`).
3. In the app service Variables, add the following values/references. Use Railway's variable picker to insert private references to the MySQL service variables:

   ```text
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC
   SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
   SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   ```

   Add `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, and `APP_REMINDERS_EMAIL_ENABLED=true` only if email reminders are wanted. Reminders default to disabled. Never paste secrets into source or this document. The root properties file uses environment overrides and local MySQL defaults; production's profile requires the three datasource variables and has no localhost fallback.
4. Deploy. The container listens on `0.0.0.0`; Spring Boot reads Railway's `PORT` and defaults to 8080 locally. Generate a public domain from the app service's Networking settings.
5. Check `https://<your-domain>/health` for HTTP 200, then open `/register` to create a demo account and add tasks through the UI. No demo credentials are built into the app.

## Environment variables

Required for production: `SPRING_PROFILES_ACTIVE`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Railway supplies the `PORT` value. `SPRING_JPA_HIBERNATE_DDL_AUTO` defaults to `update` in the production profile. Email reminders are disabled by default; enable with `APP_REMINDERS_EMAIL_ENABLED=true` and configure `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, and `SPRING_MAIL_PASSWORD` to send them.

The `.env.example` file contains placeholders for local reference only. Copy it to an ignored `.env` only if your chosen local tooling loads dotenv files; Spring Boot itself does not automatically load `.env` files.

## Future deploys and logs

Connect the app service to the desired GitHub repository and branch in Railway. Each push can trigger a new Docker build and deployment. To deploy manually, use the Railway dashboard's deployment controls. Open the app service's **Deployments** view and select a deployment to inspect build and runtime logs. The Railway CLI is not required.

## MySQL backup and persistence

Before schema changes or database maintenance, create a MySQL backup from Railway's database service using its supported backup/export workflow, or run `mysqldump` from a trusted environment with the private connection details and store the output securely outside Git. Verify the backup can be restored before relying on it. Do not expose the MySQL service publicly for routine app connectivity.

Railway plan limits, included usage, and current prices depend on the account and can change. Check the dashboard's usage/billing page before creating resources or enabling paid usage. The app's profile-picture uploads are written to the local filesystem and may not survive container replacement; use an external persistent file store if durable uploads become a requirement.

## Local build

Use Java 17 and Maven, select the `local` profile so the ignored `application-local.properties` is loaded, and run `mvn -Dspring.profiles.active=local test package`. For a distributable Docker image, the `railway-image` Maven profile excludes the local profile resource as defense in depth. The production `update` strategy is intended for this demo deployment; maintain a backup before connecting it to any database containing important data.
