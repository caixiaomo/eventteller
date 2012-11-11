package news.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import news.crwaler.articleCrawlerAndExtractor.ChineseSplit;

import org.hibernate.Query;
import org.hibernate.Session;


import db.HSession;
import db.data.Topic;
import db.data.Word;
import db.data.event;

public class TopicKeyWords {
	
	public Session session;
	
	public TopicKeyWords(){
		if(null == session){
			session = new HSession().createSession();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Topic> getTopicFromDB(){
		List<Topic> results = new ArrayList<Topic>();
		String hql = "from Topic as obj where obj.updatestate = 1"; 
//		String hql = "from Topic as obj where obj.id = 724755"; 
		Query query = session.createQuery(hql);
		results = (List<Topic>)query.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getEventsFromTopic(Topic tp){
		List<event> results = new ArrayList<event>();
		String hql = "from event as obj where obj.topicid = " + tp.getId() ;
		Query query = session.createQuery(hql);
		results = (List<event>)query.list();	
		return results;
	}
	
	public List<String> getWordsOfTileFromEvents(List<event> scrs){
		List<String> results = new ArrayList<String>();
		List<String> results_n = new ArrayList<String>();
		List<Word> tmp_res = new ArrayList<Word>();
		String temp_titles = "";
		for(event et : scrs){
			if(et.getTitle() != "" || et.getTitle() != null){
				temp_titles += et.getTitle();
			}			
		}
		tmp_res = ChineseSplit.SplitStrOnlyPerson(temp_titles);	
		for(Word wd : tmp_res){
			results.add(wd.getName());
		}
		if(results.size() < 5)
			return results;
		int num = 0;
		for(String st : results){		
			if(num > 10 ||st.length()< 2){
				continue;
			}			
			results_n.add(st);
			num++;
		}
		return results_n;
	}
	
	public Date getLastDateOfTopic(List<event> scrs){
		Date dt = new Date();
		if(scrs.size() > 0){
			dt = scrs.get(0).getTime();
		}else{
			return dt;
		}
		for(event et : scrs){
			if(dt.compareTo(et.getTime()) < 0){
				dt = et.getTime();
			}
		}
		return dt;
	}
	
	/**
	 * @param scrs
	 * @return
	 * @Description:combine all the events togethor to get keywords
	 */
	public List<Word> getKeyWords(List<event> scrs){
		List<Word> results = new ArrayList<Word>();
		Map<String,Integer> tf_results = new HashMap<String,Integer>();
		Map<String,Double> score_results = new HashMap<String,Double>();
		if(null == scrs)
			return null;
		for(event at : scrs){
			String summary = at.getSummarywords();
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
		Map<String,Double> tmp_res = new HashMap<String,Double>();
		Map<String,Integer> names  = new HashMap<String,Integer>();
		for(event et : scrs){
			List<Word> ps = new ArrayList<Word>();
			if(et.getSummary().length() > 0 ){
				String summary = et.getSummary().replace("</p>", "");
				summary = summary.replace("<p>", "");
				ps = ChineseSplit.SplitStrOnlyPerson(summary);
				for(Word wd : ps){
					if(!names.containsKey(wd.getName())){
						names.put(wd.getName(), 1);
					}else{
						names.put(wd.getName(), names.get(wd.getName()) + 1);
					}
				}
			}
		}
		List<String> wordsFromTitle = getWordsOfTileFromEvents(scrs);
		for(String wdft : wordsFromTitle){
			if(names.containsKey(wdft)){
				names.put(wdft, names.get(wdft) + 2);
			}else{
				names.put(wdft, 3);
			}
		}
		Iterator<String> its = names.keySet().iterator();
		while(its.hasNext()){
			String key = its.next();
			int count = names.get(key);
			double score = 10.5;
			if(score_results.containsKey(key)){
				score = score_results.get(key);
			}
			score = count * score;
			tmp_res.put(key, score);
		}
		int top = 50;
		double max=0;
        String maxKey=null;
        for(int i=0;i<top&&i<tmp_res.size();i++){
            for(String key:tmp_res.keySet()){
                if(tmp_res.get(key)>max){
                    max=tmp_res.get(key);
                    maxKey=key;
                }
            }
            Word wd = new Word();
            wd.setName( maxKey);
            wd.setScore(max);
            max=0;
            tmp_res.put(maxKey, -1.0);
            results.add(wd);
        }
		return results;
	}
	
	public String KeyWordsStr(List<Word> scrs){
		String result = "";
		int check_one = 0;
		for(Word wd : scrs){
			String name = wd.getName();
			if(name == null)
				continue;
			if(name.length() > 1 && name.length() < 10){
				check_one++;
				int intscore = (int)wd.getScore();
				if(check_one == 1){
					
					result = name + "|" + intscore;
				}else{
					result += "," + name + "|" + intscore;
				}
			}
			if(check_one > 50 || result.length() > 1000)
				break;
		}
		return result;
	}
	
	public String getTitleFromKeyWords(List<Word> scrs){
		String result = "";
		int check_one = 0;
		for(Word wd : scrs){
			String name = wd.getName();
			if(name.length() > 1){
				check_one++;
				if(check_one == 1){
					result = name ;
				}else{
					result += " " + name ;
				}
			}
			if(check_one > 6)
				break;
		}
		return result;
	}
	
	public void updateTopicDB(List<Topic> topics){
		org.hibernate.Transaction tx = session.beginTransaction();
		for(Topic tp : topics){
			session.saveOrUpdate(tp);
		}
		tx.commit();
		session.flush();
	}
	
	public int getMostSubTopicIDFromArticles(List<event> scrs){
		int[] subtopicids = new int[12];
		int results = 0;
		int max_id = 0;
		for(int i = 0;i<12;i++){
			subtopicids[i] = 0;
		}
		for(event at : scrs){
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
		max_id = 0;
		return max_id;
	}
	
	public static Date getMaxTime(List<event> scrs){
		Date result = new Date();
		long max_time = -1;
		for(event et : scrs){
			if(et.getTime().getTime() > max_time){
				result = et.getTime();
				max_time = et.getTime().getTime();				
			}
		}
		return result;
	}
	
	public static Date getMinTime(List<event> scrs){
		Date result = new Date();
		long min_time = 157680000000000L;
		for(event et : scrs){
			if(et.getTime().getTime() < min_time){
				result = et.getTime();
				min_time = et.getTime().getTime();				
			}
		}
		return result;
	}
	
	
	public void runTask(){
		
		while(true){
			
			int num_check = 0;
			List<Topic> results = new ArrayList<Topic>();
			List<Word> summarys = new ArrayList<Word>();
			List<Topic> tps = getTopicFromDB();
			if(tps == null)
				continue;
			for(Topic tp : tps){
				num_check++;
				System.out.println("-------"+tp.getId());
				String temps_ids = "";
				List<event> events = new ArrayList<event>();
				events = getEventsFromTopic(tp);
				summarys = getKeyWords(events);				
				Integer subtopicid = 0;
				for(event en : events){
					temps_ids += en.getId().toString() + " ";
				}
				subtopicid = getMostSubTopicIDFromArticles(events);
				tp.setKeywords(KeyWordsStr(summarys));
				if(tp.getKeywords().length() > 1000){
					System.out.println(tp.getKeywords());
				}
				tp.setTitle(getTitleFromKeyWords(summarys));
				tp.setUpdatestate(0);
				/// if there is no enough words to show , the topic will be drop down 
				if(summarys.size() < 10){
					tp.setNumber(1);
				}else{
					tp.setNumber(events.size());
				}				
				tp.setTime(getLastDateOfTopic(events));
				tp.setEvents(temps_ids);
				tp.setSubtopicid(subtopicid);
				tp.setStartTime(getMinTime(events));
				tp.setEndTime(getMaxTime(events));
				results.add(tp);
				System.out.println("---"+tp.getTitle());
				if(num_check % 1 == 0){
					System.out.println("update " + tp.getId() + " "+ tp.getTitle());
					updateTopicDB(results);	
					results.clear();
				}
			}
			updateTopicDB(results);				
			try {
				System.out.println("Topic keywords sleep for 30 minitues...");
				Thread.sleep(30*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		TopicKeyWords tkw = new TopicKeyWords();
		tkw.runTask();
	}
	
}
