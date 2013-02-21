package news.index;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.Date;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.TreeMap;

import news.crawler.articleCrawlerAndExtractor.ChineseSplit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.dom4j.Node;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.wltea.analyzer.lucene.IKQueryParser;

import util.Config;
import util.Const;
import util.Log;
import db.HSession;
import db.data.event;

public class EventIndex extends ArticleIndex{
	
	private String IndexPath;
	private Session session;

	public String getIndexPath() {
		return IndexPath;
	}

	public void setIndexPath(String indexPath) {
		IndexPath = indexPath;
	}
	
	public EventIndex(){
		Config cfg = new Config(Const.SYS_CONFIG_PATH);
		//get path from *.xml(maybe move to const is better!)
		Node elm = cfg.selectNode("/Configs/Config[@name='EventIndexPath']/Path");
		IndexPath =elm.getText();	
		session = new HSession().createSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getEventFromDB(){
		String hql = "from event as obj where obj.taskstatus="+Const.EventToTopic;
		Query query = session.createQuery(hql);
		query.setMaxResults(Const.MysqlToIndexMaxItemNum);
		List<event> ls_tns = (List<event>)query.list();
		if(ls_tns==null)
			return null;
		Log.getLogger().info("Find event (have topicid) in db: "+ls_tns.size());		
		return ls_tns;
	}
	
	public void updateEventDB(List<event> ens){
		Transaction tx = session.beginTransaction();
		for(event en : ens){
			session.saveOrUpdate(en);
		}		
		tx.commit();
		session.flush();	
	}
	
	public IndexWriter CreateIndex(){
		IndexWriter iw = null;
		try{
			iw = IndexUtil.createIndex(IndexPath);
		}catch(Exception e){
			Log.getLogger().info("Create indexWriter failed!");
			e.printStackTrace();
		}
		return iw;		
	}
	
	public IndexSearcher openIndexSearcher(){
		IndexSearcher is = null;
		try {
			is = IndexUtil.SearchIndex(IndexPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}
	
	public event getEventByid(int id){
		event results = new event();
		String hql = "from event as obj where obj.id = "+id;
		results = (event)session.createQuery(hql).uniqueResult();		
		return results;
	}
	
	public List<event> getEventsFromIndex(String text) throws IOException{
		List<event> results = new ArrayList<event>();
		String temp_text = "";
		List<String> titleWords = ChineseSplit.SplitStr(text);
		for(String st : titleWords){
			temp_text +=st+" ";
		}
//		Map<Date , event> temp_sort = new TreeMap<Date,event>();
		IndexSearcher is = null;
		try {
			is = IndexUtil.SearchIndex(IndexPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Integer> ls_results = new ArrayList<Integer>();
		Map<Integer,Boolean> temp_results = new HashMap<Integer,Boolean>();
		org.apache.lucene.search.Query query=IKQueryParser.parse("title",temp_text);
		TopDocs docs=is.search(query, Const.ReleventEvents);
		for(int i=0;i<docs.scoreDocs.length;i++){
			int docId=docs.scoreDocs[i].doc;
			Document doc=is.doc(docId);
			int id=Integer.parseInt(doc.get("id"));
			if(!temp_results.containsKey(id)){
				ls_results.add(id);
				temp_results.put(id, true);
			}
		}	
		for(Integer it : ls_results){
	     	event temp_at = new event();
	    	temp_at = getEventByid(it);
	    	if(temp_at == null)
		    	continue;
		results.add(temp_at);
	    }
//		for(Integer it : ls_results){
//			event temp_at = new event();
//			temp_at = getEventByid(it);
//			if(temp_at == null)
//				continue;
//			temp_sort.put(temp_at.getTime(), temp_at);
//		}
//		Iterator<Date> it_res = temp_sort.keySet().iterator();
//		while(it_res.hasNext()){
//			Date dt = it_res.next();
//			event tempa = temp_sort.get(dt);
//			for(String word : titleWords){
//				if(tempa.getTitle().indexOf(word) > 0){
//					results.add(tempa);
//					break;
//				}	
//			}	
//			results.add(tempa);
//		}
//		List<event> temp_res = new ArrayList<event>();
//		if(results.size() >=1){
//			for(int i= results.size()-1;i>=0;i--){
//				temp_res.add(results.get(i));
//			}
//		}		
		is.close();
//		return temp_res;
		return results;
	}
	
//	public int createOrUpdateIndex(){
//		IndexWriter iw = CreateIndex();
//		List<event> ls_ia = new ArrayList<event>();
//		List<event> ls_up_at = new ArrayList<event>();
//		ls_ia = getEventFromDB();
//		if(ls_ia==null){
//			return 0;
//		}
//		for(event ia : ls_ia){
//			int id = 0;
//			String mainpara = "";
//			id = ia.getId();
//			mainpara = ia.getSummary();
//			int day = 0;
//			day = ia.getDay();
//			if(iw!=null){
//				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
//				doc.add(new Field("id",String.valueOf(id),Field.Store.YES,Field.Index.NO));
//				doc.add(new Field("mainpara",mainpara,Field.Store.NO,Field.Index.ANALYZED));
//				doc.add(new Field("day",String.valueOf(day),Field.Store.YES,Field.Index.NO));
//				try {
//					iw.addDocument(doc);
//				} catch (CorruptIndexException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			ia.setTaskstatus(Const.TopicToIndex);
//			ls_up_at.add(ia);
//		}
//		try {
//			iw.close();
//		} catch (CorruptIndexException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		updateEventDB(ls_up_at);
//		Log.getLogger().info("update db ok!");
//		return ls_ia.size();
//	}
	
	public int createOrUpdateIndexFromList(List<event> ls_ia){
		IndexWriter iw = CreateIndex();
		List<event> ls_up_at = new ArrayList<event>();
		if(ls_ia==null){
			return 0;
		}
		for(event ia : ls_ia){
			int id = 0;
			String mainpara = "";
			id = ia.getId();
			mainpara = ia.getSummary();
			int day = 0;
			day = ia.getDay();
			if(iw!=null){
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("id",String.valueOf(id),Field.Store.YES,Field.Index.NO));
				doc.add(new Field("mainpara",mainpara,Field.Store.NO,Field.Index.ANALYZED));
				doc.add(new Field("day",String.valueOf(day),Field.Store.YES,Field.Index.NO));
				try {
					iw.addDocument(doc);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ia.setTaskstatus(Const.EventToIndex);
			ls_up_at.add(ia);
		}
		try {
			iw.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateEventDB(ls_up_at);
		Log.getLogger().info("update db ok!");
		return ls_ia.size();
	}
	
	public boolean checkNearDay(int day_a ,int day_b){
		boolean results = false;
		int day_cha = 0;
		day_cha = Math.abs(day_a - day_b);
		if( day_cha <= Const.TopicNearDayNum && day_cha >0){
			results = true;
		}		
		return results;
	}
	
	public List<Integer> getIdFromIndex(IndexSearcher is ,String text,int scr_day) throws IOException{
		List<Integer> ls_results = new ArrayList<Integer>();
		Map<Integer,Boolean> temp_results = new HashMap<Integer,Boolean>();
		org.apache.lucene.search.Query query=IKQueryParser.parse("mainpara",text);
		TopDocs docs=is.search(query, Const.NEIGHBORHORDSIZE);
		for(int i=0;i<docs.scoreDocs.length;i++){
			int docId=docs.scoreDocs[i].doc;
			Document doc=is.doc(docId);
			int id=Integer.parseInt(doc.get("id"));
			String date_old = "";
			date_old = doc.get("day");
			int day_old = 0;
			day_old = Integer.valueOf(date_old);
			if(!temp_results.containsKey(id)&&checkNearDay(scr_day,day_old)){
				ls_results.add(id);
				temp_results.put(id, true);
			}
		}		
		return ls_results;
	}

	public static void main(String[] args){
		EventIndex ei = new EventIndex();
		ei.createOrUpdateIndex();
	}

}
