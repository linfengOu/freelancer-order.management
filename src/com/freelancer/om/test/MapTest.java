package com.freelancer.om.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.freelancer.om.dao.IOrderDao;
import com.freelancer.om.dao.impl.OrderDao;

public class MapTest {

	@Test
	public void test() {
		IOrderDao od = new OrderDao(null);

		List<List<Object>> queries = new ArrayList<>();
		List<List<Object>> or_queries = new ArrayList<>();
		List<Object> query = new ArrayList<>();

		query.add("COND");
		query.add("eq");
		query.add("title");
		query.add("test_title");
		queries.add(query);
		
		query = new ArrayList<>();
		query.add("COND");
		query.add("lt");
		query.add("createdate");
		query.add(new java.util.Date(1460373230828l));
		queries.add(query);
		
		query = new ArrayList<>();
		query.add("COND");
		query.add("lt");
		query.add("deposit");
		query.add(100f);
		or_queries.add(query);
		
		query = new ArrayList<>();
		query.add("COND");
		query.add("gt");
		query.add("deposit");
		query.add(500f);
		or_queries.add(query);
		
		query = new ArrayList<>();
		query.add("COND");
		query.add("or");
		query.add(or_queries);
		queries.add(query);
		
	}

}
