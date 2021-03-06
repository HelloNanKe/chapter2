package org.smart4j.chapter2.helper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.omg.CORBA.MARSHAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.chapter2.util.PropsUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public final class DatabaseHelper {



    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<>();

    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    private static final BasicDataSource DATA_SOURCE;

    private static Logger logger=LoggerFactory.getLogger(DatabaseHelper.class);
    static {
        Properties conf = PropsUtil.loadProps("config.properties");
        String driver = conf.getProperty("jdbc.driver");
        String url = conf.getProperty("jdbc.url");
        String userName = conf.getProperty("jdbc.username");
        String password = conf.getProperty("jdbc.password");

        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(driver);
        DATA_SOURCE.setUrl(url);
        DATA_SOURCE.setUsername(userName);
        DATA_SOURCE.setPassword(password);

    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    public static Connection getConnection() {

        Connection conn = CONNECTION_HOLDER.get();
        if (conn == null) {
            try {
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("获取数据库连接失败", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }

        return conn;
    }


    /**
     * 执行一个sql文件
     * @param sqlPath 文件名称
     */
    public static void executeFile(String sqlPath)  {
        InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream(sqlPath);

        BufferedReader reader=new BufferedReader(new InputStreamReader(is));
        String sql;
        try {
            while ((sql=reader.readLine())!=null){
                executeUpdate(sql);
            }
        } catch (IOException e) {
            logger.error("执行sql文件失败",e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 查询实体列表
     *
     * @param entityClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object... params) {
        List<T> entityList = null;
        try {
            Connection conn = getConnection();
            entityList = QUERY_RUNNER.query(conn, sql, new BeanListHandler<T>(entityClass));
        } catch (SQLException e) {
            LOGGER.error("查询数据列表失败", e);
            throw new RuntimeException(e);
        }
        return entityList;
    }

    public static <T> T queryEntity(Class<T> entityClass, String sql, Object... params) {
        T entity = null;
        Connection conn = getConnection();
        try {
            entity = QUERY_RUNNER.query(conn, sql, new BeanHandler<T>(entityClass));
        } catch (SQLException e) {
            LOGGER.error("查询单个实体失败", e);
            new RuntimeException(e);
        }
        return entity;
    }


    /**
     * 查询实体,可用于多表
     *
     * @param sql
     * @param params
     * @return
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> result = null;
        try {
            Connection conn = getConnection();
            result = QUERY_RUNNER.query(conn, sql, new MapListHandler(), params);
        } catch (SQLException e) {
            LOGGER.error("执行sql语句失败", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 更新语句
     *
     * @param sql
     * @param params
     * @return
     */
    public static int executeUpdate(String sql, Object... params) {
        int rows = 0;
        try {
            Connection conn = getConnection();
            rows = QUERY_RUNNER.update(conn, sql, params);
        } catch (SQLException e) {
            LOGGER.error("执行更新语句失败", e);
            throw new RuntimeException(e);
        }

        return rows;
    }

    /**
     * 插入实体
     *
     * @param entityClass
     * @param fieldMap
     * @param <T>
     * @return
     */
    public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            LOGGER.error("要插入的实体集合为空");
            return false;
        }

        String sql = "insert into " + getTableName(entityClass);
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append(", ");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(", "), columns.length(), ") ");
        values.replace(values.lastIndexOf(", "), values.length(), ")");

        sql += columns + " VALUES " + values;

        Object[] params = fieldMap.values().toArray();
        return executeUpdate(sql, params) == 1;
    }

    /**
     * 更新一个实体
     *
     * @param entityClass
     * @param id
     * @param fieldMap
     * @param <T>
     * @return
     */
    public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            LOGGER.error("要插入的实体集合为空");
            return false;
        }

        String sql = "update " + getTableName(entityClass) + " set ";
        StringBuilder colomns = new StringBuilder();
        for (String fieldName : fieldMap.keySet()) {
            colomns.append(fieldName).append("=?, ");
        }
        sql += colomns.substring(0, colomns.lastIndexOf(", ")) + " where id=?";

        List<Object> paramList = new ArrayList<>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);

        Object[] params = paramList.toArray();
        return executeUpdate(sql, colomns) == 1;
    }

    /**
     * 删除一个实体
     *
     * @param entityClass
     * @param id
     * @param <T>
     * @return
     */
    public static <T> boolean deleteEntity(Class<T> entityClass, long id) {
        String sql = "delete from " + getTableName(entityClass) + " where id=?";
        return executeUpdate(sql, id) == 1;
    }

    /**
     * 根据实体获取该实体对应的表明名
     *
     * @param entityClass
     * @return
     */
    private static String getTableName(Class<?> entityClass) {
        return entityClass.getSimpleName();
    }

}
