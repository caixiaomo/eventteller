package web.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import news.core.EventDetection;
import news.core.IDF;
import news.crawler.articleCrawlerAndExtractor.ArticleExtractor;
import news.crawler.articleCrawlerAndExtractor.downloadHtml;
import news.index.ArticleIndex;
import news.index.EventIndex;

import org.hibernate.Session;

import util.Const;


import db.HSession;
import db.data.Topic;
import db.data.Word;
import db.data.article;
import db.data.event;
import db.data.titleNews;

public class Model {
	

	private Session session;
	
	public Model(){

		if(null == session){
			session = new HSession().createSession();
		}
	}

	
	public int getIdsFromUrl(String url){
		String hql = "from titleNews as obj where obj.url ='" + url+"'";
		titleNews results = new titleNews();
		results = (titleNews)session.createQuery(hql).uniqueResult();
		if(results == null)
			return 0;
		return results.getId();
	}


	
	public article getArticleByid(int id){
		article at = new article();
		String hql = "from article as obj where obj.id = "+id;
		at = (article)session.createQuery(hql).uniqueResult();		
		return at;
	}
	
	public event getEventByid(int id){
		event et = new event();
		String hql = "from event as obj where obj.id = "+id;
		et = (event)session.createQuery(hql).uniqueResult();		
		return et;
	}
	
	public Topic getTopicByid(int id){
		Topic tp = new Topic();
		String hql = "from Topic as obj where obj.id = "+id;
		tp = (Topic)session.createQuery(hql).uniqueResult();		
		return tp;
	}
	
	@SuppressWarnings("unchecked")
	public List<article> getArticlesByEID(int id){
		List<article> results = new ArrayList<article>();
		String hql = "from article as obj where obj.eventid = " + id;
		results = (List<article>)session.createQuery(hql).list();		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getEventsByEID(int id){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.topicid = " + id;
		results = (List<event>)session.createQuery(hql).list();		
		return results;
	}
	
	public List<article> getReleventArticles(String text){
		List<article> results = new ArrayList<article>();
		try {
			text = new String(text.getBytes("iso-8859-1"),"UTF8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArticleIndex ai = new ArticleIndex();
		try {
			results = ai.getArticlesFromIndex(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	public List<article> getMostReleventArticles(String text){
		List<article> results = new ArrayList<article>();
		try {
			text = new String(text.getBytes("iso-8859-1"),"UTF8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArticleIndex ai = new ArticleIndex();
		try {
			results = ai.getArticlesFromIndex(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	public List<event> getReleventEvents(String text){
		List<event> results = new ArrayList<event>();
		try {
			text = new String(text.getBytes("iso-8859-1"),"UTF8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		EventIndex ei = new EventIndex();
		try {
			results = ei.getEventsFromIndex(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	public List<event> getMostReleventEvents(String text){
		List<event> results = new ArrayList<event>();
		EventIndex ei = new EventIndex();
		try {
			results = ei.getEventsFromIndex(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	public article getEventID(article ls_at){
		List<Integer> ls_ids = new ArrayList<Integer>();
		double max_sim = -1;
		int max_id = -1;
		EventDetection ed = new EventDetection();
		//get all the relevent ids,not contain itself
		ls_ids = ed.getMostReleventIdFromIndex(ls_at);
		if(ls_ids.contains(ls_at.getId())){
			ls_ids.remove(ls_at.getId());
		}
		System.out.println("find "+ls_ids.size()+" ids relevent!");
		//get the max similarity
		if(ls_ids.size() <= 0){
			ls_at.setEventid(0);
			return ls_at;
		}
		for(int id:ls_ids){
			double sim = ed.getSimilaraty(ls_at,id);
			if(sim>max_sim){
				max_sim = sim;
				max_id = id;
			}
		}
		///if can't find any ids from the index 
		if(max_sim>Const.MaxEventSimNum){
			article temp =  ed.getArticleById(max_id);
			ls_at.setEventid(temp.getEventid());
		}else{
			////update event get the new eventid set it to article
			ls_at.setEventid(0);
		}
		return ls_at;
	}
	
	public String getHtmlFromUrl(String url){		
		return downloadHtml.downloadHtmlFromUrl(url);
	}
	
	public article getArticleFromUrl(String url){
		String html = getHtmlFromUrl(url);
		article at = new article();
		ArticleExtractor ae = new ArticleExtractor(html);
		at.setImg(ae.getImg());
		at.setTitle(ae.getTitle());
		at.setMainparagraph(ae.getMainParagraph());
		at.setMainwords(ae.mainParagraphWordsWithFrequency());
		at.setTitlewords(ae.titleWordsWithFrequency());
		at.setSummary(ae.SummaryWordsWithFrequency());
		at.setEventid(0);		
//		at = getSummaryWithScore(at);
//		at = getEventID(at);
		return at;
	}
	
	public List<Word> combineWordList(List<Word> bigs , List<Word> smalls){
		for(Word word : smalls){
			if(word!=null){
				bigs.add(word);
			}
		}
		return bigs;
	}
	
	public Map<String,Double> getScoreMapFromList(List<Word> scrs){
		Map<String,Double> results = new HashMap<String,Double>();
		for(Word word : scrs){
			if(!results.containsKey(word)&&word.getName()!=null){
				results.put(word.getName(), word.getScore());
			}
		}		
		return results;
	}
	
	public article updateArticleScore(article at ,Map<String,Double> scores){
		long num = 0;
		String temp_summary = "";
		String summary = at.getSummary();
		String[] terms = summary.split(",");
		for(String term : terms){
			String temp_word = term.split(" ")[0];
			if(temp_word==null||temp_word.length()<=0||temp_word.length()>20)
				continue;
			double temp_score =Const.AVGIDF; 
			if(scores.get(temp_word) != null){
				temp_score  = scores.get(temp_word);
			}
			String new_term = term + " " + String.valueOf(temp_score);				
			temp_summary +=new_term + ",";				
		}
		if(summary.equalsIgnoreCase("")){
			if(num%20==0){
				System.out.println("summary is null! "+ (num));
			}
			num++;
		}else if(summary.length()<6000){
			at.setSummary(temp_summary);
		}	
		return at;
	}
	
	public article getSummaryWithScore(article at){
		article results = new article();
		Map<String,Integer> in_mp_words = new HashMap<String,Integer>();
		List<Word> word_results = new ArrayList<Word>();
		IDF idf = new IDF();
		//get words map from articles
		
		in_mp_words = idf.getMapFromArticleList(at);
		for(int i = 0;i<Const.IDF_DB_Split_Nums; i++){
			//update the idf tables					
			long nums = 0; 
			Map<String,Word> exsitsMap = new HashMap<String,Word>();					
			Map<String,Integer> in_temp_words = new HashMap<String,Integer>();					
			List<Word> get_words = new ArrayList<Word>();
			List<Word> in_words = new ArrayList<Word>();	
			//get the idf_i table word list
			in_temp_words = idf.getSubListFromTotal(in_mp_words , i);
			get_words = idf.getWordsFromSubIDFDB(i);
			exsitsMap = idf.DBWordsToMap(get_words);
			in_words = idf.combineOldAndNew(exsitsMap,in_temp_words);
			//get total number of articles
			nums = idf.getNumFromArticle();		
			//set score of words in idf_i
			in_words = idf.setWordScore(in_words , nums);
			//combine small idf_i table to total mp
			word_results = combineWordList(word_results,in_words);
		}	
		results = updateArticleScore(at,getScoreMapFromList(word_results));
		return results;
	}

}
