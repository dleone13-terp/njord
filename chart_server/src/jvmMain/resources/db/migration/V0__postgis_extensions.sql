-- PostGIS extensions setup
-- This migration is optional and should only run on AWS RDS or when PostGIS extensions need to be installed
-- For local development with postgis/postgis Docker image, PostGIS is already enabled

-- Enable PostGIS extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_raster;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE EXTENSION IF NOT EXISTS postgis_tiger_geocoder;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
CREATE EXTENSION IF NOT EXISTS address_standardizer_data_us;

-- Set ownership for RDS compatibility
-- Note: This uses the current database user, so ensure proper permissions
DO $$
DECLARE
    current_user_name TEXT;
BEGIN
    SELECT current_user INTO current_user_name;
    
    EXECUTE format('ALTER SCHEMA tiger OWNER TO %I', current_user_name);
    EXECUTE format('ALTER SCHEMA tiger_data OWNER TO %I', current_user_name);
    EXECUTE format('ALTER SCHEMA topology OWNER TO %I', current_user_name);
EXCEPTION
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'Insufficient privileges to alter schema ownership. This is expected on managed PostgreSQL services.';
    WHEN undefined_object THEN
        RAISE NOTICE 'Schema does not exist. Skipping ownership change.';
END $$;
