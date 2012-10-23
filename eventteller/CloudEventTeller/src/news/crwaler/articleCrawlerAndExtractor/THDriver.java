package news.crwaler.articleCrawlerAndExtractor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;

import util.Const;

public class THDriver extends Configured implements Tool{

    public int run(String[] arg0) throws Exception {
    	
    	Const.loadTaskid();
		Configuration conf = new Configuration();
		conf.setLong(HConstants.HBASE_REGIONSERVER_LEASE_PERIOD_KEY, 1200000);
		conf.addResource(new Path("/usr/local/hbase/conf/hbase-site.xml"));//can be set 		
		conf = HBaseConfiguration.create(conf);  	
        Job job = new Job(conf, "Hbase_insert");
        job.setJarByClass(THDriver.class);
        Scan scan = new Scan();
        scan.setFilter(new SingleColumnValueFilter(Bytes.toBytes("info"),  
                Bytes.toBytes("status"),  
                CompareOp.EQUAL,Bytes.toBytes("2")  
                )  );
        TableMapReduceUtil.initTableMapperJob("EventTitle", scan, THMapper.class, ImmutableBytesWritable.class,
                Text.class, job);
        job.setNumReduceTasks(8);
        TableMapReduceUtil.initTableReducerJob("EventTitle", THReducer.class, job);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
        return 0;
    }
    
    

}