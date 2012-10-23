package news.crwaler.articleCrawlerAndExtractor;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import util.Const;


	public class THReducer extends TableReducer<ImmutableBytesWritable, Text, ImmutableBytesWritable> {
	 
		public void reduce(ImmutableBytesWritable key, Iterable<Text> html, Context context)
				throws IOException, InterruptedException {
			Const.loadTaskid();
			byte[] temp_by = downloadHtml.toBytes(html.iterator().next().toString());
			if(temp_by!=null){
				
			String temp = Bytes.toString(temp_by);   
			String title = "";
//			String plaintext = "";			
			String publishtime = "";
			String mainparagraph = "";
			String mainparagraphwords = "";
			String titlewords = "";
			String summarywords = "";
			String img = "";
			try{
				ArticleExtractor aex = new ArticleExtractor(temp);
				title = aex.getTitle();
//				plaintext = aex.getPlainText();			
				publishtime = aex.getPublishTime();
				mainparagraph = aex.getFormatMainParagraph();
				mainparagraphwords = aex.mainParagraphWordsWithFrequency();
				titlewords = aex.titleWordsWithFrequency();
				summarywords = aex.SummaryWordsWithFrequency();
				img = aex.getImg();
			}catch(Exception e){
				e.printStackTrace();
			}			
			Put put = new Put(key.get());
			put.add(Bytes.toBytes("info"), Bytes.toBytes("html"), Bytes.toBytes(temp));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("status"), Bytes.toBytes("3"));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(title));
//			put.add(Bytes.toBytes("info"), Bytes.toBytes("plaintext"), Bytes.toBytes(plaintext));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("publishtime"), Bytes.toBytes(publishtime));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("mainparagraph"), Bytes.toBytes(mainparagraph));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("mainparagraphwords"), Bytes.toBytes(mainparagraphwords));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("titlewords"), Bytes.toBytes(titlewords));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("summarywords"), Bytes.toBytes(summarywords));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("img"), Bytes.toBytes(img));
			if(mainparagraph.length() > 0){
				context.write(key, put);
			}			
			}
		}
	
	}
