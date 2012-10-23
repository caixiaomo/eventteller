package news.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import news.index.ArticleIndex;

import org.apache.lucene.search.IndexSearcher;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;
import util.Log;

import db.HSession;
import db.data.article;
import db.data.event;

/**
* @PackageName:news.core
* @ClassName: EventDetection
* @author: mblank
* @date: 2012-3-30 上午11:33:56
* @Description: detect the article's event
* @Marks: will modify event_day , aritlce db
*/
public class EventDetection {
	
	private Session  session;
	
	public EventDetection(){
		if(session==null){
			session = new HSession().createSession();
		}
	}
	
	/**
	 * @return
	 * @Description:get articles which taskstatus = 4 and eventid = 0 from db
	 * @Mark: batch size is Const.MysqlToIndexMaxItemNum;
	 * @Mark: because we have to get relevent ids from index, so we update batch items and then put them into index
	 */
	@SuppressWarnings("unchecked")
	public List<article> getArticleFromDB(){
		List<article> results = new ArrayList<article>();
		String hql = "from article as obj where obj.taskstatus="+Const.TASKID.get("HtmlFromHbaseToMysql")+" and obj.eventid=0";
		Query query = session.createQuery(hql);
		query.setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<article>)query.list();
		Log.getLogger().info("Find article (not have eventid) in db: "+results.size());		
		return results;
	}
	
	/** just for test
	 * @param at
	 * @return
	 * @Description:get relevent ids of article from index
	 * @Mark: using article's title words to search the index
	 * @Mark: most ids' number is limit to 500（in Const）
	 */
	public List<Integer> getReleventIdFromIndex(article at){
		List<Integer> results = new ArrayList<Integer>();
		String[] titleWords = at.getTitlewords().split(",");
		String str_temp = "";
		for(String titleword : titleWords){
			String[] temps = titleword.split(" ");
			String word = temps[0];
			if(word.length()<=1){
				continue;
			}
			str_temp +=" "+word;
		}
		ArticleIndex ai = new ArticleIndex();
		try {
			IndexSearcher is = new ArticleIndex().openIndexSearcher();
			results = ai.getIdFromIndex(is, str_temp,at.getCrawltime());
			is.getIndexReader().close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return results;
	}
	
	public List<Integer> getMostReleventIdFromIndex(article at){
		List<Integer> results = new ArrayList<Integer>();
		String[] titleWords = at.getTitlewords().split(",");
		String str_temp = "";
		for(String titleword : titleWords){
			String[] temps = titleword.split(" ");
			String word = temps[0];
			if(word.length()<=1){
				continue;
			}
			str_temp +=" "+word;
		}
		ArticleIndex ai = new ArticleIndex();
		try {
			IndexSearcher is = new ArticleIndex().openIndexSearcher();
			results = ai.getIdFromIndex(is, str_temp);
			is.getIndexReader().close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return results;
	}
	
	/**
	 * @param id
	 * @return
	 * @Description:from id to get article
	 */
	@SuppressWarnings("unchecked")
	public article getArticleById(int id){
		List<article> results = new ArrayList<article>();
		String hql = "from article as obj where obj.id="+String.valueOf(id);
		Query query = session.createQuery(hql).setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<article>)query.list();
		article result = new article();
		if(results.size()>0){
			result = results.get(0);
		}
		return result;
	}
	
	/**
	 * @param at
	 * @return
	 * @Description:get all the summary words from article
	 */
	public List<String> getWordsFromArticle(article at){
		List<String> results = new ArrayList<String>();
		String[] terms = at.getSummary().split(",");
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
	
	/**
	 * @param at_a
	 * @param at_b
	 * @return
	 * @Description:get intersection of article_a and article_b (summary words)
	 */
	public List<String> getIntersectionOfArticle(article at_a,article at_b){
		List<String> words_a = new ArrayList<String>();
		List<String> words_b = new ArrayList<String>();
		List<String> results = new ArrayList<String>();
		Map<String,Boolean> temp = new HashMap<String,Boolean>();
		words_a = getWordsFromArticle(at_a);
		words_b = getWordsFromArticle(at_b);
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
	
	public Map<String,Double> getRawWordAndScoreFromArticle(article at){
		Map<String,Double> results = new HashMap<String,Double>();
		if(at.getSummary() == null)
			return null;
		String[] terms = at.getSummary().split(",");
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
	
	public Map<String,Double> getWordAndScoreFromArticle(Map<String,Double> scrs){
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
	
	/**
	 * @param at_a
	 * @param id
	 * @return
	 * @Description:get similaraty of article and id(article)
	 */
	public double getSimilaraty(article at_a,int id){
		double results = 0;
		article at_b = getArticleById(id);
		Map<String,Double> sc_a = new HashMap<String,Double>();
		Map<String,Double> sc_b = new HashMap<String,Double>();
		List<String> interWords = new ArrayList<String>();
		sc_a = getRawWordAndScoreFromArticle(at_a);
		sc_b = getRawWordAndScoreFromArticle(at_b);
		if(sc_b == null){
			return results;
		}
		sc_a = getWordAndScoreFromArticle(sc_a);
		sc_b = getWordAndScoreFromArticle(sc_b);
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
	
	public void updateNewEvent(article at){
		event ev = new event();
		Transaction tx = session.beginTransaction();
		ev.setTime(new Date());
		Calendar cl = Calendar.getInstance();
		cl.setTime(at.getCrawltime());
		int day = 0;
		day = cl.get(Calendar.DAY_OF_YEAR);
		ev.setDay(day);
		ev.setTaskstatus(Const.NotEventToTopic);
		ev.setTopicid(Const.NotEventToTopic);
		session.saveOrUpdate(ev);
		tx.commit();
		session.flush();	
	}
	
	
	/**
	 * @return
	 * @Description:get the max id from * db
	 */
	@SuppressWarnings("unchecked")
	public int getMaxEventId(){
		String hql = "select max(id) from event";
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
	
	public void updateArticle(List<article> ats){
		Transaction tx = session.beginTransaction();
		for(article at : ats){
			session.saveOrUpdate(at);
		}
		tx.commit();
		session.flush();
	}
	
	
	
	/**
	 * @return
	 * @Description:main method
	 */
	public int getMostSimilarity(){
		
		//get article(have no event) from article db
		List<article> ls_ats =  getArticleFromDB();
		List<article> ls_results = new ArrayList<article>();
		for(article ls_at : ls_ats){
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
			System.out.println(ls_at.getId()+"--"+ls_at.getTitle()+"---("+max_sim+")---("+max_id+")"+"--"+getArticleById(max_id).getTitle());
			if(max_sim>Const.MaxEventSimNum){
				article temp =  getArticleById(max_id);
				ls_at.setEventid(temp.getEventid());
				ls_at.setTaskstatus(Const.TASKID.get("ArticleToEvent"));
			}else{
				////update event get the new eventid set it to article
				updateNewEvent(ls_at);
				ls_at.setEventid(getMaxEventId());
				ls_at.setTaskstatus(Const.TASKID.get("UseMysqlArticleToIndex"));
			}
			ls_results.add(ls_at);
		}
		//update the index
		System.out.println("update index!...");
		ArticleIndex ai = new ArticleIndex();
		ai.createOrUpdateIndex(ls_results);
		System.out.println("end update!");
		return ls_results.size();
	}
	
	public void runTask(){
		Const.loadTaskid();
		EventDetection ed  = new EventDetection();
		while(true){
			int num = 9;
			while(num>0){
				num = ed.getMostSimilarity();
			}			
			try {
				System.out.println("now end of ArticleToEvent,sleep for:"+Const.ArticleToEventSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end ArticleToEvent,sleep for:"+Const.ArticleToEventSleepTime/1000/60+" minutes");
				Thread.sleep(Const.ArticleToEventSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in ArticleToEvent!");
				e.printStackTrace();
			}			
		}

	}
	
	
	public static void main(String[] args){
		EventDetection ed = new EventDetection();		
		ed.runTask();		
	}

}
