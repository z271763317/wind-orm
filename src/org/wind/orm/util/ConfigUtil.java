package org.wind.orm.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wind.orm.annotation.Default;
import org.wind.orm.bean.Config;
import org.wind.orm.bean.Config_Read;
import org.wind.orm.bean.Config_Write;
import org.wind.orm.bean.Config_global;

/**
 * @描述 : 配置工具类（可获取配置文件信息）
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 19:12:18
 */
public final class ConfigUtil{
	
	//获取数据源配置文件
	public Map<String,Config> getDataSourceConfig(){
		Map<String,Config> configMap=new HashMap<String, Config>();
		try{
			List<Map<String,Object>> configList=new ArrayList<Map<String,Object>>();
			// step 1: 获得dom解析器工厂（工作的作用是用于创建具体的解析器）   
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
	        // step 2:获得具体的dom解析器   
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        /*流式*/
	        InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream("wind-orm.xml");
	        Document document=db.parse(is);
	        is.close();
	        // step3: 解析一个xml文档，获得Document对象（根结点）
	        NodeList list = document.getElementsByTagName("dataSource");   
	        for(int i = 0; i < list.getLength(); i++) {
	        	Map<String,Object> map=new HashMap<String,Object>();
	            Element node =(Element) list.item(i);
	            String id=node.getAttribute("id");
	            map.put("id", id);
	            NodeList nodeList=node.getChildNodes();
	            for(int j=0;j<nodeList.getLength();j++){
	            	Node t_node=nodeList.item(j);
		            //是元素
		            if(t_node.getNodeType()==Node.ELEMENT_NODE){
		            	String tag= t_node.getNodeName();
		            	boolean isChildElement= false;
		            	NodeList t_nodeList=t_node.getChildNodes();	//子节点
            			for(int k=0;t_nodeList!=null && k<t_nodeList.getLength();k++){
            			    Node t_child_node=t_nodeList.item(k);
            			    //子元素
            			    if(t_child_node.getNodeType()==Node.ELEMENT_NODE){
            			    	isChildElement=true;
            			    	break;
            			    }
            			}
		            	//子元素（该节点若拥有子元素）
	            		if(isChildElement){
	            			map.put(tag, t_node);
	            		}else{
	            			String text=t_node.getTextContent();
	            			map.put(tag, text);
	            		}
		            }
	            }
	            configList.add(map);
	        }
	        for(int i=0;i<configList.size();i++){
	        	Map<String,Object> map=configList.get(i);
	        	Config config=getConfigMap_global(Config.class, map, true);
	        	if(config!=null){
		        	Node t_node_write=(Node) map.get("write");
		        	Node t_node_read=(Node) map.get("read");
		        	if(t_node_write!=null){
		        		Config_Write t_configWrite=this.getConfigSeparation(Config_Write.class,t_node_write, false);
		        		config.setConfigWrite(t_configWrite);
		        	}
		        	if(t_node_read!=null){
		        		Config_Read t_configRead=this.getConfigSeparation(Config_Read.class,t_node_read, false);
		        		config.setConfigRead(t_configRead);
		        	}
		        	configMap.put(config.getId(), config);
	        	}
	        }
		}catch(Exception e){
			throw new RuntimeException("数据源配置错误："+e.getMessage(),e);
		}
		return configMap;
	}
	//获取 : Config_Separation对象，根据给出的nodeSeparation节点（readWriteClass=继承Config_Separation的读或写类；nodeSeparation=读或写节点；isDefaultValue=当节点的内容为空时，是否取默认值）
	private <T extends Config_global> T getConfigSeparation(Class<T> readWriteClass,Node nodeSeparation,boolean isDefaultValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Map<String,Object> map=new HashMap<String,Object>();
		NodeList nodeList=nodeSeparation.getChildNodes();
        for(int j=0;j<nodeList.getLength();j++){
        	Node t_node=nodeList.item(j);
            //当前节点是元素
            if(t_node.getNodeType()==Node.ELEMENT_NODE){
            	String tag= t_node.getNodeName();
        		String text=t_node.getTextContent();
        		map.put(tag, text);
            }
        }
        return (T) this.getConfigMap_global(readWriteClass, map, isDefaultValue);
	}
	//获取 : 通用配置，返回的对象没有任何内容，则返回null（isDefaultValue=当节点的内容为空时，是否取默认值）
	private <T extends Config_global> T getConfigMap_global(Class<T> configClass,Map<String,Object> map,boolean isDefaultValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		/**生成对象**/
		T t_obj=configClass.newInstance();
		Field t_fieldArr[]=Config_global.class.getDeclaredFields();
		Field.setAccessible(t_fieldArr, true);		//取消安全验证
		boolean isHaveValue=false;		//是否有值
		for(int i=0;i<t_fieldArr.length;i++){
			Field t_field=t_fieldArr[i];
			Default t_an_default=t_field.getAnnotation(Default.class);
			String t_name=t_field.getName();
			String t_value_default=null;		//默认值
			if(t_an_default!=null) {
				String t_temp_name=t_an_default.name();
				if(t_temp_name.length()>0) {
					t_name=t_temp_name;
				}
				t_value_default=t_an_default.value();
			}
			String t_value=(String) map.get(t_name);
			if(isDefaultValue && (t_value==null || t_value.trim().length()<=0) ) {
				t_value=t_value_default;
			}
			if(t_value!=null && t_value.trim().length()>0) {
				Object t_value_set=TableUtil.cast(t_value.toString(), t_field.getType());
				t_field.set(t_obj, t_value_set);
				isHaveValue=true;
			//除了dataBase，其他都要验证
			}else if(isDefaultValue && !t_name.equals("dataBase")) {
				throw new IllegalArgumentException("【"+t_name+"】不能为空");
			}
		}
		if(isHaveValue){
			Integer t_init=t_obj.getInit();
			Integer t_max=t_obj.getMax();
			if(t_init!=null && t_init<=0) {
				throw new IllegalArgumentException("【init】不能小于等于0"); 
			}
			if(t_max!=null && t_max<=0){
				throw new IllegalArgumentException("【max】不能小于等于0"); 
			}
			if(t_init!=null && t_max!=null && t_init>t_max){
				throw new IllegalArgumentException("【init】不能大于【max】"); 
			}
			return t_obj;
		}else{
			return null;
		}
	}
	
}