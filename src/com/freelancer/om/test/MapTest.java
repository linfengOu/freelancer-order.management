package com.freelancer.om.test;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.freelancer.om.dao.IOrderDao;
import com.freelancer.om.dao.impl.OrderDao;
import com.freelancer.om.model.User;
import com.freelancer.om.service.ITransactionService;
import com.freelancer.om.service.impl.TransactionService;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;
import com.freelancer.om.util.PasswordHash;

public class MapTest {

	@Test
	public void test() {
	  User user = new User();
	  
	  user.setUserName("boss_A");
	  user.setPw("123");
	  
	  ITransactionService its = new TransactionService(DBConnectionManager.getInstance());
	  
	  try {
      System.out.println(its.verifyUser(user));
    } catch (ExceptionMessage e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	}

}
