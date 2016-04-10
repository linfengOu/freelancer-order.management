/**
 * 
 */
package com.freelancer.om.model;

import java.util.Date;
import java.util.Properties;

import com.freelancer.om.util.ConfigManagement;

/**
 * @author Oliver
 *
 */
public class Order {
	
	private int oid;
	
	private String title;
	
	private float price;
	
	private int aDiv;
	
	private int bDiv;

	private Date createDate;
	
	private Date endDate;
	
	private Date deadline;
	
	private String place;
	
	private float deposit;
	
	private String desc;
	
	/**
	 * A party (promulgator) user name
	 */
	private String aParty;
	
	/**
	 * B party (applicant) user name
	 */
	private String bParty;
	
	private int statusCode;
	
	private String status;
	
	private int isRead;
	
	private Properties props;
	
	public Order() {
		props = ConfigManagement.getInstance().getProps();
	}
	
	public int getOid() {
		return oid;
	}

	public String getTitle() {
		return title;
	}

	public float getPrice() {
		return price;
	}

	public int getaDiv() {
		return aDiv;
	}

	public int getbDiv() {
		return bDiv;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Date getDeadline() {
		return deadline;
	}

	public String getPlace() {
		return place;
	}

	public float getDeposit() {
		return deposit;
	}

	public String getDesc() {
		return desc;
	}

	public String getaParty() {
		return aParty;
	}

	public String getbParty() {
		return bParty;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatus() {
		return status;
	}

	public int getIsRead() {
		return isRead;
	}

	public void setOid(int oid) {
		this.oid = oid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public void setaDiv(int aDiv) {
		this.aDiv = aDiv;
	}

	public void setbDiv(int bDiv) {
		this.bDiv = bDiv;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public void setDeposit(float deposit) {
		this.deposit = deposit;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setaParty(String aParty) {
		this.aParty = aParty;
	}

	public void setbParty(String bParty) {
		this.bParty = bParty;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}

	public void setStatus(int statusCode) {
		this.status = props.getProperty("order.status." + statusCode, "Status Error");
		this.statusCode = statusCode;
	}
}
