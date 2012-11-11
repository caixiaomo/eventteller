package news.DataToDbs;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import news.core.IDF;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.Const;
import util.Log;
import db.HSession;
import db.data.Word;
import db.data.article;
import db.data.titleNews;




/**
* @PackageName:news.DataToDbs
* @ClassName: HbaseToMysql
* @author: mblank
* @date: 2012-3-30 下午12:14:05
* @Description: get article from hbase ,will update the idf and article's summary words score
* @Marks: 
*/
public class HbaseToMysql {
	
	public List<titleNews> results;
	public HTable  ht ;
	public Session session;
	public Configuration conf ;
	
	public HbaseToMysql(){
		conf = new Configuration();
		conf.addResource(new Path(Const.HBASE_CONFIG_PATH_Local));
		conf = HBaseConfiguration.create(conf); 
		try {
			ht = new HTable(conf,Bytes.toBytes("EventTitle"));
			ht.setAutoFlush(false);
		} catch (IOException e) {
			Log.getLogger().error("Can't load the HTable!");
			e.printStackTrace();
		}
		session = new HSession().createSession();
	}

	/**
	 * @param result
	 * @param family
	 * @param qualifier
	 * @return
	 * @Description:get string value from hbase result
	 */
	public String getValueFromResult(Result result,String family,String qualifier){
		String result_str = "";
		byte[] temp = result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier));		
		if(temp==null){
			return result_str;
		}
		if(temp.length>0){
			result_str = Bytes.toString(temp);
		}
		return result_str;
	}
	
	/**
	 * @param bigs
	 * @param smalls
	 * @return
	 * @Description:combine two Word List
	 */
	public List<Word> combineWordList(List<Word> bigs , List<Word> smalls){
		for(Word word : smalls){
			if(word!=null){
				bigs.add(word);
			}
		}
		return bigs;
	}
	
	/**
	 * @param scrs
	 * @return
	 * @Description:transform word list into a map
	 */
	public Map<String,Double> getScoreMapFromList(List<Word> scrs){
		Map<String,Double> results = new HashMap<String,Double>();
		for(Word word : scrs){
			if(!results.containsKey(word)&&word.getName()!=null){
				results.put(word.getName(), word.getScore());
			}
		}		
		return results;
	}
	
	/**
	 * @param scrs
	 * @param scores
	 * @return
	 * @Description: according the score map ,update the article's summary words
	 */
	public List<article> updateArticleScore(List<article> scrs ,Map<String,Double> scores){
		long num = 0;
		List<article> results = new ArrayList<article>();
		for(article at : scrs){
			String temp_summary = "";
			String summary = at.getSummary();
			String[] terms = summary.split(",");
			for(String term : terms){
				String temp_word = term.split(" ")[0];
				if(temp_word==null||temp_word.length()<=0||temp_word.length()>20)
					continue;
				double temp_score = scores.get(temp_word);
				String new_term = term + " " + String.valueOf(temp_score);				
				temp_summary +=new_term + ",";				
			}
			if(summary.equalsIgnoreCase("")){
				if(num%2==0){
					System.out.println("summary is null! "+ (num));
				}
				num++;
			}else if(summary.length()<6000){
				at.setSummary(temp_summary);
				results.add(at);
			}
		}		
		return results;
	}
	
	/**
	 * @param ht
	 * @param result
	 * @Description:delete item from hbase
	 */
	public void deleteFromHbase(HTable ht , Result result){
		Delete delete = new Delete(result.getRow());
		try {
			ht.delete(delete);
		} catch (IOException e) {
			e.printStackTrace();
			Log.getLogger().error("Can't delete the hbase row :"+result.getRow().toString());
	    	updateHbase(Bytes.toInt(result.getRow()));
		}
	}
	
	/**
	 * @param result
	 * @return
	 * @Description:get article from hbase result
	 */
	public article getArticleFromResult(Result result){
		article at = new article();
		Const.loadTaskid();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		byte[] ids = result.getRow();
    	String title = getValueFromResult(result,"info","title");
    	String publishtime = getValueFromResult(result,"info","publishtime");
    	String mainparagraph = getValueFromResult(result,"info","mainparagraph");
    	String mainparagraphwords = getValueFromResult(result,"info","mainparagraphwords");
    	String titlewords = getValueFromResult(result,"info","titlewords");
    	String summarywords = getValueFromResult(result,"info","summarywords");	
    	String crawltime = getValueFromResult(result,"info","crawltime");	
    	String img = getValueFromResult(result,"info","img");
    	String imgs = getValueFromResult(result,"info","imgs");
    	String subtopicid =  getValueFromResult(result,"info","subtopicid");
    	int subtopic_int;
    	try{
    		subtopic_int = Integer.valueOf(subtopicid);
    	}catch(Exception e){
    		subtopic_int = 0;
    		e.printStackTrace();
    	}    	
    	at.setId(Bytes.toInt(ids));
    	at.setTitle(title);
    	at.setPublishtime(publishtime);
    	at.setMainparagraph(mainparagraph);//mainparagraph
    	at.setMainwords(mainparagraphwords);
    	at.setTitlewords(titlewords);
    	at.setSummary(summarywords);
    	at.setExtracttime(new Date());
    	at.setTaskstatus(Const.TASKID.get("HtmlFromHbaseToMysql"));
    	at.setEventid(0);
    	at.setImg(img);
    	at.setImgs(imgs);
   		at.setSubtopicid(subtopic_int);
    	try {
			at.setCrawltime(format.parse(crawltime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return at;
	}
	
	/**
	 * @return
	 * @Description:get articles from the htable
	 */
	public List<article> getArticleFromHbase(){
		List<article> ls_results = new ArrayList<article>();
		ResultScanner scanner;
		Scan scan = new Scan();
		scan.setFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), 
				Bytes.toBytes("status"), CompareOp.EQUAL, Bytes.toBytes("3")));
		scan.setCaching(10);
		scan.setMaxVersions();
		try {
			scanner = ht.getScanner(scan);
			for (Result result : scanner){
				article at = new article();
				at = getArticleFromResult(result);
				if(at.getMainparagraph().length() <= 0 )
					continue;
		    	ls_results.add(at);
		    	deleteFromHbase(ht,result);
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return ls_results;
	}
	
	/**
	 * @param tn
	 * @Description:update db (titlenews)
	 */
	public void updateDB(List<article> ats){
		
		for(article at:ats){	
			try{
				Transaction tx = session.beginTransaction();			
				session.merge(at);		
				tx.commit();
				session.flush();
			}catch(Exception e){
				System.out.println(at.getId());
				System.out.println(at.getImgs());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param id
	 * @Description:set the htable info-status
	 */
	public void updateHbase(int id){
		
		Const.loadTaskid();
		Put put = new Put(Bytes.toBytes(id));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("status"), Bytes.toBytes("4"));
		try {
			ht.put(put);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public void runTask() throws IOException{
		Const.loadTaskid();
		List<article> ls_results = new ArrayList<article>();
		ls_results = getArticleFromHbase();		
		if(ls_results.size()>0){		
			List<Word> word_results = new ArrayList<Word>();
			Map<String,Integer> in_mp_words = new HashMap<String,Integer>();
			IDF idf = new IDF();
			//get words map from articles
			in_mp_words = idf.getMapFromArticleList(ls_results);
			if(ls_results!=null){
				for(int i = 0;i<Const.IDF_DB_Split_Nums; i++){
					//update the idf tables					
					long nums = 0; 
					Map<String,Word> exsitsMap = new HashMap<String,Word>();					
					Map<String,Integer> in_temp_words = new HashMap<String,Integer>();					
					List<Word> get_words = new ArrayList<Word>();
					List<Word> in_words = new ArrayList<Word>();	
					//get the idf_i table word list
					in_temp_words = idf.getSubListFromTotal(in_mp_words , i);
					//get old words from idf_i db
					get_words = idf.getWordsFromSubIDFDB(i);
					exsitsMap = idf.DBWordsToMap(get_words);	
					//combine old words and new words
					in_words = idf.combineOldAndNew(exsitsMap,in_temp_words);
					//get total number of articles
					nums = idf.getNumFromArticle();		
					//set score of words in idf_i
					in_words = idf.setWordScore(in_words , nums);
					//update the idf_i table
					idf.updateSubIDFDB(in_words,i);
					//combine small idf_i table to total mp
					word_results = combineWordList(word_results,in_words);
					if(i % 100 == 0){
						System.out.println("batch " + i +"...updated "+ in_words.size()+" words");
					}					
				}								
				updateDB(updateArticleScore(ls_results,getScoreMapFromList(word_results)));
			}
		}
	}
	
	public static void main(String[] args) throws IOException{

		HbaseToMysql htm = new HbaseToMysql();
		while(true){
			System.out.println("starting....."+new Date().toString());
			Log.getLogger().info("Start HbaseToMysql.....");
			htm.runTask();
			try {
				System.out.println("now end of HbaseToMysql,sleep for:"+Const.HbaseToMysqlSleepTime/1000/60+" minutes. "+new Date().toString());
				Log.getLogger().info("end crawler,sleep for:"+Const.HbaseToMysqlSleepTime/1000/60+" minutes");
				Thread.sleep(Const.HbaseToMysqlSleepTime);
			} catch (InterruptedException e) {
				Log.getLogger().error("can't sleep in HbaseToMysql!");
				e.printStackTrace();
			}
		}		
	}
}
