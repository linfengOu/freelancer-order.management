package com.freelancer.om.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Oliver
 *
 */
public class DBConnectionManager {

    private static class SingletonHolder {
        private static final DBConnectionManager INSTANCE = new DBConnectionManager();
    }
    private static int clients;
    private Vector < Driver > drivers = new Vector < > ();
    private Hashtable < String, DBConnectionPool > pools = new Hashtable < > ();
    private Properties dbProps;
    private PrintWriter log;

    /** 
     * singleton
     */
    private DBConnectionManager() {
        this.init();
        log("Database Connection Manager in position.");
    }

    /** 
     * Return sole instance, create instance when first invoked
     *  
     * @return DBConnectionManager (sole instance)
     */
    public static final DBConnectionManager getInstance() {
        clients++;
        return SingletonHolder.INSTANCE;
    }

    /** 
     * get a free connection, if not exist and connection number is less than max limit,
     * then create and return new connection
     *  
     * @param name (connection pool name defined in prop file)
     * @return Connection (available connection or null)
     */
    public Connection getConnection(String name) {
    	Connection con = null;
        DBConnectionPool dbPool = pools.get(name);
        if (dbPool != null) {
            con = dbPool.getConnection();
            if (con == null) {
            	return dbPool.getConnection(1000);
            } else {
            	return con;
            }
        }
        return null;
    }

    /** 
     * get a free connection, if not exist and connection number is less than max limit,
     * then create and return new connection, otherwise wait for release in waiting time.
     *  
     * @param name (connection pool name)
     * @param time (waiting time in milliseconds)
     * @return Connection (available connection or null)
     */
    public Connection getConnection(String name, long time) {
        DBConnectionPool dbPool = pools.get(name);
        if (dbPool != null) {
            return dbPool.getConnection(time);
        }
        return null;
    }

    /** 
     * free connection object to connection pool of the name
     *  
     * @param name (connection pool name)
     * @param con (connection object)
     */
    public void freeConnection(String name, Connection con) {
        DBConnectionPool dbPool = pools.get(name);
        if (dbPool != null) {
            dbPool.freeConnection(con);
        }
    }

