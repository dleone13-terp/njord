package io.madrona.njord

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jmx.JmxReporter
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.util.logging.*
import io.madrona.njord.db.ChartDao
import io.madrona.njord.db.FeatureDao
import io.madrona.njord.db.TileDao
import io.madrona.njord.endpoints.AdminUtil
import io.madrona.njord.geo.TileSystem
import io.madrona.njord.geo.symbols.S57ObjectLibrary
import io.madrona.njord.layers.LayerFactory
import io.madrona.njord.model.ColorLibrary
import io.madrona.njord.util.SpriteSheet
import io.madrona.njord.util.ZFinder
import org.flywaydb.core.Flyway
import org.gdal.osr.SpatialReference
import org.locationtech.jts.geom.GeometryFactory
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

object Singletons {

    lateinit var genLog: Logger

    val adminUtil by lazy { AdminUtil() }

    val chartDao by lazy { ChartDao() }

    val featureDao by lazy { FeatureDao() }

    val tileDao by lazy { TileDao() }

    val config by lazy { ChartsConfig() }

    val spriteSheet by lazy { SpriteSheet() }

    val ds: DataSource by lazy {
        val hc = HikariConfig()
        hc.jdbcUrl = "jdbc:postgresql://${config.pgHost}:${config.pgPort}/${config.pgDatabase}"
        hc.username = config.pgUser
        hc.password = config.pgPassword
        hc.addDataSourceProperty("cachePrepStmts", "true")
        hc.addDataSourceProperty("prepStmtCacheSize", "250")
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hc.maximumPoolSize = config.pgConnectionPoolSize
        hc.connectionTimeout = 120000
        hc.leakDetectionThreshold = 300000
        HikariDataSource(hc)
    }

    /**
     * Flyway database migration manager.
     * Runs migrations automatically when accessed, ensuring database schema is up to date.
     */
    private val flyway: Flyway by lazy {
        Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true) // Allow migration on existing databases
            .baselineVersion("0") // Start from version 0 (PostGIS extensions)
            .validateOnMigrate(true)
            .load()
    }

    /**
     * Initialize database migrations.
     * This should be called early in application startup to ensure schema is ready.
     */
    fun initDatabase() {
        genLog.info("Running database migrations...")
        val result = flyway.migrate()
        genLog.info("Database migrations complete. Applied ${result.migrationsExecuted} migrations.")
    }

    val colorLibrary by lazy { ColorLibrary() }

    val wgs84SpatialRef by lazy { SpatialReference("""GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AXIS["Latitude",NORTH],AXIS["Longitude",EAST],AUTHORITY["EPSG","4326"]]""") }

    val zFinder by lazy { ZFinder() }

    val tileSystem by lazy { TileSystem() }

    val geometryFactory by lazy { GeometryFactory() }

    val metrics by lazy {
        MetricRegistry().also {

            if (config.consoleMetrics) {
                ConsoleReporter.forRegistry(it)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build()
                    .start(1, TimeUnit.MINUTES)
            }

            JmxReporter.forRegistry(it)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start()
        }
    }

    val layerFactory by lazy { LayerFactory() }

    val s57ObjectLibrary by lazy { S57ObjectLibrary() }
}
