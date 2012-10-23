package web.chart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import org.hibernate.Query;
import org.hibernate.Session;

import db.HSession;
import db.data.event;

public class ShowTopNEventFromWebSite {
	
	public Session session = null;
	public int topN = 200;
	public String path = "/usr/local/apache2/htdocs/EventTeller/SITE/Tpl/default/Public/";
	public String file_topn = "topN";
	public String file_rare = "rare_web";
	public String file_add = ".html";
//	public String path = "/home/mblank/";
	
	public ShowTopNEventFromWebSite(){
		if(null == session){
			session = new HSession().createSession();
		}
	}
	
	public int getMaxDayFromEventDB(){
		int results  = 0 ;
		String hql = "select max(obj.day) from event as obj";
		results = (Integer) session.createQuery(hql).uniqueResult();		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getMaxDayEventFromDB(int days){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj  where obj.day ="+String.valueOf(days)+" order by obj.number desc";
		Query query = session.createQuery(hql);
		query.setMaxResults(topN);
		results = (List<event>)query.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getRareEventFromDB(int days){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj  where obj.day ="+String.valueOf(days)+" and obj.number = 1";
		Query query = session.createQuery(hql);
		results = (List<event>)query.list();
		return results;
	}
	
	public List<Integer> getArticleIdFromEvents(List<event> scrs){
		List<Integer> results = new ArrayList<Integer>();
		for(event et : scrs){
			String ats = et.getArticles();
			String[] temps = ats.split(" ");
			if(temps.length == 0){
				continue;
			}
			for(String temp : temps){
				temp.trim();
				results.add(Integer.valueOf(temp));
			}		
		}		
		return results;
	}
	
	
	public Map<String,Integer> getWebSiteFromId(List<Integer> scrs){
		Map<String,Integer> results = new HashMap<String,Integer>();
		for(Integer it : scrs){
			String hql = "select webSite from titleNews where id = "+String.valueOf(it);
			String website = (String)session.createQuery(hql).uniqueResult();
			if(results.containsKey(website)){
				Integer num = results.get(website);
				num++;
				results.put(website, num);
			}else{
				results.put(website, new Integer(1));
			}
		}	
		return results;
	}
	
	public List<String> soreMapValue(Map<String,Integer> scrs){
		List<String> results_n = new ArrayList<String>();
		List<String> results = new ArrayList<String>();
		Map<Double,String> temp_res = new TreeMap<Double,String>();
		Iterator<String> it_res = scrs.keySet().iterator();
		while(it_res.hasNext()){
			String temp = it_res.next();
			int num = scrs.get(temp);
			temp_res.put(num+temp.hashCode()%10*0.1, temp);
		}		
		Iterator<Double> it_temp = temp_res.keySet().iterator();
		int num = 0;
		while(it_temp.hasNext()){
			Double tempd = it_temp.next();
			String temp_str = temp_res.get(tempd);
			results.add(temp_str);
		}
		for(int i =results.size()-1 ;i>=0 ;i--){
			num++;
			if(num > 5){
				break;
			}
			results_n.add(results.get(i));
		}
		return results_n;
	}
	
	
	public void writeNHtmlFromMap(Map<String,Integer> scrs, String filepath){
		try {
			List<String> ls_scrs = soreMapValue(scrs);
			FileWriter fw = new FileWriter(new File(path+filepath+file_add),false);
			Iterator<String> it_res = scrs.keySet().iterator();
			fw.write("<table id=\""+filepath+"\">");
			fw.write("\n");
			for(String temp : ls_scrs){
				int num = scrs.get(temp);
				scrs.put(temp, 0);
				fw.write("<tr>"+"\n");
				String writes = "<td>"+temp+"</td><td>"+num+"</td>"+"\n";
				fw.write(writes);
				fw.write("</tr>"+"\n");				
			}
			int total = 0;
			while(it_res.hasNext()){
				String temp = it_res.next();
				 total += scrs.get(temp);				
			}
			fw.write("<tr>"+"\n");
			String writes = "<td>"+"other"+"</td><td>"+total+"</td>"+"\n";
			fw.write(writes);
			fw.write("</tr>"+"\n");			
			fw.write("</table>");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void runTask(){
		
		while(true){
		
			int today = 0;
			List<event> event_ls_topn = new ArrayList<event>();
			List<event> event_ls_rare = new ArrayList<event>();
			List<Integer> ids_topn = new ArrayList<Integer>();
			List<Integer> ids_rare = new ArrayList<Integer>();
			Map<String,Integer> results_topn = new HashMap<String,Integer>();
			Map<String,Integer> results_rare = new HashMap<String,Integer>();
			today = getMaxDayFromEventDB();
			if(today > 0){
				event_ls_topn = getMaxDayEventFromDB(today);
				event_ls_rare = getRareEventFromDB(today);
				if(event_ls_topn == null || event_ls_topn.size() <= 0 ){
					try {
						Thread.sleep(30*60*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("no data sleep");
					continue;
				}
				if(event_ls_rare == null || event_ls_rare.size() <= 0)
					continue;
				ids_topn = getArticleIdFromEvents(event_ls_topn);
				ids_rare = getArticleIdFromEvents(event_ls_rare);
				results_topn = getWebSiteFromId(ids_topn);	
				results_rare = getWebSiteFromId(ids_rare);
			}
			if(results_topn.size() > 0){
				writeNHtmlFromMap(results_topn,file_topn);
				writeNHtmlFromMap(results_rare,file_rare);
			}	
			try {
				System.out.println("write ok.sleep for 30 minitues! "+new Date().toString());
				Thread.sleep(30*60*1000);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		ShowTopNEventFromWebSite stnefw = new ShowTopNEventFromWebSite();
		stnefw.runTask();
	}


	
	
}
