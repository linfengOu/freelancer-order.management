/**
 * 
 */
package com.freelancer.om.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.freelancer.om.dao.IUserDao;
import com.freelancer.om.model.User;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;

/**
 * @author Oliver
 *
 */
public class UserDao implements IUserDao {

  private DBConnectionManager dbcm;

  public UserDao(DBConnectionManager dbcm) {
    this.dbcm = dbcm;
  }

  @Override
  public User insertUser(User user) throws ExceptionMessage {
    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;
    ResultSet rs = null;
    StringBuffer sql = new StringBuffer();

    try {

      sql.append("select `username`, `name` from `user` where `username` = ? or `name` = ? limit 1");
      pstm = con.prepareStatement(sql.toString());
      pstm.setString(1, user.getUserName());
      pstm.setString(2, user.getName());
      rs = pstm.executeQuery();

      if (rs.next()) {
        if (user.getUserName() == rs.getString(1)) {
          throw new ExceptionMessage("登录名 " + user.getUserName() + " 已经被注册");
        }
        if (user.getName() == rs.getString(2)) {
          throw new ExceptionMessage("用户名 " + user.getName() + " 已经被注册");
        }
      }
      rs.close();
      pstm.close();

      sql.setLength(0);
      sql.append("insert into `user` (`username`, `name`, `pw`, `usertype`) " + "values (?,?,?,?)");
      pstm = con.prepareStatement(sql.toString());
      pstm.setString(1, user.getUserName());
      pstm.setString(2, user.getName());
      pstm.setString(3, user.getPw());
      pstm.setInt(4, user.getUserType());
      pstm.executeUpdate();

    } catch (SQLException e) {
      throw new ExceptionMessage("创建用户失败, 请重试. 错误信息: " + e.getMessage());
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
    user.setPw(null);
    return user;
  }

  @Override
  public void deleteUser(int uid) throws ExceptionMessage {
    throw new ExceptionMessage("目前不支持删除操作, 请联系管理员.");

  }

  @Override
  public void updatePw(int uid, Map<String, String> params) throws ExceptionMessage {
    boolean hasName = params.containsKey("name");
    boolean hasPw = params.containsKey("pw");

    if (!hasName && !hasPw) {
      return;
    }

    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;
    ResultSet rs = null;
    StringBuffer sql = new StringBuffer();

    try {
      if (hasName) {
        sql.append("select `uid` from `user` where `name` = ? limit 1");
        pstm = con.prepareStatement(sql.toString());
        pstm.setString(1, params.get("name"));
        rs = pstm.executeQuery();
        if (rs.next()) {
          throw new ExceptionMessage("用户名 " + params.get("name") + " 已经被注册");
        }
        rs.close();
        pstm.close();
      }
      int index = 0;
      sql.setLength(0);
      sql.append("update `user` set");
      if (hasName) {
        sql.append(" `name` = ?");
        index++;
      }
      if (hasPw) {
        sql.append(" `pw` = ?");
        index++;
      }
      sql.append(" where `uid` = ?");
      index++;
      pstm = con.prepareStatement(sql.toString());
      pstm.setInt(index, uid);
      index --;
      if (hasPw) {
        pstm.setString(index, params.get("pw"));
        index --;
      }
      if (hasName) {
        pstm.setString(index, params.get("name"));
      }
      pstm.executeUpdate();
      
    } catch (SQLException e) {
      throw new ExceptionMessage("更新用户失败, 请重试. 错误信息: " + e.getMessage());
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
  }

  @Override
  public User verifyUser(String username) throws ExceptionMessage {
    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;
    ResultSet rs = null;
    User user = null;
    StringBuffer sql = new StringBuffer();
    try {
      sql.append("select `pw`, `usertype` from `user` where `username` = ? limit 1");
      pstm = con.prepareStatement(sql.toString());
      pstm.setString(1, username);
      rs = pstm.executeQuery();
      if (rs.next()) {
        user = new User();
        user.setPw(rs.getString(1));
        user.setUserType(rs.getInt(2));
      }
    } catch (SQLException e) {
      throw new ExceptionMessage("验证用户失败, 请重试. 错误信息: " + e.getMessage());
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
    return user;
  }

  @Override
  public User getUser(String name) throws ExceptionMessage {
    Connection con = dbcm.getConnection("mysql");
    PreparedStatement pstm = null;
    ResultSet rs = null;
    User user = null;
    StringBuffer sql = new StringBuffer();
    try {
      sql.append("select `uid`, `username`, `name`, `usertype`, `joindate` from `user` where `name` = ? limit 1");
      pstm = con.prepareStatement(sql.toString());
      pstm.setString(1, name);
      rs = pstm.executeQuery();
      if (rs.next()) {
        user = new User();
        user.setUid(rs.getInt(1));
        user.setUserName(rs.getString(2));
        user.setName(rs.getString(3));
        user.setUserType(rs.getInt(4));
        if (rs.getTimestamp(5) != null) {
          user.setJoinDate(rs.getTimestamp(5).getTime());
        }
      }
    } catch (SQLException e) {
      throw new ExceptionMessage("查找用户失败, 请重试. 错误信息: " + e.getMessage());
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
    return user;
  }

}
