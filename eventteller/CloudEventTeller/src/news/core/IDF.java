package news.core;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Interceptor;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;


import db.HSession;
import db.IDFInterceptor;
import db.data.Word;
import db.data.article;


/**
* @PackageName:news.core
* @ClassName: IDF
* @author: mblank
* @date: 2012-3-30 下午12:12:52
* @Description: update the idf tables 
* @Marks: will be used in HbaseToMysql class
*/
public class IDF {
	
	public Session session;
	public Interceptor ITC ;
	public List<Word> ExsitsWords;
	public Map<String,Word> ExsitsMap;
	
	public IDF(){
		session = new HSession().createSession();
	}
	
	/**
	 * @param scrs
	 * @return
	 * @Description:convert list to string map
	 */
	public Map<String,Word> DBWordsToMap(List<Word> scrs){
		Map<String,Word> results = new HashMap<String,Word>();
		for(Word word : scrs){
			if(!results.containsKey(word.getName())){
				results.put(word.getName(), word);
			}
		}
		return results;
	}
	
	/**
	 * @param olds
	 * @param news
	 * @return
	 * @Description:combine old words and new words
	 */
	public List<Word> combineOldAndNew(Map<String,Word> olds , Map<String,Integer> news){
		List<Word> results = new ArrayList<Word>();
		Iterator<String> it_news = news.keySet().iterator();
		while(it_news.hasNext()){
			String temp = it_news.next();
			if(olds.containsKey(temp)){
				Word word = olds.get(temp);
				int  tf  = word.getTf();
				tf = tf + news.get(temp);
				word.setTf(tf);		
				results.add(word);
			}else{
				Word word = new Word();
				word.setName(temp);
				word.setTf(news.get(temp));
				results.add(word);
			}
		}	
		return results;
	}

	/**
	 * @param summary
	 * @return
	 * @Description:get summary's words
	 */
	public List<String> getWordsFromSummary(String summary){
		List<String> results = new ArrayList<String>(); 
		Map<String,Integer> mp_results = new HashMap<String,Integer>();
		String[] words = summary.split(",");
		for(String word:words){
			String[] temp = word.split(" ");
			if(temp[0]!=null){
				if(!mp_results.containsKey(temp[0])){
					mp_results.put(temp[0], 1);
					results.add(temp[0]);
				}
			}
		}		
		return results;
	}
	
	/**
	 * @param ats
	 * @return
	 * @Description:get all the word map from article list
	 */
	public Map<String,Integer> getMapFromArticleList(List<article> ats){
		Map<String,Integer> results = new HashMap<String,Integer>();
		for(article at:ats){
			String summary = at.getSummary();
			List<String> words = getWordsFromSummary(summary);
			for(String word:words){
				if(word.length()>20)
					continue;
				if(results.containsKey(word)){
					int value = results.get(word);
					results.put(word, value+1);
				}else{
					results.put(word, 1);
				}
			}
		}		
		return results;
	}
	
	
	/**
	 * @param ats
	 * @return
	 * @Description:get all the word map from article list
	 */
	public Map<String,Integer> getMapFromArticleList(article at){
		Map<String,Integer> results = new HashMap<String,Integer>();
		String summary = at.getSummary();
		List<String> words = getWordsFromSummary(summary);
		for(String word:words){
			if(word.length()>20)
				continue;
			if(results.containsKey(word)){
				int value = results.get(word);
				results.put(word, value+1);
			}else{
				results.put(word, 1);
			}
		}	
		return results;
	}
	
	
	
	
	
	/**
	 * @param scrs
	 * @param num
	 * @return
	 * @Description:set the word's score 
	 */
	public List<Word> setWordScore(List<Word> scrs , double num){

		List<Word> results = new ArrayList<Word>();		
		for(Word wd : scrs){
			int tf = wd.getTf();
			double score = Math.log(((num)/((double)tf)));
			wd.setScore(score);
			results.add(wd);
		}
		return results;
	}
	
	
	/**
	 * @return
	 * @Description:get the article's num if number = 0,return 20W
	 */
	public long getNumFromArticle(){

		String hql ="select count(*) from article as obj ";
		Session tsession = new HSession().createSession();
		Query query = tsession.createQuery(hql);
		long results = 0;
		results = ((Number)query.uniqueResult()).longValue();		
		tsession.close();
		if(results == 0){
			results = 200000;
		}
		return results;
	}
	
	
	/**
	 * @param scrs
	 * @param num
	 * @return mp(string,int) ---> ("for test" , 5),5 stand for which idf db it belongs to
	 * @Description:split the total mp to submp
	 */
	public Map<String,Integer> getSubListFromTotal(Map<String,Integer> scrs , int num){
		Iterator<String> it_str = scrs.keySet().iterator();
		Map<String,Integer> results = new HashMap<String,Integer>();
		while(it_str.hasNext()){
			String word = it_str.next();
			int hash_num = Math.abs( word.hashCode()%Const.IDF_DB_Split_Nums );
			if(hash_num == num){
				results.put(word,scrs.get(word));
			}
		}
		return results;
	}
	
	/**
	 * @param num
	 * @return
	 * @Description:get words from idf_i db
	 */
	@SuppressWarnings("unchecked")
	public List<Word> getWordsFromSubIDFDB(int num){
		List<Word> results = new ArrayList<Word>();
		IDFInterceptor itc = new IDFInterceptor(num);
		Session tsession = new HSession().createSession(itc);
		String hql = "from Word as obj";
		Query query = tsession.createQuery(hql);
		results = (List<Word>)query.list();
		tsession.close();
		return results;
	}

	/**
	 * @param scrs
	 * @param num
	 * @Description:update the idf_i db
	 */
	public void updateSubIDFDB(List<Word> scrs , int num){
		IDFInterceptor itc = new IDFInterceptor(num);
		Session tsession = new HSession().createSession(itc);
		Transaction tx = tsession.beginTransaction();
		for(Word word : scrs){
			tsession.saveOrUpdate(word);
		}
		tx.commit();
		tsession.flush();
		tsession.close();
	}
	
}
