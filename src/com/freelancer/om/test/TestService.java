package com.freelancer.om.test;

import java.util.Date;

import com.freelancer.om.dao.IOrderDao;
import com.freelancer.om.dao.impl.OrderDao;
import com.freelancer.om.model.Order;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;

public class TestService {
	private DBConnectionManager dbcm;
	
	public TestService (DBConnectionManager dbcm) {
		this.dbcm = dbcm;
	}
	
	public int insertRandom() throws ExceptionMessage {

		String[] titles = {"项目A","项目B","项目C"};
		String[] places = {"码头","菜市场","车站","二炮基地","学校","区政府"};
		String[] aParty = {"老板A","老板B"};
		String[] bParty = {"雇员A","雇员B","雇员C","雇员D","雇员E"};
		
		int aDiv = (int) (Math.random()*9);
		long d = new Date().getTime() + 100000000 + Math.round(Math.random()*10000000000l);

		Order order = new Order();
		order.setTitle(titles[(int) (Math.random()*100)%3] + (int) (Math.random()*100));
		order.setPrice(Math.round(Math.random()*100000));
		order.setaDiv(aDiv);
		order.setbDiv(10 - aDiv);
		order.setDeadline(new java.util.Date(d));
		order.setPlace(places[(int) (Math.random()*100)%6]);
		order.setDeposit(Math.round(Math.random()*100));
		order.setaParty(aParty[(int) (Math.random()*100)%2]);
		order.setbParty(bParty[(int) (Math.random()*100)%5]);
		order.setStatus(0);
		order.setIsRead(0);
		
		OrderDao od = new OrderDao(dbcm);
		order = od.insertOrder(order);
		return order.getOid();
	}
	
	public Order findOrder() throws ExceptionMessage{
		
		IOrderDao od = new OrderDao(dbcm);
		Order o = null;
		o = od.getOrderDetail(1);
		return o;
	}
}
