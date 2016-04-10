package com.freelancer.om.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.freelancer.om.dao.IOrderDao;
import com.freelancer.om.model.Order;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;

/**
 * @author Oliver
 * Implement interface IOrdarDao
 */
public class OrderDao implements IOrderDao {
	
	/**
	 * Database connection manager
	 */
	private DBConnectionManager dbcm;

	/**
	 * Construct class instance with given Database manager
	 * @param dbcm
	 */
	public OrderDao(DBConnectionManager dbcm) {
		this.dbcm = dbcm;
	}

	@Override
	public Order insertOrder(Order order) throws ExceptionMessage {
		Connection con = dbcm.getConnection("mysql");
		Statement stmt = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		
		try {
			sql.append("insert into `order` (`title`,`price`,`adiv`,`bdiv`,`deadline`,`place`,`deposit`) " +
				"values (?,?,?,?,?,?,?)");
			pstm = con.prepareStatement(sql.toString());
			pstm.setString(1, order.getTitle());
			pstm.setFloat(2, order.getPrice());
			pstm.setInt(3, order.getaDiv());
			pstm.setInt(4, order.getbDiv());
			pstm.setDate(5, new java.sql.Date(order.getDeadline().getTime()));
			pstm.setString(6, order.getPlace());
			pstm.setFloat(7, order.getDeposit());
			pstm.executeUpdate();
			pstm.close();

			sql.setLength(0);
			sql.append("select last_insert_id()");
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if (rs.next()) {
				order.setOid(rs.getInt(1));
			}

			if (order.getDesc() != null) {
				sql.setLength(0);
				sql.append("insert into `orderdesc` (`oid`,`desc`) values (?,?)");
				pstm = con.prepareStatement(sql.toString());
				pstm.addBatch(sql.toString());
				pstm.setInt(1, order.getOid());
				pstm.setString(2, order.getDesc());
				pstm.executeUpdate();
				pstm.close();
			}
			
			sql.setLength(0);
			sql.append("insert into `orderstatus` (`oid`,`aparty`,`bparty`,`status`,`isread`) values (?,?,?,?,?)");
			pstm = con.prepareStatement(sql.toString());
			pstm.setInt(1, order.getOid());
			pstm.setString(2, order.getaParty());
			pstm.setString(3, order.getbParty());
			pstm.setInt(4, order.getStatusCode());
			pstm.setInt(5, order.getIsRead());
			pstm.executeUpdate();
			pstm.close();
						
		} catch (Exception e) {
			throw new ExceptionMessage("创建订单失败, 请重试. 错误信息: " + e.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (pstm != null) {
					pstm.close();	
				}
			} catch (SQLException e) { }
			dbcm.freeConnection("mysql", con);
		}
		
		return order;
	}

	@Override
	public void deleteOrder(int oid) throws ExceptionMessage {
		throw new ExceptionMessage("目前不支持删除操作, 请联系管理员.");
	}

	@Override
	public void update(int oid, Map<String, Object> param) throws ExceptionMessage {
		boolean hasOrder = false, hasDesc = false, hasStatus = false;
		for (String key : param.keySet()) {
			switch (key) {
			case "price": case "deposit": case "place": case "adiv": case "bdiv": case "deadline":
				hasOrder = true;
				break;
			case "desc":
				hasDesc = true;
				break;
			case "status": case "isread":
				hasStatus = true;
				break;
			}
		}
		if (hasOrder) {
			updateOrder(oid, param);
		}
		if (hasDesc) {
			updateOrderDesc(oid, param.get("desc"));
		}
		if (hasStatus) {
			updateOrderStatus(oid, param);
		}
	}

	/**
	 * update table `order`, invoked by function 'update'
	 * @param oid
	 * @param param
	 * @throws ExceptionMessage
	 */
	private void updateOrder(int oid, Map<String, Object> param) throws ExceptionMessage {
		StringBuffer sql = new StringBuffer();
		sql.append("update `order` set ");
		for (String key : param.keySet()) {
			switch (key) {
			case "price": case "deposit": case "place": case "adiv": case "bdiv": case "deadline":
				sql.append("`" + key + "`=?,");
				break;
			}
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" where `oid`=?");
		
		Connection con = dbcm.getConnection("mysql");
		PreparedStatement pstm = null;
		
		try {
			pstm = con.prepareStatement(sql.toString());
			int i = 1;
			for (Map.Entry<String, Object> entry : param.entrySet()) {
				switch (entry.getKey()) {
				case "place":
					pstm.setString(i, (String) entry.getValue());
					i++;
					break;
				case "price": case "deposit":
					pstm.setFloat(i, (float) entry.getValue());
					i++;
					break;
				case "adiv": case "bdiv":
					pstm.setInt(i, (int) entry.getValue());
					i++;
					break;
				case "deadline":
					java.util.Date utilDate = (Date) entry.getValue();
					java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
					pstm.setDate(i, sqlDate);
					i++;
					break;
				}
			}
			pstm.setInt(i, oid);
			pstm.executeUpdate();
		} catch (Exception e) {
			throw new ExceptionMessage("更新订单失败, 请重试. 错误信息: " + e.getMessage());
		} finally {
			try {
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) { }
			dbcm.freeConnection("mysql", con);
		}
	}

