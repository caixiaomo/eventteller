package news.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKSimilarity;

/**
* @PackageName:news.index
* @ClassName: IndexUtil
* @author: mblank
* @date: 2012-3-30 下午12:15:20
* @Description: just a base class of index
* @Marks: 
*/
public class IndexUtil {
	
	public synchronized static IndexWriter createIndex(String indexDir)throws Exception{
		Analyzer analyzer = new IKAnalyzer(); 
		///use in linux
		NIOFSDirectory dir = new NIOFSDirectory(new File(indexDir));
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35,analyzer);	
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer=new IndexWriter(dir,iwc);
		return writer;
	}

    public synchronized static IndexSearcher SearchIndex(String indexDir)throws Exception{
		SimpleFSDirectory dir=new SimpleFSDirectory(new File(indexDir));
		IndexReader ir = IndexReader.open(dir);
		IndexSearcher is = new IndexSearcher(ir);
		is.setSimilarity(new IKSimilarity());
		return is;
	}
    
	public synchronized static void createBlankIndex(String indexDir)throws Exception{
		Analyzer analyzer = new IKAnalyzer(); 
		///use in linux
		NIOFSDirectory dir = new NIOFSDirectory(new File(indexDir));
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35,analyzer);	
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer=new IndexWriter(dir,iwc);
		writer.commit();
		writer.close();
	}
	
	public synchronized static void IndexSearcherClose(IndexSearcher is){
		IndexReader ir = is.getIndexReader();
		try {
			ir.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}	
