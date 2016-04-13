/**
 * 
 */
package com.freelancer.om.service;

import com.freelancer.om.model.User;
import com.freelancer.om.util.ExceptionMessage;

/**
 * @author Oliver
 *
 */
public interface ITransactionService {
  
  public boolean verifyUser(User user) throws ExceptionMessage;
}
