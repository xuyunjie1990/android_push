package com.youguu.android.util;

import java.util.Date;

/**
* @ClassName: RankTokenUser 
* @Description: 排序bean
* @author lqipr 
* @date 2015年4月7日 下午3:38:23
 */
public class RankTokenUser implements Comparable<RankTokenUser> {
	private String token;
	private String app;
	private Date lastDate;
	private int id;
	
	public RankTokenUser() {
	}

	public RankTokenUser(String token, String app, Date lastDate, int id) {
		this.token = token;
		this.app = app;
		this.lastDate = lastDate;
		this.id = id;
	}



	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getApp() {
		return this.app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Date getLastDate() {
		return this.lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	@Override
	public int compareTo(RankTokenUser t) {
		return compare_date(this,t);
	}
	

	/**
	 * 先根据最后时间比较，再根据ID比较
	* @Title: compare_date
	* @Description: 
	* @param t1
	* @param t2
	* @return    
	* int    返回类型
	 */
	private int compare_date(RankTokenUser t1, RankTokenUser t2)
	{
		if(!t1.getLastDate().equals(t2.getLastDate()))
		{
			if(t1.getLastDate().before(t2.getLastDate()))
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			if(t1.getId()>t2.getId())
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		
	}
}