    /** 
     * Close all connection, deregister drivers
     */
    public synchronized void release() {
        // wait till the last client
        if (--clients != 0) {
            return;
        }
        Enumeration < DBConnectionPool > allPools = pools.elements();
        while (allPools.hasMoreElements()) {
            DBConnectionPool pool = allPools.nextElement();
            pool.release();
        }
        Enumeration < Driver > allDrivers = drivers.elements();
        while (allDrivers.hasMoreElements()) {
            Driver driver = (Driver) allDrivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log("Deregister JDBC driver: " + driver.getClass().getName());
            } catch (SQLException e) {
                log(e, "Cannot deregister JDBC driver: " + driver.getClass().getName());
            }
        }
        log("Database Connection Manager released all resources.");
    }

    /** 
     * read properties and initial
     */
    private void init() {
    	ConfigManagement cm = ConfigManagement.getInstance();
    	dbProps = cm.getProps();
    	
        String logFile = dbProps.getProperty("logfile", "DBConnectionManager.log") + "db.log";
        try {
            log = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            System.out.println("Cannot open log file: " + logFile);
            System.out.println("Assuming not log file exsit. Begin to create log file.");
            File lf = new File(logFile);
            try {
				lf.createNewFile();
	            log = new PrintWriter(new FileWriter(logFile, true), true);
			} catch (IOException e1) {
				System.err.println("Cannot create file.");
	            log = new PrintWriter(System.err);
			}
        }

        loadDrivers(dbProps);

        createPools(dbProps);
    }

    /** 
     * load and register all JDBC drivers
     *  
     * @param props 
     */
    private void loadDrivers(Properties props) {
        String driverClasses = props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while (st.hasMoreElements()) {
            String driverClassName = st.nextToken().trim();
            try {
                Driver driver = (Driver) Class.forName(driverClassName).newInstance();
                DriverManager.registerDriver(driver);
                drivers.addElement(driver);
                log("Register JDBC successfully " + driverClassName);
            } catch (Exception e) {
                log("Cannot register JDBC: " + driverClassName + ", error: " + e);
            }
        }
    }

    /** 
     * Create connection pools
     *  
     * @param props 
     */
    private void createPools(Properties props) {
        Enumeration <? > propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                StringBuffer sb_url = new StringBuffer();
                String basicUrl = props.getProperty(poolName + ".url");
                if (basicUrl == null) {
                    log("Not found assigned URL for connection pool " + poolName);
                    continue;
                }
                String schema = props.getProperty(poolName + ".schema");
                String dbParams = "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false";
                sb_url.append(basicUrl).append("/").append(schema).append(dbParams);
                String url = sb_url.toString();
                String user = props.getProperty(poolName + ".user");
                String password = props.getProperty(poolName + ".password");
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                int max;
                try {
                    max = Integer.valueOf(maxconn).intValue();
                } catch (NumberFormatException e) {
                    log("Wrong max connection limit: " + maxconn + " . Connection pool: " + poolName);
                    max = 0;
                }
                DBConnectionPool pool = new DBConnectionPool(poolName, url, user, password, max, basicUrl, schema, dbParams);
                pools.put(poolName, pool);
                log("Create connection pool successfully: " + poolName);
            }
        }
    }

    /** 
     * Write log
     */
    private void log(String msg) {
        log.println(new Date() + ": " + msg);
    }

    /** 
     * Write log with exceptions 
     */
    private void log(Throwable e, String msg) {
        log.println(new Date() + ": " + msg);
        e.printStackTrace(log);
    }

    /*********************** internal class: connection pool *****************************/

    /** 
     *  
     * @function internal connection pool. Able to create new connection on demand, until reach to max limit.
     * 			  Before return to client process, it can check the validation of connection. 
     *  
     */
    class DBConnectionPool {

        private String poolName;
        private String dbConnUrl;
        private String dbUserName;
        private String dbPassWord;
        private int maxConn;
        private int checkedOut; // current connection number
        private Vector < Connection > freeConnections; // all free connections
        private String basicUrl;
        private String schema;
        private String dbParams;

        /** 
         * Construction of new connection pool
         *  
         * @param poolName 
         * @param dbConnUrl 
         * @param dbUserName 
         * @param dbPassWord 
         * @param maxConn 
         */
        public DBConnectionPool(String poolName, String dbConnUrl, String dbUserName, String dbPassWord, int maxConn, String basicUrl, String schema, String dbParams) {
            this.poolName = poolName;
            this.dbConnUrl = dbConnUrl;
            this.dbUserName = dbUserName;
            this.dbPassWord = dbPassWord;
            this.maxConn = maxConn;
            this.basicUrl = basicUrl;
            this.schema = schema;
            this.dbParams = dbParams;

            this.freeConnections = new Vector < Connection > ();
        }

        /** 
         * Get connection from connection pool. if no free connection and current number less than max limit,
         * then create new connection.
         * if the free connection no longer valid, delete it then recur itself to get a new connection.
         */
        @
        SuppressWarnings("resource")
        public synchronized Connection getConnection() {
            Connection conn = null; // define connection scalar
            if (freeConnections != null && freeConnections.size() > 0) {
                // get first connection from vectors
                conn = (Connection) freeConnections.firstElement();
                freeConnections.removeElementAt(0);
                try {
                    if (conn.isClosed()) {
                        // recur itself
                        conn = getConnection();
                    }
                } catch (SQLException e) {
                    // recur itself
                    conn = getConnection();
                }
            } else if (maxConn == 0 || checkedOut < maxConn) {
                conn = newConnection();
            }
            if (conn != null) {
                checkedOut++;
            }
            return conn;
        }

        /** 
         * get connection with waiting time
         *  
         * @param timeout 
         * 
         */
        public synchronized Connection getConnection(long timeout) {
            long startTime = System.currentTimeMillis();
            Connection conn = null;
            while ((conn = getConnection()) == null) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if ((System.currentTimeMillis() - startTime) >= timeout) {
                	log("[ NO CONNECTION ! ] No available connection after waiting " + timeout);
                    return null;
                }
            }
            return conn;
        }

        /** 
         * Create new connection
         *  
         * @return connection
         */
        private Connection newConnection() {
            Connection conn = null;
            try {
                if (dbUserName == null) {
                    conn = DriverManager.getConnection(dbConnUrl);
                } else {
                    conn = DriverManager.getConnection(dbConnUrl, dbUserName, dbPassWord);
                }
                log("Connection pool " + poolName + " create a new connection: " + conn.toString());
            } catch (SQLException e) {
                log("Cannot create connection with URL: " + dbConnUrl);
                log("Assuming no schema exsit, try to initialize schema");
                if (schema != null && initial()) {
                    conn = newConnection();
                } else {
                    return null;
                }
            }
            return conn;
        }

        /** 
         * Return connection
         *  
         * @param con  
         */
        public synchronized void freeConnection(Connection conn) {
            freeConnections.addElement(conn);
            checkedOut--;
            notifyAll();
        }

        /** 
         * Close all connections
         */
        public synchronized void release() {
            Enumeration < Connection > allConnections = freeConnections.elements();
            while (allConnections.hasMoreElements()) {
                Connection con = (Connection) allConnections.nextElement();
                try {
                    con.close();
                } catch (SQLException e) {
                    log(e, "Cannot close a connection in pool: " + poolName);
                }
            }
            freeConnections.removeAllElements();
        }

        /**
         * initial schema
         */
        public synchronized boolean initial() {
        	String _schema = schema;
            schema = null;
            boolean flag = false;
            Connection con = null;
            Statement stmt = null;
            try {
                if (dbUserName == null) {
                    con = DriverManager.getConnection(basicUrl + dbParams);
                } else {
                    con = DriverManager.getConnection(basicUrl + dbParams, dbUserName, dbPassWord);
                }
            } catch (SQLException e) {
                log(e, "Cannot create connection with URL: " + basicUrl + dbParams);
                return flag;
            }
            
            String[] sql = {"CREATE DATABASE IF NOT EXISTS `" + _schema + "` DEFAULT CHARACTER SET utf8",
                "CREATE TABLE `" + _schema + "`.`order` (" +
                "  `oid` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                "  `title` char(40) NOT NULL," +
                "  `price` float(8,1) NOT NULL," +
                "  `adiv` tinyint(1) NOT NULL," +
                "  `bdiv` tinyint(1) NOT NULL," +
                "  `createdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  `deadline` datetime NOT NULL," +
                "  `place` char(40) DEFAULT NULL," +
                "  `deposit` float(6,1) DEFAULT NULL," +
                "  PRIMARY KEY (`oid`)," +
                "  INDEX `create_date` (`createdate` ASC)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8",
                "CREATE TABLE `" + _schema + "`.`orderdesc` (" +
                "  `oid` int(10) unsigned NOT NULL," +
                "  `desc` varchar(255) DEFAULT NULL," +
                "  PRIMARY KEY (`oid`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8",
                "CREATE TABLE `" + _schema + "`.`orderstatus` (" +
                "  `oid` int(10) unsigned NOT NULL," +
                "  `aparty` char(16) NOT NULL," +
                "  `bparty` char(16)," +
                "  `status` tinyint(1) NOT NULL," +
                "  `enddate` datetime," +
                "  `isread` tinyint(1) NOT NULL," +
                "  PRIMARY KEY (`oid`)," +
                "  INDEX `person` (`aparty` ASC, `bparty` ASC, `enddate` ASC)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8",
                "CREATE TABLE `" + _schema + "`.`user` (" +
                "  `uid` int(10) unsigned NOT NULL AUTO_INCREMENT," +
                "  `name` char(16) NOT NULL," +
                "  `pw` char(40) NOT NULL," +
                "  `usertype` tinyint(1) NOT NULL," +
                "  `joindate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (`uid`)," +
                "  INDEX `user_name` (`name` ASC)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8"};
            try {
                stmt = con.createStatement();
                for (int i = 0; i < sql.length; i++) {
                	stmt.addBatch(sql[i]);
                }
                stmt.executeBatch();
                log("Schema " + _schema + " initialized successfully.");
                flag = true;
            } catch (SQLException e) {
                log(e, "Connot initial the schema " + _schema);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (con != null) {
                        con.close();
                    }
                } catch (SQLException e) {}
            }
            return flag;
        }
    }
}