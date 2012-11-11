package news.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import news.crwaler.articleCrawlerAndExtractor.ChineseSplit;
import news.index.EventIndex;

import org.apache.lucene.search.IndexSearcher;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;
import util.Log;

import db.HSession;
import db.data.Topic;
import db.data.event;

public class TopicTrace {
	
	public Session session;
	
	public TopicTrace(){
		if(null == session){
			session  = new HSession().createSession();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getEventFromDB(){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.taskstatus = "+Const.NotEventToTopic + " and obj.title != null and obj.number != null";
		Query query = session.createQuery(hql);
		query.setMaxResults(Const.MaxEventToTopicNum);
		results = (List<event>)query.list();
		return results;
	}
	
	public List<Integer> getReleventIdFromIndex(event en){
		List<Integer> results = new ArrayList<Integer>();
		String scr_title = "";
		scr_title = en.getTitle();
		List<String> titleWords = ChineseSplit.SplitStr(scr_title);
		String str_temp = "";
		for(String titleword : titleWords){
			if(titleword.length()<=1){
				continue;
			}
			str_temp +=" "+titleword;
		}
		EventIndex ei = new EventIndex();
		try {
			IndexSearcher is = new EventIndex().openIndexSearcher();
			results = ei.getIdFromIndex(is, str_temp,en.getDay());
			is.getIndexReader().close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public event getEventById(int id){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.id="+String.valueOf(id);
		Query query = session.createQuery(hql).setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<event>)query.list();
		event result = new event();
		if(results.size()>0){
			result = results.get(0);
		}
		return result;
	}
	
	public List<String> getWordsFromEvent(event en){
		List<String> results = new ArrayList<String>();
		if(en.getSummarywords() == null || en.getSummarywords().length() == 0)
			return results;
		String[] terms = en.getSummarywords().split(",");
		Map<String,Integer> w_tfs = new HashMap<String,Integer>();
		for(String term:terms){
			String[] temp = term.split(" ");
			if(temp.length==3){
				if(!w_tfs.containsKey(temp[0])){
					w_tfs.put(temp[0],Integer.valueOf(temp[1]));
					results.add(temp[0]);
				}
			}
		}
		return results;
	}
	
	public List<String> getIntersectionOfArticle(event at_a,event at_b){
		List<String> words_a = new ArrayList<String>();
		List<String> words_b = new ArrayList<String>();
		List<String> results = new ArrayList<String>();
		Map<String,Boolean> temp = new HashMap<String,Boolean>();
		words_a = getWordsFromEvent(at_a);
		words_b = getWordsFromEvent(at_b);
		for(String word_a:words_a){
			if(!temp.containsKey(word_a)){
				temp.put(word_a, false);
			}
		}
		for(String word_b:words_b){
			if(temp.containsKey(word_b)){
				temp.put(word_b, true);
			}
		}
		Iterator<String> it_results = temp.keySet().iterator();
		while(it_results.hasNext()){
			String word_temp = it_results.next();
			if(temp.get(word_temp)&&word_temp.length()>1){
				results.add(word_temp);
			}
		}
		return results;
	}
	
	public Map<String,Double> getRawWordAndScoreFromArticle(event en){
		Map<String,Double> results = new HashMap<String,Double>();
		if(en.getSummarywords()== null || en.getSummarywords().length() == 0){
			return results;
		}
		String[] terms = en.getSummarywords().split(",");
		Map<String,Integer> w_tfs = new HashMap<String,Integer>();
		for(String term:terms){
			String[] temp = term.split(" ");
			if(temp.length==3){
				if(!w_tfs.containsKey(temp[0])){
					w_tfs.put(temp[0],Integer.valueOf(temp[1]));
					results.put(temp[0], Double.valueOf(temp[2]));
				}
			}
		}
		return results;
	}
	
	public double getTotalScoreInArticle(Map<String,Double> scrs){
		double results = 0;		
		Iterator<String> it_scrs = scrs.keySet().iterator();
		while(it_scrs.hasNext()){
			String temp_str = it_scrs.next();
			double temp_score = scrs.get(temp_str);
			results +=temp_score*temp_score;
		}
		results = Math.sqrt(results);
		return results;
	}
	
	public Map<String,Double> getWordAndScoreFromEvent(Map<String,Double> scrs){
		Map<String,Double> results = new HashMap<String,Double>();
		double total = 0;
		total = getTotalScoreInArticle(scrs);
		Iterator<String> it_scrs = scrs.keySet().iterator();
		while(it_scrs.hasNext()){
			String temp_str = it_scrs.next();
			double temp_score = scrs.get(temp_str);
			temp_score = temp_score / total;
			results.put(temp_str, temp_score);
		}
		return results;
	}
	
	public double getSimilaraty(event at_a,int id){
		double results = 0;
		event at_b = getEventById(id);
		Map<String,Double> sc_a = new HashMap<String,Double>();
		Map<String,Double> sc_b = new HashMap<String,Double>();
		List<String> interWords = new ArrayList<String>();
		sc_a = getRawWordAndScoreFromArticle(at_a);
		sc_b = getRawWordAndScoreFromArticle(at_b);
		sc_a = getWordAndScoreFromEvent(sc_a);
		sc_b = getWordAndScoreFromEvent(sc_b);
		interWords = getIntersectionOfArticle(at_a,at_b);
		if(interWords!=null){
			for(String interword : interWords){
				double db_a = sc_a.get(interword);
				double db_b = sc_b.get(interword);
				results += db_a*db_b;
			}
		}
		return results;
	}
	
	public void updateNewTopic(){
		Topic tp = new Topic();
		Transaction tx = session.beginTransaction();
		tp.setTime(new Date());
		tp.setUpdatestate(1);/// 1 stand for will be update in the future
		session.saveOrUpdate(tp);
		tx.commit();
		session.flush();	
	}
	
	public void updateEventDB(List<event> scrs){
		Transaction tx = session.beginTransaction();
		for(event en : scrs){
			session.merge(en);
		}
		tx.commit();
		session.flush();
	}
	
	@SuppressWarnings("unchecked")
	public int getMaxTopicId(){
		String hql = "select max(id) from Topic";
		int results = 0;
		if(session!=null){
			Query query = session.createQuery(hql);
			List<Integer> ls_results = query.list();
			if(ls_results.size()>0){
				results = ls_results.get(0);
			}
		}
		return results;
	}
	
	public void updateTopicDB(List<Topic> scrs){ 
		Transaction tx = session.beginTransaction();
		for(Topic topic : scrs){
			if(topic == null)
				continue;
			topic.setUpdatestate(1);//1 stand for will be update in the future
			session.saveOrUpdate(topic);
		}
		tx.commit();
		session.flush();	
	}
	
	
	public int getMostSimilarity(){
		
//		List<Integer> up_res = new ArrayList<Integer>();
		//get article(have no event) from article db
		List<event> ls_ats =  getEventFromDB();
		List<event> ls_results = new ArrayList<event>();
		List<Topic> ls_tps = new ArrayList<Topic>();
		for(event ls_at : ls_ats){
			System.out.println("handle "+ls_at.getId()+" ----");
			List<Integer> ls_ids = new ArrayList<Integer>();
			double max_sim = -1;
			int max_id = -1;
			//get all the relevent ids,not contain itself
			ls_ids = getReleventIdFromIndex(ls_at);
			if(ls_ids.contains(ls_at.getId())){
				ls_ids.remove(ls_at.getId());
			}
			System.out.println("find "+ls_ids.size()+" ids relevent!");
			//get the max similarity
			for(int id:ls_ids){
				double sim = getSimilaraty(ls_at,id);
				if(sim>max_sim){
					max_sim = sim;
					max_id = id;
				}
			}
			///if can't find any ids from the index 
			System.out.println(ls_at.getId()+"--"+ls_at.getTitle()+"---("+max_sim+")---("+max_id+")"+"--"+getEventById(max_id).getTitle());
			if(max_sim>Const.MaxTopicSimNum){
				event temp =  getEventById(max_id);
				ls_at.setTopicid(temp.getTopicid());
				ls_at.setTaskstatus(Const.EventToTopic);
				String hql = "from Topic as obj where obj.id = " + String.valueOf(temp.getTopicid());
				Topic topic_up = (Topic)session.createQuery(hql).uniqueResult();
				topic_up.setEvents(topic_up.getEvents() + " " + ls_at.getId());
				topic_up.setUpdatestate(1);
				ls_tps.add(topic_up);
			}else{
				////update topic get the new topicsid set it to article
				updateNewTopic();
				int id_tmp = getMaxTopicId();
				ls_at.setTopicid(id_tmp);
				ls_at.setTaskstatus(Const.EventToTopic);
				String hql = "from Topic as obj where obj.id = " + String.valueOf(id_tmp);
				Topic topic_up = (Topic)session.createQuery(hql).uniqueResult();
				topic_up.setEvents(topic_up.getEvents() + " " + ls_at.getId());
				topic_up.setUpdatestate(1);
				ls_tps.add(topic_up);
			}
			ls_results.add(ls_at);
		}
		updateTopicDB(ls_tps);
		updateEventDB(ls_results);
		//update the index
		while(true){
			System.out.println("update index!...");
			EventIndex ai = new EventIndex();
			int num = ai.createOrUpdateIndex();
			if(num == 0){
				break;
			} 
		}
		System.out.println("end update!");
		return ls_results.size();
	}

	public void runTask(){
		Const.loadTaskid();
		while(true){
			int num = 9;
			while(num>0){
				num = getMostSimilarity();
			}			
			try {
				System.out.println("now end of TopicTrack,sleep for:"+Const.TopicTrackSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end TopicTrack,sleep for:"+Const.TopicTrackSleepTime/1000/60+" minutes");
				Thread.sleep(Const.TopicTrackSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in TopicTrack!");
				e.printStackTrace();
			}			
		}

	}
	
	
	public static void main(String[] args){
		TopicTrace tp = new TopicTrace();		
		tp.runTask();		
	}
	
	
}
