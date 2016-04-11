package com.freelancer.om.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.freelancer.om.dao.IOrderDao;
import com.freelancer.om.model.Order;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;

/**
 * @author Oliver
 *         Implement interface IOrdarDao
 */
public class OrderDao implements IOrderDao {

  /**
   * Database connection manager
   */
  private DBConnectionManager dbcm;

  /**
   * Construct class instance with given Database manager
   * 
   * @param dbcm
   */
  public OrderDao(DBConnectionManager dbcm) {
    this.dbcm = dbcm;
  }

  @Override
  public Order insertOrder(Order order) throws ExceptionMessage {
    Connection con = dbcm.getConnection("mysql");
    Statement stmt = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    StringBuffer sql = new StringBuffer();

    try {
      sql.append("insert into `order` (`title`,`price`,`adiv`,`bdiv`,`deadline`,`place`,`deposit`) "
          + "values (?,?,?,?,?,?,?)");
      pstm = con.prepareStatement(sql.toString());
      pstm.setString(1, order.getTitle());
      pstm.setFloat(2, order.getPrice());
      pstm.setInt(3, order.getaDiv());
      pstm.setInt(4, order.getbDiv());
      pstm.setTimestamp(5, new Timestamp(order.getDeadline().getTime()));
      pstm.setString(6, order.getPlace());
      pstm.setFloat(7, order.getDeposit());
      pstm.executeUpdate();
      pstm.close();

      sql.setLength(0);
      sql.append("select last_insert_id()");
      stmt = con.createStatement();
      rs = stmt.executeQuery(sql.toString());
      if (rs.next()) {
        order.setOid(rs.getInt(1));
      }
      rs.close();
      stmt.close();

      if (order.getDesc() != null) {
        sql.setLength(0);
        sql.append("insert into `orderdesc` (`oid`,`desc`) values (?,?)");
        pstm = con.prepareStatement(sql.toString());
        pstm.addBatch(sql.toString());
        pstm.setInt(1, order.getOid());
        pstm.setString(2, order.getDesc());
        pstm.executeUpdate();
        pstm.close();
      }

      sql.setLength(0);
      sql.append("insert into `orderstatus` (`oid`,`aparty`,`bparty`,`status`,`isread`) values (?,?,?,?,?)");
      pstm = con.prepareStatement(sql.toString());
      pstm.setInt(1, order.getOid());
      pstm.setString(2, order.getaParty());
      pstm.setString(3, order.getbParty());
      pstm.setInt(4, order.getStatusCode());
      pstm.setInt(5, order.getIsRead());
      pstm.executeUpdate();
      pstm.close();
      
      sql.setLength(0);
      sql.append("select `name` from `user` where `username` = ? limit 1");
      pstm = con.prepareStatement(sql.toString());
      pstm.setString(1, order.getaParty());
      rs = pstm.executeQuery();
      order.setaParty(null);
      if (rs.next()) {
        order.setaParty(rs.getString(1));
      }

    } catch (Exception e) {
      throw new ExceptionMessage("创建订单失败, 请重试. 错误信息: " + e.getMessage());
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (pstm != null) {
          pstm.close();
        }
      } catch (SQLException e) {}
      dbcm.freeConnection("mysql", con);
    }

