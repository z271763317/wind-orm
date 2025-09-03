package org.wind.orm.time;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimerTask;

import org.wind.orm.service.ConnectionManager;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : Connection闲置处理任务（可自行配置定时任务处理）
 * @作者 : 胡路路
 * @时间 : 2023年11月10日 10:35:42
 */
public class ConnectionFreeTask extends TimerTask{

	private static Set<ConnectionManager> list=new LinkedHashSet<>();
	
	public static void add(ConnectionManager conManager) {
		if(conManager!=null) {
			list.add(conManager);
		}
	}
	
	@Override
	public void run() {
		long currentTime=System.currentTimeMillis();		//当前时间戳
		for(ConnectionManager conManager:list) {
			long opTime=conManager.getLastOperationTime();
			long freeTime=conManager.getConnectionPool().getFreeTime();
			long cha=currentTime-opTime;
			//超过了指定数
			if(cha>=freeTime) {
				TableUtil.closeConnection(conManager.getConnection());		//关闭
			}
		}	
	}
	
}