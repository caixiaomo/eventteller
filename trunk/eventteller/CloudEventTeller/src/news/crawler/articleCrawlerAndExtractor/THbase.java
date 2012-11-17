package news.crawler.articleCrawlerAndExtractor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class THbase {
    public static void main(String [] args) throws Exception{
        int mr;
        mr = ToolRunner.run(new Configuration(),new THDriver(),args);
        System.exit(mr);
    }
}