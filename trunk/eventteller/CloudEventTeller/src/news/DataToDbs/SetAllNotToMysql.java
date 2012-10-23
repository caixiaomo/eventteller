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
import org.apache.hadoop.hbase.util.Bytes;


import util.Const;


/**
* @PackageName:news.DataToDbs
* @ClassName: SetAllNotToMysql
* @author: mblank
* @date: 2012-3-30 上午11:36:10
* @Description: not used!
* @Marks: TODO
*/
public class SetAllNotToMysql {


	public HTable  ht ;

	public Configuration conf ;
	
	
	/**
	 * @param tn
	 * @Description:insert id+url to hbase
	 */
	public void insertToHbase(HTable ht,Result result){
		Const.loadTaskid();
		Put put = new Put(result.getRow());
		put.add(Bytes.toBytes("info"), Bytes.toBytes("status"), Bytes.toBytes("3"));
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
		ht = new HTable(conf,Bytes.toBytes("EventTitle"));
		ht.setAutoFlush(false);
		ResultScanner scanner;
		Scan scan = new Scan();
//		scan.setFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("status"), CompareOp.EQUAL, Bytes.toBytes("4")));
		scan.setCaching(200);
		scanner = ht.getScanner(scan);		
		
		for (Result result : scanner){
			if(num>8194){
				insertToHbase(ht,result);
			}

			System.out.println(Bytes.toInt(result.getRow())+"----"+(num++));
			if(num>100000)
				break;
			num++;
		}
		scanner.close();
		ht.flushCommits();
		ht.close();
		
	}
	
	
	
	public static void main(String[] args) throws IOException{
		SetAllNotToMysql se = new SetAllNotToMysql();
		
		while(true){
			
			se.runTask();
		}

	}
}
