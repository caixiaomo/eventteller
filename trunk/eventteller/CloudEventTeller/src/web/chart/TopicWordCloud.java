package web.chart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import news.crwaler.articleCrawlerAndExtractor.ChineseSplit;

import org.hibernate.Session;

import db.HSession;
import db.data.event;

public class TopicWordCloud {

	public Session session;
	
	public TopicWordCloud(){
		if(null == session){
			session = new HSession().createSession();
		}
	}
	
	public event getEventById(int id){
		event et = new event();
		String hql = "from event as obj where obj.id = " + id;
		et = (event)session.createQuery(hql).uniqueResult();	
		return et;
	}
	
	public Map<String,Integer> getWordsFromEvents(String in_ids){
		Map<String,Integer> results = new HashMap<String,Integer>();
		String[] ids = in_ids.split(" ");
		for(String id : ids){
			int int_id = Integer.valueOf(id);
			event et = new event();
			et = getEventById(int_id);
			String title = et.getTitle();
			if(title != null && title.length() > 0){
				List<String> words = ChineseSplit.SplitStr(title);
				for(String word : words){
					if(word.length() == 1)
						continue;
					if(results.containsKey(word)){
						results.put(word, results.get(word)+1);
					}else{
						results.put(word, 1);
					}
				}
			}
		}		
		return results;
	}
	
	public String getStrFromMap(Map<String,Integer> scrs){
		String results = "";
		Iterator<String> it_strs = scrs.keySet().iterator();
		while(it_strs.hasNext()){
			String temp = it_strs.next();
			int num = scrs.get(temp);
			while((num--) >= 0){
				results =results + temp + " ";
			}
		}
		return results;
	}
	
	public void writeToFile(String context) throws IOException{
		String path = "/home/mblank/Desktop/";
		String name = "ncp";
		FileWriter fw = new FileWriter(new File(path+name));
		fw.write(context);
		fw.flush();
		fw.close();
	}
	
	public void runTask() throws IOException{
		String words = "";
		String ids = "238401 241002 252089 252195 253207 253308 254068 254199 256748 262396 262443 265584 266992 273099 278191 278209 281509 282148 282206 282768 291062 292387 293059 294775 296269 296470 299074 301172 301436 304260 306723 310130 313919 321538 326713 327243 329525 338427 350726 353164 353718 370265 370580 379278 405103 411138 426114 455439";
		Map<String,Integer> lists = new HashMap<String,Integer>();
		lists = getWordsFromEvents(ids);
		words = getStrFromMap(lists);
		System.out.println(words);
		writeToFile(words);
	}
	
	public static void main(String[] args) throws IOException{
		TopicWordCloud twc = new TopicWordCloud();
		twc.runTask();
	}
}
