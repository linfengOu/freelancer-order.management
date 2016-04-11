/**
 * 
 */
package com.freelancer.om.dao;

import com.freelancer.om.model.User;

/**
 * @author Oliver
 *
 */
public interface IUserDao {
  
  /**
   * Insert a new user, username and name duplication will be checked
   * @param user
   * @return
   */
  public User insertUser(User user);
  
  /**
   * delete a user
   * @param uid
   */
  public void deleteUser(int uid);
  
  /**
   * update password of a user
   * @param oid
   * @param pw
   */
  public void updatePw(int oid, String pw);
  
  /**
   * @param user
   * @return
   */
  public boolean verifyUser(User user);
  
}