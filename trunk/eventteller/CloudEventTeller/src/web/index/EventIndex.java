package web.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;



import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import org.dom4j.Node;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;


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
		Node elm = cfg.selectNode("/Configs/Config[@name='WebEventIndexPath']/Path");
		IndexPath =elm.getText();	
		session = new HSession().createSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<event> getEventFromDB(){
		String hql = "from event as obj where obj.taskstatus="+Const.TopicToIndex;
		Query query = session.createQuery(hql);
		query.setMaxResults(Const.ArticleToWebIndexNum);
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
	
	
	
	public int createOrUpdateIndex(){
		IndexWriter iw = CreateIndex();
		List<event> ls_ia = new ArrayList<event>();
		List<event> ls_up_at = new ArrayList<event>();
		ls_ia = getEventFromDB();
		if(ls_ia==null){
			return 0;
		}
		for(event ia : ls_ia){
			int id = 0;
			String title = "";
			id = ia.getId();
			title = ia.getTitle();
			if(iw!=null){
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("id",String.valueOf(id),Field.Store.YES,Field.Index.NO));
				doc.add(new Field("title",title,Field.Store.NO,Field.Index.ANALYZED));
				try {
					iw.addDocument(doc);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ia.setTaskstatus(Const.TopicToWebIndex);
			ls_up_at.add(ia);
			System.out.println(ia.getId());
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
	
	public void runTask(){
		Const.loadTaskid();
		while(true){
			int num = 0;
			num = createOrUpdateIndex();
			if(num == 0){
				System.out.println("there is no more event to update...");
				try {
					System.out.println("now end of WebEventIndex,sleep for:"+Const.ArticleIndexSleepTime/1000/60+" minutes. "+new Date().toString());
					Log.getLogger().info("end ArticleIndex,sleep for:"+Const.ArticleIndexSleepTime/1000/60+" minutes");
					Thread.sleep(Const.ArticleIndexSleepTime);
				} catch (InterruptedException e) {
					Log.getLogger().error("can't sleep in ArticleIndex!");
					e.printStackTrace();
				}				
			}
		}
	}
	
	

	public static void main(String[] args){
		EventIndex ei = new EventIndex();
		ei.runTask();
	}

}
