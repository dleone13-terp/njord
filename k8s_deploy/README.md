# Kubernetes Deployment Configs

------------------

## Chart Server

Deploy
```shell
./gradlew deploy
```

Logs
```shell
kubectl -n njord logs $(kubectl get pods -n njord -l app=njord-chart-svc -o jsonpath='{.items[*].metadata.name}')
```

------------------

## PostGIS

**Note**: Database migrations are now handled automatically by Flyway within the chart_server application.
The init container is no longer needed. Flyway will run migrations on application startup.

Add Github container registry secrets to njord namespace
```shell
cd k8s
./k8s_create_reg_sec.sh k8s_login
cd ..
./gradlew secret
```

Deploy postgis service
```shell
cd k8s
kubectl apply -f postgis_volume.yaml
kubectl apply -f postgis.yaml
# Migrations will run automatically when chart_server starts
```

Check postgis service
```shell
kubectl -n njord describe pod -l app=njord-postgis-svc 
kubectl -n njord logs $(kubectl get pods -n njord -l app=njord-postgis-svc -o jsonpath='{.items[*].metadata.name}')
kubectl -n njord describe deployment njord-postgis
```

### RDS and PostGIS Extensions

Flyway migration V0__postgis_extensions.sql automatically handles PostGIS extension setup on **all** PostgreSQL environments including:
- AWS RDS for PostgreSQL
- Local PostgreSQL with PostGIS
- Docker containers (postgis/postgis image)
- Self-managed PostgreSQL servers

The migration includes proper error handling for RDS permission restrictions and will:
- Create extensions using `CREATE EXTENSION IF NOT EXISTS` (safe and idempotent)
- Handle schema ownership gracefully (skips if insufficient privileges)
- Work with any PostgreSQL user that has extension creation rights

**No manual setup is needed** - Flyway handles everything on first application startup.
