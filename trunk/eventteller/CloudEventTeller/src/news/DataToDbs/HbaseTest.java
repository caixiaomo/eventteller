package news.DataToDbs;

import java.io.IOException;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;


import util.Const;


/**
* @PackageName:news.DataToDbs
* @ClassName: HbaseTest
* @author: mblank
* @date: 2012-3-30 上午11:35:55
* @Description: not used! 
* @Marks: TODO
*/
public class HbaseTest {


	public HTable  ht ;

	public Configuration conf ;
	
	
	/**
	 * @param tn
	 * @Description:insert id+url to hbase
	 */
	public void insertToHbase(HTable ht,Result result){
		Const.loadTaskid();
		Put put = new Put(result.getRow());
		
		put.add(Bytes.toBytes("info"), Bytes.toBytes("url"), Bytes.toBytes("test1"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("status"), Bytes.toBytes(1));
		try {
			ht.put(put);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void runTask() throws IOException{
		
		long num = 0;
		conf = new Configuration();
//		conf.setLong(HConstants.HBASE_REGIONSERVER_LEASE_PERIOD_KEY, 120000);
		conf.addResource(new Path(Const.HBASE_CONFIG_PATH_Local));
		conf = HBaseConfiguration.create(conf); 
		ht = new HTable(conf,Bytes.toBytes("test"));
		ht.setAutoFlush(false);
		ResultScanner scanner;
		Scan scan = new Scan();
//		scan.setFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("status"), CompareOp.EQUAL, Bytes.toBytes(Const.TASKID.get("HtmlFromHbaseToMysql"))));
		scan.setCaching(15000);
		scanner = ht.getScanner(scan);		
		
		for (Result result : scanner){
			insertToHbase(ht,result);
			System.out.println(Bytes.toInt(result.getRow())+"----"+(num++));
		}
		scanner.close();
		ht.flushCommits();
		ht.close();
		
	}
	
	public void insert(HTable ht){
	Put put = new Put(Bytes.toBytes(331403));
		
//		put.add(Bytes.toBytes("info"), Bytes.toBytes("url"), Bytes.toBytes("test1"));
//		put.add(Bytes.toBytes("info"), Bytes.toBytes("html"), Bytes.toBytes("test1_html"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("status"), Bytes.toBytes("3"));
		try {
			ht.put(put);
			ht.flushCommits();
			ht.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException{
//		SetAllNotToMysql se = new SetAllNotToMysql();
//		se.runTask();
		
 
		HbaseTest htest = new HbaseTest();
//		
		htest.conf = new Configuration();
////		conf.setLong(HConstants.HBASE_REGIONSERVER_LEASE_PERIOD_KEY, 120000);
		htest.conf.addResource(new Path(Const.HBASE_CONFIG_PATH_Local));
		htest.conf = HBaseConfiguration.create(htest.conf);
//		
		htest.ht = new HTable(htest.conf,Bytes.toBytes("EventTitle"));
////		htest.ht.setAutoFlush(false);
////		htest.insert(htest.ht);
////		
////		
////		
////		
////		
////		
		int num = 0;
//		
		ResultScanner scanner;
		Scan scan = new Scan();
		
		scan.setMaxVersions();
		
		
		scan.setFilter(new SingleColumnValueFilter(Bytes.toBytes("id"), Bytes.toBytes("status"), CompareOp.EQUAL, Bytes.toBytes("2")));
		scan.setCaching(10);
		
		
		scanner = htest.ht.getScanner(scan);		
		
		
		
		
		for (Result result : scanner){
		
			
			
			
			if(num >10000)
				break;
			System.out.println(num+"--ddd"+Bytes.toString(result.getValue("info".getBytes(), "crawltime".getBytes())));
			num++;
		}
		scanner.close();
//		
		
		
		
	}
}
