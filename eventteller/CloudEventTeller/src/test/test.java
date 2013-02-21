package test;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;


import util.Const;
import db.HSession;
import db.data.article;
import db.data.event;



class DoubleWords{
	public String words;
	public int count;
	
}


@SuppressWarnings("rawtypes")
class ComparatorWords implements Comparator{

	 public int compare(Object arg0, Object arg1) {
		 DoubleWords eta=(DoubleWords)arg0;
		 DoubleWords etb=(DoubleWords)arg1;		 
		 if(eta.count > etb.count){
			 return -1;
		 }else if(eta.count < etb.count){
			 return 1;
		 }else{
			 return 0;
		 }
	 }
	
} 


public class test {
	
private Session  session;
	
	public test(){
		if(session==null){
			session = new HSession().createSession();
		}
	}
	
	/**
	 * @param id
	 * @return
	 * @Description:from id to get article
	 */
	@SuppressWarnings("unchecked")
	public article getArticleById(String id){
		List<article> results = new ArrayList<article>();
		String hql = "from article as obj where obj.id="+id;
		Query query = session.createQuery(hql).setMaxResults(Const.MysqlToIndexMaxItemNum);
		results = (List<article>)query.list();
		article result = new article();
		if(results.size()>0){
			result = results.get(0);
		}
		return result;
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

	
	

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException{

		String file_path = "D:\\ET_TEST\\res";
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file_path)));
		List<List<String>> all_words = new ArrayList<List<String>>();
		int id = 1560430;
		test tt = new test();
		event et = tt.getEventById(id);
		System.out.println(et.getArticles());
		
		String articles = et.getArticles();
		String[] at_ids = articles.split(" ");
		for(String at_id : at_ids){
			article at = tt.getArticleById(at_id);
			String main_paragraph = at.getMainparagraph().replace("<p>", "").replace("</p>", "");
			List<String> words = news.crawler.articleCrawlerAndExtractor.ChineseSplit.SplitStr(main_paragraph);
			all_words.add(words);
		}
		
		Map<String,Integer> res = new HashMap<String,Integer>();
		
		for(List<String> it_words : all_words){
			for(String it_word : it_words){
				if(it_word.length() == 1)
					continue;
				for(String tmp_word : it_words){
					if(it_word.equals(tmp_word) || tmp_word.length() == 1){
						continue;
					}
					if(res.containsKey(it_word + "\t" + tmp_word)){ 
						res.put(it_word+"\t"+tmp_word, res.get(it_word+"\t"+tmp_word) + 1);
					}else if(res.containsKey(tmp_word + "\t" + it_word)){
						res.put(it_word+"\t"+tmp_word, res.get(tmp_word + "\t" + it_word) + 1);
						res.remove(tmp_word + "\t" + it_word);
					}else{
						res.put(it_word+"\t" + tmp_word, 1);
					}					
				}
			}
		}
		
		System.out.println(res.size());
		
		List<DoubleWords> rank_res = new ArrayList<DoubleWords>();
		Iterator<String> tmp_its = res.keySet().iterator();
		while(tmp_its.hasNext()){
			String key = tmp_its.next();
			String[] its = key.split("\t");
			int count = 0;
			for(List<String> tmps : all_words){
				if(tmps.contains(its[0]) && tmps.contains(its[1])){
					count++;
				}
			}
			DoubleWords dw = new DoubleWords();
			dw.words = key;
			dw.count = count * res.get(key);
			rank_res.add(dw);
		}
		
		ComparatorWords comparator=new ComparatorWords();
	    Collections.sort(rank_res, comparator);
        for(DoubleWords tmp_dw : rank_res){
        	bw.write(tmp_dw.words + "\t" + tmp_dw.count + "\n");
        }
		bw.close();

		
	}
}