	/**
	 * update table `orderdesc`, invoked by function 'update'
	 * @param oid
	 * @param desc
	 * @throws ExceptionMessage
	 */
	private void updateOrderDesc(int oid, Object desc) throws ExceptionMessage {
		String sql = "insert `orderdesc` (`oid`, `desc`) values (?,?) on duplicate key update `desc`=?";

		Connection con = dbcm.getConnection("mysql");
		PreparedStatement pstm = null;
		
		try {
			pstm = con.prepareStatement(sql.toString());
			pstm.setInt(1, oid);
			pstm.setString(2, (String) desc);
			pstm.setString(3, (String) desc);
			pstm.executeUpdate();
		} catch (Exception e) {
			throw new ExceptionMessage("更新订单失败, 请重试. 错误信息: " + e.getMessage());
		} finally {
			try {
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) { }
			dbcm.freeConnection("mysql", con);
		}
	}

	/**
	 * update table `orderstatsu`, invoked by function 'update'
	 * @param oid
	 * @param param
	 * @throws ExceptionMessage
	 */
	private void updateOrderStatus(int oid, Map<String, Object> param) throws ExceptionMessage {
		StringBuffer sql = new StringBuffer();
		sql.append("update `orderstatus` set ");
		for (String key : param.keySet()) {
			if (key == "status" || key == "isread") {
				sql.append("`" + key + "`=?,");
			}
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" where `oid`=?");
		
		Connection con = dbcm.getConnection("mysql");
		PreparedStatement pstm = null;
		
		try {
			pstm = con.prepareStatement(sql.toString());
			int i = 1;
			for (Map.Entry<String, Object> entry : param.entrySet()) {
				if (entry.getKey() == "status" || entry.getKey() == "isread") {
					pstm.setInt(i, (int) entry.getValue());
					i++;
				}
			}
			pstm.setInt(i, oid);
			pstm.executeUpdate();
		} catch (Exception e) {
			throw new ExceptionMessage("更新订单失败, 请重试. 错误信息: " + e.getMessage());
		} finally {
			try {
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) { }
			dbcm.freeConnection("mysql", con);
		}
	}

	@Override
	public Order getOrderDetail(int oid) throws ExceptionMessage {
		Connection con = dbcm.getConnection("mysql");
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "select * "
				+ "from `order` as o join `orderstatus` as os on o.oid = os.oid "
				+ "left join `orderdesc` as od on o.oid = od.oid where "
				+ "o.oid=? limit 1";
		Order od = new Order();
		
		try {
			pstm = con.prepareStatement(sql);
			pstm.setInt(1, oid);
			rs = pstm.executeQuery();
			
			if (rs.next()) {
				od.setOid(rs.getInt(1)); 
				od.setTitle(rs.getString("title"));
				od.setPrice(rs.getFloat("price"));
				od.setaDiv(rs.getInt("aDiv"));
				od.setbDiv(rs.getInt("bDiv"));
				od.setCreateDate(rs.getDate("createDate")); 
				od.setEndDate(rs.getDate("endDate"));
				od.setDeadline(rs.getDate("deadline")); 
				od.setPlace(rs.getString("place"));
				od.setDeposit(rs.getFloat("deposit")); 
				od.setDesc(rs.getString("desc"));
				od.setaParty(rs.getString("aParty"));
				od.setbParty(rs.getString("bParty"));
				od.setStatus(rs.getInt("status"));
				od.setIsRead(rs.getInt("isRead"));
			}
		} catch (SQLException e) {
			throw new ExceptionMessage("查询订单失败, 请重试. 错误信息: " + e.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstm != null) {
					pstm.close();
				}
			} catch (SQLException e) { }
			dbcm.freeConnection("mysql", con);
		}
		return od;
	}

	@Override
	public List<Order> getOrdersWithQueries(List<List<Object>> queries, int offset, int size) {
		// select o.`oid`, `title`, `price`, `aparty`, `bparty`, `createdate`, `status`, `isread`
		// from `order` as o 
		// join `orderstatus` as os on o.oid = os.oid
		// where `col` = ? ...
		// order by `col` asc/desc
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlCondition = new StringBuffer();
		StringBuffer sqlSort = new StringBuffer();
		
		sql.append("select o.`oid`, `title`, `price`, `aparty`, `bparty`, `createdate`, `status`, `isread` "
				+ "from `order` as o join `orderstatus` as os on o.oid = os.oid");
		sqlCondition.append(" where");
		sqlSort.append(" order by");
		
		List<Object> query = null;
		for (int i = 1; i<queries.size(); i++) {
			query = (List<Object>) queries.get(i);
			if ( (String) query.get(0) == "COND") {
				if (query.get(2) == "or") {
					
				} else {
					sqlCondition.append(setCondition((String)query.get(1), (String)query.get(2)) + " and");
				}
			}
		}
		return null;
	}
	
	private String setCondition(String field, String op) {
		String cd = null;
		switch (op) {
		case "eq":
			cd = " `" + field + "` = ?";
			break;
		case "ne":
			cd = " `" + field + "` != ?";
			break;
		case "is":
			cd = " `" + field + "` is ?";
			break;
		case "not":
			cd = " `" + field + "` not ?";
			break;
		case "lt":
			cd = " `" + field + "` < ?";
			break;
		case "gt":
			cd = " `" + field + "` > ?";
			break;
		case "lte":
			cd = " `" + field + "` <= ?";
			break;
		case "gte":
			cd = " `" + field + "` >= ?";
			break;
		case "li":
			cd = " `" + field + "` like ?";
			break;
		case "nl":
			cd = " `" + field + "` not like ?";
			break;
		case "or":
			
			break;
		}
		return cd;
	}

}
