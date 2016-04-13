/**
 * 
 */
package com.freelancer.om.model;

import java.util.Date;

/**
 * @author Oliver
 *
 */
public class User {
  
  private int uid;
  
  private String userName;
  
  private String name;
  
  private String pw;
  
  private int userType;
  
  private Date joinDate;

  public int getUid() {
    return uid;
  }

  public String getUserName() {
    return userName;
  }

  public String getName() {
    return name;
  }

  public String getPw() {
    return pw;
  }

  public int getUserType() {
    return userType;
  }

  public Date getJoinDate() {
    return joinDate;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPw(String pw) {
    this.pw = pw;
  }

  public void setUserType(int userType) {
    this.userType = userType;
  }

  public void setJoinDate(long timestamp) {
    this.joinDate = new Date(timestamp);
  }
  
}
