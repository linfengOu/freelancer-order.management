/**
 * 
 */
package com.freelancer.om.dao;

import java.util.Map;

import com.freelancer.om.model.User;
import com.freelancer.om.util.ExceptionMessage;

/**
 * @author Oliver
 *
 */
public interface IUserDao {
  
  /**
   * Insert a new user, username and name duplication will be checked
   * @param user
   * @return
   * @throws ExceptionMessage 
   */
  public User insertUser(User user) throws ExceptionMessage;
  
  /**
   * delete a user
   * @param uid
   * @throws ExceptionMessage 
   */
  public void deleteUser(int uid) throws ExceptionMessage;
  
  /**
   * update password of a user
   * @param oid
   * @param pw
   * @throws ExceptionMessage 
   */
  public void updatePw(int uid, Map<String, String> params) throws ExceptionMessage;
  
  /**
   * return password to verify
   * @param user
   * @return
   * @throws ExceptionMessage 
   */
  public User verifyUser(String username) throws ExceptionMessage;
  
  /**
   * get one user by name
   * @param name
   * @return
   * @throws ExceptionMessage 
   */
  public User getUser(String name) throws ExceptionMessage;
  
}