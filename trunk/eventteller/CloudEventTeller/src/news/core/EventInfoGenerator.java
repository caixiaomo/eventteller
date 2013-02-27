package news.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import news.crawler.articleCrawlerAndExtractor.ChineseSplit;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;
import util.Log;

import db.HSession;
import db.data.Word;
import db.data.article;
import db.data.event;

public class EventInfoGenerator {
	
	public Session session = null;
	
	
	
	public EventInfoGenerator(){
		if(null == session){
			session = new HSession().createSession();
		}
		Const.loadTaskid();
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
		String hql = "from event as obj where obj.day = " +String.valueOf(days);
//		String hql = "from event as obj where obj.day = 413";
		Query query = session.createQuery(hql);
		results = (List<event>)query.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getEventsFromDB(){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.updatestatus = 0" ;
		Query query = session.createQuery(hql);
		results = (List<event>)query.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<article> getArticleFromEvent(event en){
		List<article> results = new ArrayList<article>();
		int en_id = 0;
		en_id = en.getId();
		String hql = "from article as obj where obj.eventid ="+String.valueOf(en_id);
		results = (List<article>)session.createQuery(hql).list();
		return results;
	}
	
	public List<Word> combineAllArticleSummary(List<article> scrs){
		List<Word> results = new ArrayList<Word>();
		Map<String,Integer> tf_results = new HashMap<String,Integer>();
		Map<String,Double> score_results = new HashMap<String,Double>();
		if(null == scrs)
			return null;
		for(article at : scrs){
			String summary = at.getSummary();
			if(null != summary || !"".equalsIgnoreCase(summary)){
				String[] s_words = summary.split(",");
				for(String word : s_words){
					String[] word_sp =  word.split(" ");
					if(3 == word_sp.length){
						String temp_word = word_sp[0];
						int    temp_tf = Integer.valueOf(word_sp[1]);
						double temp_score = Double.valueOf(word_sp[2]);
						if(!tf_results.containsKey(temp_word)){
							tf_results.put(temp_word, temp_tf);
							score_results.put(temp_word, temp_score);
						}else{
							int now_tf = tf_results.get(temp_word);
							double now_score = score_results.get(temp_word);
							tf_results.put(temp_word, now_tf+temp_tf);
							score_results.put(temp_word, (now_score+temp_score)/2);
						}
					}

				}
			}
		}
		Iterator<String> it_words = tf_results.keySet().iterator();
		while(it_words.hasNext()){
			String str_word = it_words.next();
			Word new_word = new Word();
			int new_tf = 0;
			double new_score = 0;
			new_tf = tf_results.get(str_word);
			new_score = score_results.get(str_word);
			new_word.setName(str_word);
			new_word.setTf(new_tf);
			new_word.setScore(new_score);
			results.add(new_word);
		}	
		return results;
	}
	
	public String getMostSmallFromMap(Map<String,Double> scrs){
		double min = Double.MAX_VALUE;
		String str_min ="";
		Iterator<String> it_strs = scrs.keySet().iterator();
		while(it_strs.hasNext()){
			String temp = it_strs.next();
			if(scrs.get(temp) < min){
				str_min = temp;
				min = scrs.get(temp);
			}
		}
		return str_min;
	}
	
	public List<Word> getTopNWord(List<Word> scrs , int n){
		List<Word> results = new ArrayList<Word>();
		Map<String,Double> tmp_results = new HashMap<String,Double>();
		if(scrs.size() < n){
			n = Const.EventSummaryWordsTopNLest;
		}
		long num = 0;
		double min_score = -1;
		for(Word word : scrs){
			double total_sc = 0;
			total_sc = word.getTf()*word.getScore();
			if(num < n){
				tmp_results.put(word.getName(), total_sc);
				num++;
			}else if(total_sc > min_score){
				tmp_results.put(word.getName(), total_sc);
				String min_str = "";
				min_str = getMostSmallFromMap(tmp_results);
				tmp_results.remove(min_str);
				min_str = getMostSmallFromMap(tmp_results);
				min_score = tmp_results.get(min_str);
			}
		}
		for(Word word : scrs){
			if(tmp_results.containsKey(word.getName())){
				results.add(word);
			}
		}
		return results;
	}
	
	public String changeListToString(List<Word> scrs){
		String summary = "";
		for(Word word : scrs){
			String name = "";
			int   tf = 0;
			double score = 0;
			name = word.getName();
			tf = word.getTf();
			score = word.getScore();
			String str_word = name+" "+String.valueOf(tf)+" "+String.valueOf(score)+",";
			summary +=str_word;		
		}
		return summary;
	}
	
	public Map<String,Boolean> changeListToMap(List<Word> scrs){
		Map<String,Boolean> results = new HashMap<String,Boolean>();
		for(Word word : scrs){
			if(!results.containsKey(word.getName())){
				results.put(word.getName(), true);
			}
		}
		return results;
	}
	
	public int checkNumInTitle(String title , Map<String,Boolean> scrs){
		int results = 0;
		String[] title_words = title.split(",");
		for(String tw : title_words){
			String[] temp = tw.split(" ");
			if(temp.length == 2){
				if(scrs.containsKey(temp[0])){
					results++;
				}
			}
		}
		return results;
	}
	public String getTitleFromArticle(List<article> scrs , Map<String,Boolean> scr_words){
		int max_num = 0;
		String title = new String("");
		for(article at : scrs){
			int num = 0;
			num = checkNumInTitle(at.getTitlewords(),scr_words);
			if(num > max_num){
				title = at.getTitle();
			}
		}		
		return title;
	}
	
	public String getImgFromArticles(List<article> scrs,String title){
		int have_num = 0;
		int no_num = 0;
		String img = "";
		String results = "";
		for(article at : scrs){
			if(at.getImg() == null || at.getImg().equalsIgnoreCase("")){
				no_num++;
				continue;
			}
			if(at.getImg().contains("http")){
				results = at.getImg();
				have_num++;
			}
			if(at.getImg().indexOf("sohu") >= 0){
				img = at.getImg();
			}else if(at.getImg().indexOf("http://images.china.cn/")>= 0){
				img = at.getImg();
			}
			
		}
		if(!img.equalsIgnoreCase("")){
			results = img;
		}
		if(have_num*2 <= no_num){
			results = "";
		}
		return results;
	}
	
	public String getImg(List<article> scrs , String title){
		String results = "";
		for(article at : scrs){
			if(at.getImg() == null)
				continue;
			if(at.getTitle().equalsIgnoreCase(title) && at.getImg().toLowerCase().contains("http")){
				results = at.getImg();
				break;
			}
			if(at.getImg().toLowerCase().contains("http")){
				results = at.getImg();
			}
		}		
		return results;
	}
	
	public long  getNumContainInMain(String mainstr , Map<String,Boolean> scrs){
		long results = 0;
		if(null == mainstr){
			return results;
		}
		String[] words = mainstr.split(",");
		for(String word : words){
			String[] temp = word.split(" ");
			if(2 == temp.length){
				String name = temp[0];
				int   tf = Integer.valueOf(temp[1]);
				if(scrs.containsKey(name)){
					results += tf;
				}
			}
		}
		return results;
	}
	
	
	public String getSummary(List<article> scrs , Map<String,Boolean> scr_words){
		String results = "";
		article at_results = new article();
		long  num_results = 0;
		for(article at : scrs){
			String mainwords = at.getMainwords();
			long num_temp = 0;
			num_temp = getNumContainInMain(mainwords,scr_words);
			if(num_temp > num_results){
				at_results = at;
				num_results = num_temp;
			}
		}
		if(num_results > 0){
			results = at_results.getMainparagraph();
		}
		return results;
	}
	
	public void updateEventDB(event en){
		Transaction tx = session.beginTransaction();
		session.merge(en);
		tx.commit();
		session.flush();		
	}
	
	public void updateEventDBList(List<event> scrs){
		Transaction tx = session.beginTransaction();
		for(event en : scrs){
			session.merge(en);
		}
		tx.commit();
		session.flush();
	}
	
	public String getArticlesToText(List<article> scrs){
		String results = "";
		for(article at : scrs){
			long id = 0;
			id = at.getId();
			results += String.valueOf(id) + " ";
		}
		return results;
	}
	
	public int getMostSubTopicIDFromArticles(List<article> scrs){
		int[] subtopicids = new int[12];
		int results = 0;
		int max_id = 0;
		for(int i = 0;i<12;i++){
			subtopicids[i] = 0;
		}
		for(article at : scrs){
			for(int i = 1; i<12;i++){
				if((at.getSubtopicid()&(1<<i)) == (1<<i)){
					subtopicids[i]++;
					
				}
			}
		}
		for(int i=1;i<12;i++){
			if(subtopicids[i] > results){
				results = subtopicids[i];
				max_id = i;
			}
		}	
		return max_id;
	}
	
	public int getProvinceFromTitle(String title){
		Const.loadChinaProvince();
		int result = -1;
		if(title.length() <= 1)
			return result;
		List<Word> words = ChineseSplit.SplitStrWithPOS(title);
		for(Word word : words){
			if(word.getNature().equals("ns")){
				String ps = word.getName();
				ps = ps.replace("市", "");
				ps = ps.replace("省", "");
				if(Const.ZH_PS.containsKey(ps)){
					result = Const.ZH_PS.get(ps);
				}
				return result;
			}			
		}		
		return result;
	}
	
	
	public event setEventContent(event en){
		List<article> articles = new ArrayList<article>();
		List<Word> words = new ArrayList<Word>();
		Map<String,Boolean> tmp_mp = new HashMap<String,Boolean>();
		Date time = new Date();
		String summarywords  = "";
		String summary = "";
		String title = "";
		String articles_text = "";
		String img = "";
		int ps = -1;
		Integer subtopicid = 1;
		articles = getArticleFromEvent(en);
		if(articles.size() <= Const.EventInfoGeneratorLeastNum)
			return en;
		if(articles.size() > 0){
			time = articles.get(0).getCrawltime();
		}
		words = combineAllArticleSummary(articles);
		words = getTopNWord(words , Const.EventSummaryWordsTopN);
		summarywords = changeListToString(words);
		tmp_mp = changeListToMap(words);
		title = getTitleFromArticle(articles , tmp_mp);
		img = getImgFromArticles(articles,title);
		summary = getSummary(articles , tmp_mp);
		articles_text = getArticlesToText(articles);
		subtopicid = getMostSubTopicIDFromArticles(articles);
		ps = getProvinceFromTitle(title);
		en.setSummarywords(summarywords);
		en.setSummary(summary);
		en.setNumber(articles.size());
		en.setArticles(articles_text);
		en.setTitle(title);
		en.setImg(img);
		en.setSubtopicid(subtopicid);
		en.setProvince(ps);
		en.setTime(time);
		en.setUpdatestatus(1);
//		System.out.println("update for evnet " + en.getId()+"--"+en.getTitle()+"--"+en.getArticles());		
		return en;
	}
	
	
	
	public void runTask(){
		
		while(true){
			List<event> results_en = new ArrayList<event>();
//			int maxDay = 0;
			int num_update = 0;
			List<event> events = new ArrayList<event>();
//			maxDay = getMaxDayFromEventDB();
//			events = getMaxDayEventFromDB(maxDay);
			events = getEventsFromDB();
			System.out.println(events.size());
			for(event en : events){
				num_update++;
				event en_new = new event();
				en_new = setEventContent(en);
				results_en.add(en_new);
				if(num_update % 500 == 0){
					System.out.println((double)num_update * 100 / (double)events.size() + " %");
					updateEventDBList(results_en);
					results_en.clear();
				}
			}
			updateEventDBList(results_en);
			try {
				System.out.println("now end of EventInfoGenerator,sleep for:"+Const.EventInfoGeneratorSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end EventInfoGenerator,sleep for:"+Const.EventInfoGeneratorSleepTime/1000/60+" minutes");
				Thread.sleep(Const.EventInfoGeneratorSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in EventInfoGenerator!");
				e.printStackTrace();
			}						
		}	
	}
	
	
	public static void main(String[] args){
		EventInfoGenerator eig = new EventInfoGenerator();
		eig.runTask();		
	}
	
	

}
