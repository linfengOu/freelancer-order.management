package com.freelancer.om.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		String[] aParty = {"boss_a","boss_b"};
//		String[] bParty = {"staff_a","staff_b","staff_c","staff_d","staff_e"};
		
		int aDiv = (int) (Math.random()*9);
		long d = new Date().getTime() + 100000000 + Math.round(Math.random()*10000000000l);

		Order order = new Order();
		order.setTitle(titles[(int) (Math.random()*100)%3] + (int) (Math.random()*100));
		order.setPrice(Math.round(Math.random()*100000));
		order.setaDiv(aDiv);
		order.setbDiv(10 - aDiv);
		order.setDeadline(new java.util.Date(d).getTime());
		order.setPlace(places[(int) (Math.random()*100)%6]);
		order.setDeposit(Math.round(Math.random()*100));
		order.setaParty(aParty[(int) (Math.random()*100)%2]);
//		order.setbParty(bParty[(int) (Math.random()*100)%5]);
		order.setStatus(0);
		order.setIsRead(0);
		
		OrderDao od = new OrderDao(dbcm);
		order = od.insertOrder(order);
		return order.getOid();
	}
	
	public Order findOrder(int i) throws ExceptionMessage{
		
		IOrderDao od = new OrderDao(dbcm);
		Order o = null;
		o = od.getOrderDetail(i);
		return o;
	}
	
	public List<Order> checkCondition() throws ExceptionMessage {
		IOrderDao od = new OrderDao(dbcm);

		List<List<Object>> queries = new ArrayList<>();
		List<List<Object>> or_queries = new ArrayList<>();
		List<Object> query = new ArrayList<>();

		query = new ArrayList<>();
		query.add("COND");
		query.add("li");
		query.add("title");
		query.add("%A%");
		queries.add(query);
		 
    query = new ArrayList<>();
    query.add("COND");
    query.add("gt");
    query.add("price");
    query.add(80000f);
    or_queries.add(query);
    
    query = new ArrayList<>();
    query.add("COND");
    query.add("lt");
    query.add("price");
    query.add(5000f);
    or_queries.add(query);
  	
		query = new ArrayList<>();
		query.add("COND");
		query.add("or");
		query.add(or_queries);
		queries.add(query);

    query = new ArrayList<>();
    query.add("SORT");
    query.add("price");
    query.add("asc");
    queries.add(query);
		
		return od.getOrdersWithQueries(queries, 0, 0);
	}
}
