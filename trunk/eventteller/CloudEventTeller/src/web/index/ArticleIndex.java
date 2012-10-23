package web.index;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
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



import db.HSession;
import db.data.article;

import util.Config;
import util.Const;
import util.Log;

/**
* @PackageName:news.index
* @ClassName: ArticleIndex
* @author: mblank
* @date: 2012-3-30 下午7:16:11
* @Description: put articles which have eventid and not in index into index
* @Marks: 
*/
public class ArticleIndex {
	
	private  String IndexPath;
	private  Session session;
	
	public ArticleIndex(){
		Config cfg = new Config(Const.SYS_CONFIG_PATH);
		//get path from *.xml(maybe move to const is better!)
		Node elm = cfg.selectNode("/Configs/Config[@name='WebindexPath']/Path");
		IndexPath =elm.getText();	
		session = new HSession().createSession();
	}

	public String getIndexPath() {
		return IndexPath;
	}

	public void setIndexPath(String indexPath) {
		IndexPath = indexPath;
	}
	
	/**
	 * @return
	 * @Description:create index writer
	 */
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
	
	/**
	 * @return
	 * @Description:get article list from db,which eventid is not null and not be put into index
	 */
	@SuppressWarnings("unchecked")
	public List<article> getArticleFromDB(){
		String hql = "from article as obj where obj.taskstatus="+Const.TASKID.get("UseMysqlArticleToIndex");
//		String hql = "from article as obj";
		Query query = session.createQuery(hql);
		query.setMaxResults(Const.ArticleToWebIndexNum);
		List<article> ls_tns = (List<article>)query.list();
		if(ls_tns==null)
			return null;
		System.out.println("Find article (have eventid) in db: "+ls_tns.size());		
		return ls_tns;
	}
	
	/**
	 * @param ats
	 * @Description:update the article db
	 */
	public void updateDB(List<article> ats){
		Transaction tx = session.beginTransaction();
		for(article at:ats){
			session.saveOrUpdate(at);
		}		
		tx.commit();
		session.flush();	
	}

	
	
	/**
	 * @return
	 * @Description:update index and db
	 */
	public int createOrUpdateIndex(){
		int number = 0;
		List<article> ls_results = new ArrayList<article>();
		IndexWriter iw = CreateIndex();
		List<article> ls_ia = new ArrayList<article>();
		ls_ia = getArticleFromDB();
		if(ls_ia==null){
			return 0;
		}
		for(article ia : ls_ia){
			number++;
			int id = 0;
			String mainpara = "";
			id = ia.getId();
			mainpara = ia.getTitle();
			Date crawltime = new Date();
			crawltime = ia.getCrawltime();
			if(iw!=null){
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("id",String.valueOf(id),Field.Store.YES,Field.Index.NO));
				doc.add(new Field("title",mainpara,Field.Store.NO,Field.Index.ANALYZED));
				doc.add(new Field("crawltime",crawltime.toString(),Field.Store.YES,Field.Index.NO));
				try {
					iw.addDocument(doc);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(number%1000==0){
				System.out.println(ia.getId());
			}		
			ia.setTaskstatus(Const.TASKID.get("ArticleToWebIndex"));
			ls_results.add(ia);
		}
		try {
			iw.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateDB(ls_results);
		Log.getLogger().info("update db ok!");
		return ls_ia.size();
	}
	
	/**
	 * @return
	 * @Description:get indexsearcher
	 */
	public IndexSearcher openIndexSearcher(){
		IndexSearcher is = null;
		try {
			is = IndexUtil.SearchIndex(IndexPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}
	

	

	/**
	 * @param dt1
	 * @param dt2
	 * @return
	 * @Description:check date_1 and date_2 is same day or not
	 */
	public boolean checkSameDay(Date dt1 , Date dt2){
		boolean check = false;
		Calendar cl1 = Calendar.getInstance();
		Calendar cl2 = Calendar.getInstance();
		cl1.setTime(dt1);
		cl2.setTime(dt2);
		int day1 = cl1.get(Calendar.DAY_OF_YEAR);
		int day2 = cl2.get(Calendar.DAY_OF_YEAR);
		if(day1 == day2){
			check  = true;
		}
		return check;
	}
	

	public void runTask(){
		Const.loadTaskid();
		while(true){
			int num = 0;
			num = createOrUpdateIndex();
			if(num == 0){
				System.out.println("there is no more article to update...");
				try {
					System.out.println("now end of ArticleIndex,sleep for:"+Const.ArticleIndexSleepTime/1000/60+" minutes. "+new Date().toString());
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
		ArticleIndex ai = new ArticleIndex();
		ai.runTask();
	}
	
	

}
