package org.iot.dsa.dslink.java.v2.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.security.DSPasswordAes;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class designed for handling connections with and arbitrary driver using C3P0 pooling.
 *
 * @author James (Juris) Puchin
 * Created on 10/13/2017
 */
public class C3P0PooledDBConnectionNode extends DBConnectionNode {
    private ComboPooledDataSource pool_data_source = null;

    public C3P0PooledDBConnectionNode() {

    }

    C3P0PooledDBConnectionNode(DSMap params) {
        super(params);
    }

    @Override
    DSAction makeEditAction() {
        DSAction act = super.makeEditAction();
        DSList drivers = JDBCv2Helpers.getRegisteredDrivers();
        act.addParameter(JDBCv2Helpers.DB_URL, DSValueType.STRING, null).setPlaceHolder("jdbc:mysql://127.0.0.1:3306");
        act.addParameter(JDBCv2Helpers.DRIVER, DSValueType.ENUM, null).setEnumRange(drivers);
        return act;
    }

    @Override
    void closeConnections() {
        if (pool_data_source != null) {
            pool_data_source.close();
        }
    }

    @Override
    void createDatabaseConnection() {
        try {
            String url = db_url.getValue().toString();
            String name = usr_name.getValue().toString();
            String pass = ((DSPasswordAes) password.getValue()).decode();
            String drvr = driver.getValue().toString();

            pool_data_source = new ComboPooledDataSource();
            pool_data_source.setDriverClass(drvr); //loads the jdbc driver
            pool_data_source.setJdbcUrl(url);
            pool_data_source.setUser(name);
            pool_data_source.setPassword(pass);
            pool_data_source.setAcquireRetryAttempts(6);
            pool_data_source.setAcquireRetryDelay(500);
            pool_data_source.setCheckoutTimeout(3000);
            //TODO: implement dynamic ping cycle/connections
            //pool_data_source.setTestConnectionOnCheckout(true);
            //pool_data_source.setPreferredTestQuery("SELECT 1");

            /*
            //Alternative, uses standard JDBC drivers
            //Might be useful later if implementation that does not need explicit driver passing is desired.
            DataSource ds_unpooled = DataSources.unpooledDataSource(url, name, pass);
            DataSource ds_pooled = DataSources.pooledDataSource( ds_unpooled );
            */
        } catch (PropertyVetoException e) {
            connSuccess(false);
            warn("Failed to connect to Database: " + db_name.getValue() + " Message: " + e);
        }
        testConnection();
    }

    @Override
    Connection getConnection() throws SQLException {
        return pool_data_source.getConnection();
    }
}