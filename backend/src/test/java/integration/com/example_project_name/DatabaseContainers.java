package integration.com.example_project_name;

import java.util.Locale;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.oracle.OracleContainer;

/**
 * One shared database container for all integration tests, chosen with -Dit.db=sqlserver|oracle
 * (default: sqlserver, set in pom.xml). Run both in CI:  ./mvnw verify  and  ./mvnw verify -Dit.db=oracle
 */
public final class DatabaseContainers {

    private static final String SQLSERVER_IMAGE = "mcr.microsoft.com/mssql/server:2022-latest";
    private static final String ORACLE_IMAGE = "gvenzl/oracle-free:23-slim-faststart";

    private static JdbcDatabaseContainer<?> container;

    private DatabaseContainers() {
    }

    public static String selected() {
        return System.getProperty("it.db", "sqlserver").toLowerCase(Locale.ROOT);
    }

    private static OracleContainer oracle() {
        return new OracleContainer(ORACLE_IMAGE)
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }

    private static MSSQLServerContainer<?> sqlServer() {
        MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(SQLSERVER_IMAGE);
        sqlServer.acceptLicense(); // you must accept the SQL Server EULA to use the image
        sqlServer.withUrlParam("trustServerCertificate", "true"); // container uses a self-signed cert
        return sqlServer;
    }

    public static synchronized JdbcDatabaseContainer<?> getInstance() {
        if (container == null) {
            container = switch (selected()) {
                case "oracle" -> oracle();
                case "sqlserver" -> sqlServer();
                default -> throw new IllegalArgumentException("Unknown it.db: " + selected() + " (use sqlserver or oracle)");
            };
            container.start();
        }
        return container;
    }
}
