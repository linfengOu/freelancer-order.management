/**
 * 
 */
package com.freelancer.om.dao;

import java.util.List;
import java.util.Map;

import com.freelancer.om.model.Order;
import com.freelancer.om.util.ExceptionMessage;

/**
 * @author Oliver
 *
 */
public interface IOrderDao {
	
	/**
	 * insert given order object into corresponding data tables 
	 * @param order
	 * @return inserted order object
	 * @throws ExceptionMessage
	 */
	public Order insertOrder(Order order) throws ExceptionMessage;
	
	/**
	 * delete order entry by order id ( not implement yet )
	 * @param oid
	 * @throws ExceptionMessage
	 */
	public void deleteOrder(int oid) throws ExceptionMessage;
	
	/**
	 * update order entry by given order and the parameters that to be updated
	 * <br/>Param should format as:
	 * <br/>  - Map < filed, value >
	 * @param oid
	 * @param params
	 * @throws ExceptionMessage
	 */
	public void update(int oid, Map<String, Object> params) throws ExceptionMessage;
	
	/**
	 * get single detailed order information
	 * @param oid
	 * @return
	 * @throws ExceptionMessage 
	 */
	public Order getOrderDetail(int oid) throws ExceptionMessage;
	
	/**
	 * Get orders with given queries.
	 * <br/>Query should format as:
	 * <br/>  - condition: [ "COND", "eq/ne/is/not/lt/gt/lte/gte/li/nl", "field", value ]
	 * <br/>  - condition: [ "COND", "or", condition_list ]
	 * <br/>  - sort:      [ "SORT", "field", "asc/desc"]
	 * @param queries
	 * @param skip
	 * @param size
	 * @return
	 * @throws ExceptionMessage 
	 */
	public List<Order> getOrdersWithQueries(List<List<Object>> queries, int offset, int size) throws ExceptionMessage;
}
