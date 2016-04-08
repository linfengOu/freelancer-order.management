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

    private static DBConnectionManager instance; // 唯一实例  
    private static int clients; // 连接的客户端  
    private Vector < Driver > drivers = new Vector < > (); // 驱动集合  
    private Hashtable < String, DBConnectionPool > pools = new Hashtable < > (); // 连接池  
    private Properties dbProps; // 属性文件  
    private PrintWriter log; // 日志变量  

    /** 
     * 单例模式建构私有函数以防止其它对象创建本类实例 
     */
    private DBConnectionManager() {
        this.init();
    }

    /** 
     * 采用单例模式，返回唯一实例.如果是第一次调用此方法,则创建实例 
     *  
     * @return DBConnectionManager 唯一实例 
     */
    public static synchronized DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        clients++;
        return instance;
    }

    /** 
     * 获得一个可用的(空闲的)连接.如果没有可用连接,且已有连接数小于最大连接数 限制,则创建并返回新连接 
     *  
     * @param name 
     *            在属性文件中定义的连接池名字 
     * @return Connection 可用连接或null 
     */
    public Connection getConnection(String name) {
        DBConnectionPool dbPool = (DBConnectionPool) pools.get(name);
        if (dbPool != null) {
            return dbPool.getConnection();
        }
        return null;
    }

    /** 
     * 获得一个可用连接.若没有可用连接,且已有连接数小于最大连接数限制, 则创建并返回新连接. 否则,在指定的时间内等待其它线程释放连接. 
     *  
     * @param name 
     *            连接池名字 
     * @param time 
     *            以毫秒计的等待时间 
     * @return Connection 可用连接或null 
     */
    public Connection getConnection(String name, long time) {
        DBConnectionPool dbPool = (DBConnectionPool) pools.get(name);
        if (dbPool != null) {
            return dbPool.getConnection(time);
        }
        return null;
    }

    /** 
     * 将连接对象返回给由名字指定的连接池 
     *  
     * @param name 
     *            在属性文件中定义的连接池名字 
     * @param con 
     *            连接对象 
     */
    public void freeConnection(String name, Connection con) {
        DBConnectionPool dbPool = (DBConnectionPool) pools.get(name);
        if (dbPool != null) {
            dbPool.freeConnection(con);
        }
    }

    /** 
     * 关闭所有连接,撤销驱动程序的注册 
     */
    public synchronized void release() {
        // 等待直到最后一个客户程序调用  
        if (--clients != 0) {
            return;
        }
        Enumeration < DBConnectionPool > allPools = pools.elements();
        while (allPools.hasMoreElements()) {
            DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
            pool.release();
        }
        Enumeration < Driver > allDrivers = drivers.elements();
        while (allDrivers.hasMoreElements()) {
            Driver driver = (Driver) allDrivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log("撤销JDBC驱动程序 " + driver.getClass().getName() + "的注册");
            } catch (SQLException e) {
                log(e, "无法撤销下列JDBC驱动程序的注册: " + driver.getClass().getName());
            }
        }
    }

    /** 
     * 读取属性完成初始化 
     */
    private void init() {

        // 文件流输入方式  
        InputStream fileinputstream = null;
        try {
            fileinputstream = new FileInputStream("./src/db.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            dbProps = new Properties();
            dbProps.load(fileinputstream);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("不能读取属性文件. " + "请确保db.properties在CLASSPATH指定的路径中");
            return;
        }

        String logFile = dbProps.getProperty("logfile",
            "DBConnectionManager.log");
        try {
            log = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            System.err.println("无法打开日志文件: " + logFile);
            log = new PrintWriter(System.err);
        }
        // 加载驱动  
        loadDrivers(dbProps);
        // 创建连接池  
        createPools(dbProps);
    }

    /** 
     * 装载和注册所有JDBC驱动程序 
     *  
     * @param props 
     *            属性 
     */
    private void loadDrivers(Properties props) {
        String driverClasses = props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while (st.hasMoreElements()) {
            String driverClassName = st.nextToken().trim();
            try {
                Driver driver = (Driver) Class.forName(driverClassName)
                    .newInstance();
                DriverManager.registerDriver(driver);
                drivers.addElement(driver);
                log("成功注册JDBC驱动程序" + driverClassName);
            } catch (Exception e) {
                log("无法注册JDBC驱动程序: " + driverClassName + ", 错误: " + e);
            }
        }
    }

    /** 
     * 根据指定属性创建连接池实例. 
     *  
     * @param props 
     *            连接池属性 
     */
    private void createPools(Properties props) {
        Enumeration <? > propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                System.out.println(" poolName ||" + poolName + "|");
                String url = props.getProperty(poolName + ".url");
                if (url == null) {
                    log("没有为连接池" + poolName + "指定URL");
                    continue;
                }
                String user = props.getProperty(poolName + ".user");
                String password = props.getProperty(poolName + ".password");
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                int max;
                try {
                    max = Integer.valueOf(maxconn).intValue();
                } catch (NumberFormatException e) {
                    log("错误的最大连接数限制: " + maxconn + " .连接池: " + poolName);
                    max = 0;
                }
                DBConnectionPool pool = new DBConnectionPool(poolName, url,
                    user, password, max);
                pools.put(poolName, pool);
                log("成功创建连接池" + poolName);
            }
        }
    }

    /** 
     * 将文本信息写入日志文件 
     */
    private void log(String msg) {
        log.println(new Date() + ": " + msg);
    }

    /** 
     * 将文本信息与异常写入日志文件 
     */
    private void log(Throwable e, String msg) {
        log.println(new Date() + ": " + msg);
        e.printStackTrace(log);
    }

    /*************************************************************************** 
     ************************数据库连接池内部类************************************ 
     **************************************************************************/
    /** 
     *  
     * @功能：数据库连接池内类 此内部类定义了一个连接池.它能够根据要求创建新连接,直到预定的最大连接数为止. 
     *              在返回连接给客户程序之前,它能够验证连接的有效性. 
     * @创建人 gao_jie 
     * @创建日期 Jun 19, 2009 
     * @版本 1.0 
     *  
     */
    class DBConnectionPool {

        private String poolName; // 连接池名字  
        private String dbConnUrl; // 数据库的JDBC URL  
        private String dbUserName; // 数据库账号或null  
        private String dbPassWord; // 数据库账号密码或null  
        private int maxConn; // 此连接池允许建立的最大连接数  
        private int checkedOut; // 当前连接数  
        private Vector < Connection > freeConnections; // 保存所有可用连接  

        /** 
         * 创建新的连接池构造函数 
         *  
         * @param poolName 
         *            连接池名字 
         * @param dbConnUrl 
         *            数据库的JDBC URL 
         * @param dbUserName 
         *            数据库帐号或 null 
         * @param dbPassWord 
         *            密码或 null 
         * @param maxConn 
         *            此连接池允许建立的最大连接数 
         */
        public DBConnectionPool(String poolName, String dbConnUrl,
            String dbUserName, String dbPassWord, int maxConn) {
            this.poolName = poolName;
            this.dbConnUrl = dbConnUrl;
            this.dbUserName = dbUserName;
            this.dbPassWord = dbPassWord;
            this.maxConn = maxConn;
            this.freeConnections = new Vector < Connection > ();
        }

        /** 
         * 从连接池获得一个可用连接.如果没有空闲的连接且当前连接数小于最大连接 数限制,则创建新连接. 
         * 如原来登记为可用的连接不再有效,则从向量删除之,然后递归调用自己以尝试新的可用连接. 
         */
        @
        SuppressWarnings("resource")
        public synchronized Connection getConnection() {
            Connection conn = null; // 定义连接标量  
            if (freeConnections != null && freeConnections.size() > 0) {
                // 获取向量中第一个可用连接  
                conn = (Connection) freeConnections.firstElement();
                freeConnections.removeElementAt(0);
                try {
                    if (conn.isClosed()) {
                        log("从连接池" + poolName + "删除一个无效连接");
                        // 递归调用自己,尝试再次获取可用连接  
                        conn = getConnection();
                    }
                } catch (SQLException e) {
                    log("从连接池" + poolName + "删除一个无效连接");
                    // 递归调用自己,尝试再次获取可用连接  
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
         * 从连接池获取可用连接.可以指定客户程序能够等待的最长时间 参见前一个getConnection()方法. 
         *  
         * @param timeout 
         *            以毫秒计的等待时间限制 
         */
        public synchronized Connection getConnection(long timeout) {
            long startTime = System.currentTimeMillis();
            Connection conn = null; // 定义连接标量  
            while ((conn = getConnection()) == null) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if ((System.currentTimeMillis() - startTime) >= timeout) {
                    // wait()返回的原因是超时  
                    return null;
                }
            }
            return conn;
        }

        /** 
         * 创建新的连接 
         *  
         * @return 返回数据库连接 
         */
        private Connection newConnection() {
            Connection conn = null; // 定义连接标量  
            try {
                if (dbUserName == null) {
                    conn = DriverManager.getConnection(dbConnUrl);
                } else {
                    conn = DriverManager.getConnection(dbConnUrl, dbUserName,
                        dbPassWord);
                }
                log("连接池" + poolName + "创建一个新的连接");
            } catch (SQLException e) {
                log(e, "无法创建下列URL的连接: " + dbConnUrl);
                return null;
            }
            return conn;
        }

        /** 
         * 将不再使用的连接返回给连接池 
         *  
         * @param con 
         *            客户程序释放的连接 
         */
        public synchronized void freeConnection(Connection conn) {
            // 将指定连接加入到向量末尾  
            freeConnections.addElement(conn);
            checkedOut--;
            notifyAll(); // 删除等待队列中的所有线程  
        }

        /** 
         * 关闭所有连接 
         */
        public synchronized void release() {
            Enumeration < Connection > allConnections = freeConnections.elements();
            while (allConnections.hasMoreElements()) {
                Connection con = (Connection) allConnections.nextElement();
                try {
                    con.close();
                    log("关闭连接池" + poolName + "中的一个连接");
                } catch (SQLException e) {
                    log(e, "无法关闭连接池" + poolName + "中的连接");
                }
            }
            freeConnections.removeAllElements();
        }
    }
}