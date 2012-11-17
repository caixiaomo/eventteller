package web.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import news.crawler.articleCrawlerAndExtractor.ChineseSplit;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import db.HSession;
import db.data.event;

public class LableCloudWord {
	
	public Session session;
	public event et;
	
	public LableCloudWord(){
		if(null == session){
			session = new HSession().createSession();
		}
		if(null == et){
			et = new event();
			String hql = "from event as obj where obj.id = 584373" ;
			et = (event)session.createQuery(hql).uniqueResult();	
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
		String hql = "from event as obj where obj.day ="+String.valueOf(days) +" order by obj.number desc";
		Query query = session.createQuery(hql);
		query.setMaxResults(500);
		results = (List<event>)query.list();
		return results;
	}

	public String getTopNWordsFromEvents(){
		String results = "";
		int maxday = getMaxDayFromEventDB();
		List<event> scrs = getMaxDayEventFromDB(maxday);
		String titles = "";		
		int numb = 0;
		System.out.println(scrs.size());		
		for(event et : scrs){
			String title = et.getTitle();
			if(null != title){
				titles =titles + title + " ";
			}
			numb++;
			System.out.println(numb);
		}
		List<String> lists = ChineseSplit.findMaxOfenWordOnlyNN(titles, 70);		
		Iterator<String> it_strs = lists.iterator();
		while(it_strs.hasNext()){
			String word = it_strs.next();
			if(word.length() > 1 && !word.equalsIgnoreCase("南方网")){
				results = results + word + " ";
			}			
		}
		return results;
	}
	
	public void updateDB(event et){
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(et);
		tx.commit();
		session.flush();
	}
	
	public void runTask(){
		
		while(true){
			String words = "";
			words = getTopNWordsFromEvents();
			System.out.println(words);
			et.setTitle(words);
			updateDB(et);			
			try {
				System.out.println("updated!.sleep for 10 minitues! "+new Date().toString());
				Thread.sleep(30*60*1000);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args){
		LableCloudWord lcw = new LableCloudWord();
		lcw.runTask();
	}
}
