package news.index;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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
		Node elm = cfg.selectNode("/Configs/Config[@name='indexPath']/Path");
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
		String hql = "from article as obj where obj.taskstatus="+Const.TASKID.get("ArticleToEvent")+" and obj.eventid !=0";
		Query query = session.createQuery(hql);
		query.setMaxResults(Const.MysqlToIndexMaxItemNum);
		List<article> ls_tns = (List<article>)query.list();
		if(ls_tns==null)
			return null;
		Log.getLogger().info("Find article (have eventid) in db: "+ls_tns.size());		
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
	public int createOrUpdateIndex(List<article> ls_ia){
		IndexWriter iw = CreateIndex();
		List<article> ls_up_at = new ArrayList<article>();
		if(ls_ia==null){
			return 0;
		}
		for(article ia : ls_ia){
			int id = 0;
			String mainpara = "";
			id = ia.getId();
			mainpara = ia.getMainparagraph();
			Date crawltime = new Date();
			crawltime = ia.getCrawltime();
			if(iw!=null){
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("id",String.valueOf(id),Field.Store.YES,Field.Index.NO));
				doc.add(new Field("mainpara",mainpara,Field.Store.NO,Field.Index.ANALYZED));
				doc.add(new Field("crawltime",crawltime.toString(),Field.Store.YES,Field.Index.NO));
				try {
					iw.addDocument(doc);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ia.setTaskstatus(Const.TASKID.get("UseMysqlArticleToIndex"));
			ls_up_at.add(ia);
		}
		try {
			iw.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateDB(ls_up_at);
		Log.getLogger().info("update db ok!");
		return ls_ia.size();
	}
	
	
	/**
	 * @return
	 * @Description:update index and db
	 */
	public int createOrUpdateIndex(){
		IndexWriter iw = CreateIndex();
		List<article> ls_ia = new ArrayList<article>();
		List<article> ls_up_at = new ArrayList<article>();
		ls_ia = getArticleFromDB();
		if(ls_ia==null){
			return 0;
		}
		for(article ia : ls_ia){
			int id = 0;
			String mainpara = "";
			id = ia.getId();
			mainpara = ia.getMainparagraph();
			Date crawltime = new Date();
			crawltime = ia.getCrawltime();
			if(iw!=null){
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("id",String.valueOf(id),Field.Store.YES,Field.Index.NO));
				doc.add(new Field("mainpara",mainpara,Field.Store.NO,Field.Index.ANALYZED));
				doc.add(new Field("crawltime",crawltime.toString(),Field.Store.YES,Field.Index.NO));
				try {
					iw.addDocument(doc);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ia.setTaskstatus(Const.TASKID.get("UseMysqlArticleToIndex"));
			ls_up_at.add(ia);
		}
		try {
			iw.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateDB(ls_up_at);
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
	
	public article getArticleByid(int id){
		article results = new article();
		String hql = "from article as obj where obj.id = "+id;
		results = (article)session.createQuery(hql).uniqueResult();		
		return results;
	}
	
	public List<article> getArticlesFromIndex(String text) throws IOException{
		List<article> results = new ArrayList<article>();
		List<String> titleWords = ChineseSplit.SplitStr(text);
		String temp_text = "";
		for(String st : titleWords){
			temp_text +=st+" ";
		}
		Map<Date , article> temp_sort = new TreeMap<Date,article>();
		IndexSearcher is = null;
		try {
			is = IndexUtil.SearchIndex(IndexPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Integer> ls_results = new ArrayList<Integer>();
		Map<Integer,Boolean> temp_results = new HashMap<Integer,Boolean>();
		org.apache.lucene.search.Query query=IKQueryParser.parse("title",temp_text);
		TopDocs docs=is.search(query, Const.ReleventArticles);
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
			article temp_at = new article();
			temp_at = getArticleByid(it);
			if(temp_at == null)
				continue;
			temp_sort.put(temp_at.getExtracttime(), temp_at);
		}
		Iterator<Date> it_res = temp_sort.keySet().iterator();
		while(it_res.hasNext()){
			Date dt = it_res.next();
			article tempa = temp_sort.get(dt);
			for(String word : titleWords){
				if(tempa.getTitle().indexOf(word) > 0){
					results.add(tempa);
					break;
				}
			}			
		}
		List<article> temp_res = new ArrayList<article>();
		if(results.size() >=1){
			for(int i= results.size()-1;i>=0;i--){
				temp_res.add(results.get(i));
			}
		}		
		is.close();
		return temp_res;
	}
	

	
	
	
	/**
	 * @param is
	 * @param text
	 * @return
	 * @throws IOException
	 * @Description:get relevent ids from index 
	 */
	public List<Integer> getIdFromIndex(IndexSearcher is ,String text) throws IOException{
		List<Integer> ls_results = new ArrayList<Integer>();
		Map<Integer,Boolean> temp_results = new HashMap<Integer,Boolean>();
		org.apache.lucene.search.Query query=IKQueryParser.parse("mainpara",text);
		TopDocs docs=is.search(query, Const.NEIGHBORHORDSIZE);
		for(int i=0;i<docs.scoreDocs.length;i++){
			int docId=docs.scoreDocs[i].doc;
			Document doc=is.doc(docId);
			int id=Integer.parseInt(doc.get("id"));
			if(!temp_results.containsKey(id)){
				ls_results.add(id);
				temp_results.put(id, true);
			}
		}		
		return ls_results;
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
	
	/**
	 * @param is
	 * @param text
	 * @param date
	 * @return
	 * @throws IOException
	 * @Description:get relevent ids(about text) from index(only ids in same day will be returned) 
	 */
	public List<Integer> getIdFromIndex(IndexSearcher is ,String text,Date date) throws IOException{
		List<Integer> ls_results = new ArrayList<Integer>();
		Map<Integer,Boolean> temp_results = new HashMap<Integer,Boolean>();
		org.apache.lucene.search.Query query=IKQueryParser.parse("mainpara",text);
		
		TopDocs docs=is.search(query, Const.NEIGHBORHORDSIZE);
		for(int i=0;i<docs.scoreDocs.length;i++){
			int docId=docs.scoreDocs[i].doc;
			Document doc=is.doc(docId);
			int id=Integer.parseInt(doc.get("id"));
			String date_old = "";
			date_old = doc.get("crawltime");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date_oldd = new Date();
			try {
				date_oldd = sdf.parse(date_old);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if(!temp_results.containsKey(id)&&checkSameDay(date_oldd,date)){
				ls_results.add(id);
				temp_results.put(id, true);
			}
		}		
		return ls_results;
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
