package news.crawler.articleCrawlerAndExtractor;

import java.io.IOException;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;



public class THMapper extends TableMapper<ImmutableBytesWritable, Text> {

    @Override
    public void map(ImmutableBytesWritable row, Result values, Context context) throws IOException {

        NavigableMap<byte[],byte[]> ngm = values.getFamilyMap("info".getBytes());
        byte[] by_url = ngm.get("url".getBytes());
        String str_url = Bytes.toString(by_url);
        try {
            context.write(row,new Text(str_url));
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}