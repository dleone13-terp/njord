# Flyway Database Migrations

This directory contains Flyway database migration scripts for the njord chart server.

## Migration Files

- **V0__postgis_extensions.sql** - Sets up PostGIS and related extensions (optional for Docker PostGIS images)
- **V1__initial_schema.sql** - Creates the core database schema (meta, charts, features tables)

## How It Works

Flyway automatically runs migrations when the chart_server application starts. Migrations are:
- Versioned (V0, V1, V2, etc.)
- Immutable (once applied, they should not be changed)
- Tracked in the `flyway_schema_history` table

## Migration Naming Convention

Flyway migration files follow this naming pattern:
```
V{version}__{description}.sql
```

Examples:
- `V1__initial_schema.sql`
- `V2__add_user_table.sql`
- `V3__add_indexes.sql`

## Adding New Migrations

1. Create a new SQL file with the next version number
2. Use double underscore `__` after the version
3. Use descriptive names separated by underscores
4. Flyway will automatically detect and run new migrations on app startup

## Baseline

The Flyway configuration uses:
- `baselineOnMigrate(true)` - Allows migration on existing databases
- `baselineVersion("0")` - Treats version 0 as the baseline

This means:
- Fresh databases: All migrations run (V0, V1, etc.)
- Existing databases: Baselining at V0, then runs V1 and later

## Local Development

When running `docker-compose up` with the `postgis/postgis:13-3.1` image:
- PostGIS extensions are already enabled
- V0__postgis_extensions.sql will run but may skip some operations (this is fine)
- V1__initial_schema.sql creates the application tables

## Kubernetes/Production

On first deployment:
1. PostgreSQL starts (from postgis.yaml)
2. chart_server starts and Flyway runs migrations automatically
3. No separate init job is needed

## Troubleshooting

If migrations fail:
1. Check chart_server logs for Flyway errors
2. Verify database connectivity
3. Check `flyway_schema_history` table: `SELECT * FROM flyway_schema_history;`
4. To repair failed migrations: Update Flyway configuration or manually fix `flyway_schema_history`

## Migration from Old Init System

The previous system used:
- `chart_server_db/postgres_init/` scripts
- Separate init container/job
- Manual execution via shell scripts

Now:
- Migrations are embedded in the application
- Automatic execution on startup
- Version-controlled and repeatable
- Better tracking and rollback support
