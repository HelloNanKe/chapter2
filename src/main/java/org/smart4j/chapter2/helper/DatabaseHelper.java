package org.smart4j.chapter2.helper;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.chapter2.service.CustomerService;
import org.smart4j.chapter2.util.PropsUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class DatabaseHelper {


    private static final String DRIVER;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static  final ThreadLocal<Connection> CONNECTION_HOLDER=new ThreadLocal<>();

    private static final QueryRunner QUERY_RUNNER=new QueryRunner();


    static {
        Properties conf = PropsUtil.loadProps("config.properties");
        DRIVER = conf.getProperty("jdbc.driver");
        URL = conf.getProperty("jdbc.url");
        USERNAME = conf.getProperty("jdbc.username");
        PASSWORD = conf.getProperty("jdbc.password");

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            LOGGER.error("不能加载jdbc驱动", e);
        }

    }

    /**
     * 获取数据库连接
     * @return
     */
    public static Connection getConnection() {

        Connection conn=CONNECTION_HOLDER.get();
        if(conn==null){
            try {
                conn= DriverManager.getConnection(URL,USERNAME,PASSWORD);
            } catch (SQLException e) {
                LOGGER.error("获取数据库连接失败",e);
                throw new RuntimeException(e);
            }finally {
                CONNECTION_HOLDER.set(conn);
            }
        }

        return conn;
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection(){
        Connection conn=CONNECTION_HOLDER.get();
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("关闭数据库连接失败",e);
                new RuntimeException(e);
            }finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    /**
     * 查询实体列表
     * @param entityClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public static <T> List<T> queryEntityList(Class<T> entityClass,String sql,Object ... params){
        List<T> entityList = null;
        try {
            Connection conn=getConnection();
            entityList=QUERY_RUNNER.query(conn,sql,new BeanListHandler<T>(entityClass));
        } catch (SQLException e) {
            LOGGER.error("查询数据列表失败",e);
            throw new RuntimeException(e);
        } finally {
            closeConnection();
        }

        return entityList;
    }

    public static<T> T queryEntity(Class<T> entityClass,String sql,Object ...params){
        T entity = null;
        Connection conn=getConnection();
        try {
            entity=QUERY_RUNNER.query(conn,sql,new BeanHandler<T>(entityClass));
        } catch (SQLException e) {
            LOGGER.error("查询单个实体失败",e);
            new RuntimeException(e);
        }finally {
            closeConnection();
        }
        return entity;
    }


}