    return order;
  }

  @Override
  public void deleteOrder(int oid) throws ExceptionMessage {
    throw new ExceptionMessage("目前不支持删除操作, 请联系管理员.");
  }

  @Override
  public void update(int oid, Map<String, Object> params) throws ExceptionMessage {
    boolean hasOrder = false, hasDesc = false, hasStatus = false;
    for (String key : params.keySet()) {
      switch (key) {
        case "price":
        case "deposit":
        case "place":
        case "adiv":
        case "bdiv":
        case "deadline":
          hasOrder = true;
          break;
        case "desc":
          hasDesc = true;
          break;
        case "status":
        case "isread":
          hasStatus = true;
          break;
      }
    }
    if (hasOrder) {
      updateOrder(oid, params);
    }
    if (hasDesc) {
      updateOrderDesc(oid, params.get("desc"));
    }
    if (hasStatus) {
      updateOrderStatus(oid, params);
    }
  }

  /**
   * update table `order`, invoked by function 'update'
   * 
   * @param oid
   * @param params
   * @throws ExceptionMessage
   */
  private void updateOrder(int oid, Map<String, Object> params) throws ExceptionMessage {
    StringBuffer sql = new StringBuffer();
    sql.append("update `order` set ");
    for (String key : params.keySet()) {
      switch (key) {
        case "price":
        case "deposit":
        case "place":
        case "adiv":
        case "bdiv":
        case "deadline":
          sql.append("`" + key + "`=?,");
          break;
      }
    }
    sql.deleteCharAt(sql.length() - 1);
    sql.append(" where `oid`=?");

    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;

    try {
      pstm = con.prepareStatement(sql.toString());
      int i = 1;
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        switch (entry.getKey()) {
          case "place":
            pstm.setString(i, (String) entry.getValue());
            i++;
            break;
          case "price":
          case "deposit":
            pstm.setFloat(i, (float) entry.getValue());
            i++;
            break;
          case "adiv":
          case "bdiv":
            pstm.setInt(i, (int) entry.getValue());
            i++;
            break;
          case "deadline":
            Date utilDate = (Date) entry.getValue();
            Timestamp sqlDate = new Timestamp(utilDate.getTime());
            pstm.setTimestamp(i, sqlDate);
            i++;
            break;
        }
      }
      pstm.setInt(i, oid);
      pstm.executeUpdate();
    } catch (Exception e) {
      throw new ExceptionMessage("更新订单失败, 请重试. 错误信息: " + e.getMessage());
    } finally {
      try {
        if (pstm != null) {
          pstm.close();
        }
      } catch (SQLException e) {}
      dbcm.freeConnection("mysql", con);
    }
  }

  /**
   * update table `orderdesc`, invoked by function 'update'
   * 
   * @param oid
   * @param desc
   * @throws ExceptionMessage
   */
  private void updateOrderDesc(int oid, Object desc) throws ExceptionMessage {
    String sql = "insert `orderdesc` (`oid`, `desc`) values (?,?) on duplicate key update `desc`=?";

    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;

    try {
      pstm = con.prepareStatement(sql.toString());
      pstm.setInt(1, oid);
      pstm.setString(2, (String) desc);
      pstm.setString(3, (String) desc);
      pstm.executeUpdate();
    } catch (Exception e) {
      throw new ExceptionMessage("更新订单失败, 请重试. 错误信息: " + e.getMessage());
    } finally {
      try {
        if (pstm != null) {
          pstm.close();
        }
      } catch (SQLException e) {}
      dbcm.freeConnection("mysql", con);
    }
  }

  /**
   * update table `orderstatsu`, invoked by function 'update'
   * 
   * @param oid
   * @param params
   * @throws ExceptionMessage
   */
  private void updateOrderStatus(int oid, Map<String, Object> params) throws ExceptionMessage {
    StringBuffer sql = new StringBuffer();
    sql.append("update `orderstatus` set ");
    for (String key : params.keySet()) {
      if (key == "status" || key == "isread") {
        sql.append("`" + key + "`=?,");
      }
    }
    sql.deleteCharAt(sql.length() - 1);
    sql.append(" where `oid`=?");

    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;

    try {
      pstm = con.prepareStatement(sql.toString());
      int i = 1;
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        if (entry.getKey() == "status" || entry.getKey() == "isread") {
          pstm.setInt(i, (int) entry.getValue());
          i++;
        }
      }
      pstm.setInt(i, oid);
      pstm.executeUpdate();
    } catch (Exception e) {
      throw new ExceptionMessage("更新订单失败, 请重试. 错误信息: " + e.getMessage());
    } finally {
      try {
        if (pstm != null) {
          pstm.close();
        }
      } catch (SQLException e) {}
      dbcm.freeConnection("mysql", con);
    }
  }

  @Override
  public Order getOrderDetail(int oid) throws ExceptionMessage {
    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;
    ResultSet rs = null;
    String sql = "select o.`oid`,`title`,`price`,`adiv`,`bdiv`,`createdate`,`enddate`,`deadline`,"
        + "`place`,`deposit`,`desc`,auser.`name`,buser.`name`,`status`,`isread` "
        + "from `order` as o join `orderstatus` as os on o.oid = os.oid "
        + "left join `user` as auser on os.`aparty` = auser.`username` "
        + "left join `user` as buser on os.`bparty` = buser.`username` "
        + "left join `orderdesc` as od on o.oid = od.oid where o.oid=? limit 1";
    Order od = new Order();

    try {
      pstm = con.prepareStatement(sql);
      pstm.setInt(1, oid);
      rs = pstm.executeQuery();

      if (rs.next()) {
        od.setOid(rs.getInt(1));
        od.setTitle(rs.getString(2));
        od.setPrice(rs.getFloat(3));
        od.setaDiv(rs.getInt(4));
        od.setbDiv(rs.getInt(5));
        if (rs.getTimestamp(6) != null) {
          od.setCreateDate(rs.getTimestamp(6).getTime());
        }
        if (rs.getTimestamp(7) != null) {
          od.setEndDate(rs.getTimestamp(7).getTime());
        }
        if (rs.getTimestamp(8) != null) {
          od.setDeadline(rs.getTimestamp(8).getTime());
        }
        od.setPlace(rs.getString(9));
        od.setDeposit(rs.getFloat(10));
        od.setDesc(rs.getString(11));
        od.setaParty(rs.getString(12));
        od.setbParty(rs.getString(13));
        od.setStatus(rs.getInt(14));
        od.setIsRead(rs.getInt(15));
      }
    } catch (SQLException e) {
      throw new ExceptionMessage("查询订单失败, 请重试. 错误信息: " + e.getMessage());
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (pstm != null) {
          pstm.close();
        }
      } catch (SQLException e) {}
      dbcm.freeConnection("mysql", con);
    }
    return od;
  }

  @Override
  public List<Order> getOrdersWithQueries(List<List<Object>> queries, int offset, int size) throws ExceptionMessage {
    StringBuffer sql = new StringBuffer();
    StringBuffer sqlCondition = new StringBuffer();
    StringBuffer sqlSort = new StringBuffer();
    List<Order> orders = new ArrayList<>();

    // to setup sql string
    sql.append("select o.`oid`, `title`, `price`, auser.`name`, buser.`name`, `createdate`, `status`, `isread` "
        + "from `order` as o join `orderstatus` as os on o.oid = os.oid "
        + "left join `user` as auser on os.`aparty` = auser.`username` "
        + "left join `user` as buser on os.`bparty` = buser.`username`");
    sqlCondition.append(" where");
    sqlSort.append(" order by");

    try {
      sqlCondition.append(setupConditionQuery(queries, "and"));
      sqlSort.append(setupSortQuery(queries));

      if (sqlCondition.length() > 6) {
        sql.append(sqlCondition.toString());
      }
      if (sqlSort.length() > 9) {
        sql.append(sqlSort.toString());
      }

      // to execute sql
      Connection con = dbcm.getConnection("mysql");
      PreparedStatement pstm = null;
      ResultSet rs = null;
      Order od = null;

      pstm = con.prepareStatement(sql.toString());
      int index = 1;
      Map<String, Object> pi = null;
      pi = setupPstm(queries, pstm, index);
      pstm = (PreparedStatement) pi.get("pstm");

      System.out.println(pstm.toString());

      rs = pstm.executeQuery();
      while (rs.next()) {
        od = new Order();
        od.setOid(rs.getInt(1));
        od.setTitle(rs.getString(2));
        od.setPrice(rs.getFloat(3));
        od.setaParty(rs.getString(4));
        od.setbParty(rs.getString(5));
        if (rs.getTimestamp(6) != null) {
          od.setCreateDate(rs.getTimestamp(6).getTime());
        }
        od.setStatus(rs.getInt(7));
        od.setIsRead(rs.getInt(8));
        orders.add(od);
      }
    } catch (Exception e) {
      throw new ExceptionMessage("查询订单失败, 请重试. 错误信息: " + e.getMessage());
    }

    return orders;
  }

  /**
   * use given queries to set up sql segment like ' where `col` = ? '
   * 
   * @param queries
   * @param link
   * @return
   */
  @SuppressWarnings("unchecked")
  private String setupConditionQuery(List<List<Object>> queries, String link) {
    StringBuffer sqlCondition = new StringBuffer();
    List<Object> query = null;
    for (int i = 0; i < queries.size(); i++) {
      query = (List<Object>) queries.get(i);
      if ((String) query.get(0) == "COND") {
        switch ((String) query.get(1)) {
          case "eq":
            sqlCondition.append(" `").append(query.get(2)).append("` = ? ").append(link);
            break;
          case "ne":
            sqlCondition.append(" `").append(query.get(2)).append("` != ? ").append(link);
            break;
          case "is":
            sqlCondition.append(" `").append(query.get(2)).append("` is ? ").append(link);
            break;
          case "not":
            sqlCondition.append(" `").append(query.get(2)).append("` not ? ").append(link);
            break;
          case "lt":
            sqlCondition.append(" `").append(query.get(2)).append("` < ? ").append(link);
            break;
          case "gt":
            sqlCondition.append(" `").append(query.get(2)).append("` > ? ").append(link);
            break;
          case "lte":
            sqlCondition.append(" `").append(query.get(2)).append("` <= ? ").append(link);
            break;
          case "gte":
            sqlCondition.append(" `").append(query.get(2)).append("` >= ? ").append(link);
            break;
          case "li":
            sqlCondition.append(" `").append(query.get(2)).append("` like ? ").append(link);
            break;
          case "nl":
            sqlCondition.append(" `").append(query.get(2)).append("` not like ? ").append(link);
            break;
          case "or":
            sqlCondition.append(" (").append(setupConditionQuery((List<List<Object>>) query.get(2), "or")).append(") ")
                .append(link);
            break;
        }
      }
    }
    if (sqlCondition.length() > 0) {
      sqlCondition.delete(sqlCondition.length() - link.length(), sqlCondition.length());
    }
    return sqlCondition.toString();
  }

  /**
   * set up 'order by'
   * 
   * @param queries
   * @return
   */
  private String setupSortQuery(List<List<Object>> queries) {
    StringBuffer sqlSort = new StringBuffer();
    List<Object> query = null;
    for (int i = 0; i < queries.size(); i++) {
      query = (List<Object>) queries.get(i);
      if ((String) query.get(0) == "SORT") {
        sqlSort.append(" `").append(query.get(1)).append("` ").append(query.get(2)).append(",");
      }
    }
    if (sqlSort.length() > 0) {
      sqlSort.deleteCharAt(sqlSort.length() - 1);
    }
    return sqlSort.toString();
  }

  /**
   * Set up preparedStatement by given queries
   * 
   * @param queries
   * @param pstm
   * @param index
   * @return
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> setupPstm(List<List<Object>> queries, PreparedStatement pstm, int index)
      throws SQLException {
    List<Object> query = null;
    Map<String, Object> pi = null;
    for (int i = 0; i < queries.size(); i++) {
      query = (List<Object>) queries.get(i);
      if (query.get(0) == "COND") {
        if (query.get(2) instanceof List) {
          pi = setupPstm((List<List<Object>>) query.get(2), pstm, index);
          pstm = (PreparedStatement) pi.get("pstm");
          index = (int) pi.get("index");
        } else {
          pstm.setObject(index, query.get(3));
          index++;
        }
      }
    }
    pi = new HashMap<>();
    pi.put("pstm", pstm);
    pi.put("index", index);
    return pi;
  }

}
