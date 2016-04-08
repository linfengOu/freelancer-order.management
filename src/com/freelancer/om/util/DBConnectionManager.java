package com.freelancer.om.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

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
        DBConnectionPool dbPool = pools.get(name);
        if (dbPool != null) {
            return dbPool.getConnection();
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
    }

    /** 
     * read properties and initial
     */
    private void init() {

        InputStream fileinputstream = null;
        try {
            fileinputstream = new FileInputStream("./config/db.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            dbProps = new Properties();
            dbProps.load(fileinputstream);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot read properties file. " + "make sure db.properties is in the right path");
            return;
        }

        String logFile = dbProps.getProperty("logfile", "DBConnectionManager.log");
        try {
            log = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            System.err.println("Cannot open log file: " + logFile);
            log = new PrintWriter(System.err);
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
                log("Register JDBC successfully" + driverClassName);
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
        Enumeration <?> propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                System.out.println(" poolName ||" + poolName + "|");
                String url = props.getProperty(poolName + ".url");
                if (url == null) {
                    log("Not found assigned URL for connection pool " + poolName);
                    continue;
                }
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
                DBConnectionPool pool = new DBConnectionPool(poolName, url, user, password, max);
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

        /** 
         * Construction of new connection pool
         *  
         * @param poolName 
         * @param dbConnUrl 
         * @param dbUserName 
         * @param dbPassWord 
         * @param maxConn 
         */
        public DBConnectionPool(String poolName, String dbConnUrl, String dbUserName, String dbPassWord, int maxConn) {
            this.poolName = poolName;
            this.dbConnUrl = dbConnUrl;
            this.dbUserName = dbUserName;
            this.dbPassWord = dbPassWord;
            this.maxConn = maxConn;
            this.freeConnections = new Vector < Connection > ();
        }

        /** 
         * Get connection from connection pool. if no free connection and current number less than max limit,
         * then create new connection.
         * if the free connection no longer valid, delete it then recur itself to get a new connection.
         */
        @SuppressWarnings("resource")
        public synchronized Connection getConnection() {
            Connection conn = null; // define connection scalar
            if (freeConnections != null && freeConnections.size() > 0) {
                // get first connection from vectors
                conn = (Connection) freeConnections.firstElement();
                freeConnections.removeElementAt(0);
                try {
                    if (conn.isClosed()) {
                        log("Delete a invalid connection from connection pool: " + poolName);
                        // recur itself
                        conn = getConnection();
                    }
                } catch (SQLException e) {
                    log("Delete a invalid connection from connection pool: " + poolName);
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
                log("Connection pool " + poolName + " create a new connection");
            } catch (SQLException e) {
                log(e, "Cannot create connection with URL: " + dbConnUrl);
                return null;
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
                    log("Close one connection in pool: " + poolName);
                } catch (SQLException e) {
                    log(e, "Cannot close a connection in pool: " + poolName);
                }
            }
            freeConnections.removeAllElements();
        }
    }
}