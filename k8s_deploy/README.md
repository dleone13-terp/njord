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

### Legacy: Manual PostGIS Extension Setup (if needed)

If you need to manually set up PostGIS extensions (e.g., on a fresh RDS instance), you can run:
```shell
kubectl run -n njord postgis-setup --rm -i --restart=Never --image=postgres:13 -- \
  psql -h njord-postgis-svc.njord.svc.cluster.local -U admin -d s57server \
  -c "CREATE EXTENSION IF NOT EXISTS postgis; \
      CREATE EXTENSION IF NOT EXISTS postgis_raster; \
      CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;"
```

Otherwise, Flyway migration V0__postgis_extensions.sql will handle this automatically.
