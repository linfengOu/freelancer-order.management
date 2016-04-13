package com.freelancer.om.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.freelancer.om.dao.IUserDao;
import com.freelancer.om.dao.impl.UserDao;
import com.freelancer.om.model.User;
import com.freelancer.om.service.ITransactionService;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;
import com.freelancer.om.util.PasswordHash;

public class TransactionService implements ITransactionService {
  
  private DBConnectionManager dbcm;
  
  public TransactionService(DBConnectionManager dbcm) {
    this.dbcm = dbcm;
  }

  @Override
  public boolean verifyUser(User user) throws ExceptionMessage {
    IUserDao userDao = new UserDao(dbcm);
    
    User correctUser = userDao.verifyUser(user.getUserName());
    
    if (correctUser != null) {
      try {
        return PasswordHash.validatePassword(user.getPw(), correctUser.getPw());
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {}
    }
    
    return false;
  }

}
