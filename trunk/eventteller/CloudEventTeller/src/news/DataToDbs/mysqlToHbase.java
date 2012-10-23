package news.DataToDbs;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;
import util.Log;

import db.data.titleNews;
import db.HSession;

/**
* @PackageName:news.DataToDbs
* @ClassName: mysqlToHbase
* @author: mblank
* @date: 2012-3-6 下午8:56:50
* @Description: get id+urls from mysql,put them into hbase
* @Marks: titlenews----->EventTitle
*/
public class mysqlToHbase {
	
	public List<titleNews> results;
	public Session session;
	public Configuration conf;
	
	public mysqlToHbase(){
		conf = new Configuration();
		conf.addResource(new Path(Const.HBASE_CONFIG_PATH_Local));
		conf = HBaseConfiguration.create(conf); 
		session = new HSession().createSession();
	}
	
	/**
	 * @return
	 * @Description:get all the items from mysql
	 */
	@SuppressWarnings("unchecked")
	public List<titleNews> getTitleNewsFromDB(){

		String hql = "from titleNews as obj where obj.taskStatus="+Const.TASKID.get("urlToMysql");
		Query query = session.createQuery(hql);
		List<titleNews> ls_tn = (List<titleNews>)query.list();
		Log.getLogger().info("Find titles in db: "+ls_tn.size());		
		return ls_tn;
	}
	
	/**
	 * @param tn
	 * @Description:insert id+url to hbase
	 */
	public void insertToHbase(HTable ht,titleNews tn){
		Const.loadTaskid();
		Put put = new Put(Bytes.toBytes(tn.getId()));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("url"), Bytes.toBytes(tn.getUrl()));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("crawltime"), Bytes.toBytes(tn.getCrawlTime().toString()));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("status"), Bytes.toBytes("2"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("subtopicid"), Bytes.toBytes(String.valueOf(tn.getSubtopicId())));
		try {
			ht.put(put);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param tn
	 * @Description:update db (titlenews)
	 */
	public void updateDB(titleNews tn){
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(tn);
		tx.commit();
		session.flush();
	}
	
	/**
	 * @param ls_tn
	 * @Description:put the id+url to hbase and update the taskstatus of mysql
	 */
	public void updateNewTitleNews(List<titleNews> ls_tn){
		Transaction tx = session.beginTransaction();		
		Iterator<titleNews> it_tn = ls_tn.iterator();
		while(it_tn.hasNext()){
			titleNews tn = it_tn.next();
			session.saveOrUpdate(tn);
			System.out.println(tn.getId());
		}
		tx.commit();
		session.flush();
		Log.getLogger().info("update new titlesStatus: "+ls_tn.size());
	}
	
	/**
	 * 
	 * @Description:just run
	 */
	public void runTask(){
		Const.loadTaskid();
		try {
			HTable ht = new HTable(conf,Bytes.toBytes("EventTitle"));
			List<titleNews> ls_tn = getTitleNewsFromDB();
			Iterator<titleNews> it_tn = ls_tn.iterator();
			System.out.println("start!.......");
			while(it_tn.hasNext()){
				titleNews tn = it_tn.next();
				insertToHbase(ht,tn);
				tn.setTaskStatus(Const.TASKID.get("UrlFromMysqlToHbase"));
			}
			updateNewTitleNews(ls_tn);
			System.out.println("end! put ("+ls_tn.size()+") urls to hbase!");
			ht.close();
		} catch (IOException e) {
			Log.getLogger().error("Can't load the HTable!");
			e.printStackTrace();
		}
	}
	

	
	public static void main(String[] args){
		Const.loadTaskid();
		mysqlToHbase mth = new mysqlToHbase();
		while(true){
			
			mth.runTask();
			try {
				System.out.println("now end of mysqlToHbase,sleep for:"+Const.MysqlToHbaseSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end crawler,sleep for:"+Const.MysqlToHbaseSleepTime/1000/60+" minutes");
				Thread.sleep(Const.MysqlToHbaseSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in mysqlToHbase!");
				e.printStackTrace();
			}
		}		
	}
	
	
	
}
