package news.crwaler.articleCrawlerAndExtractor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.recognition.NatureRecognition;

import db.data.Word;





/**
* @PackageName:news.extractor
* @ClassName: ChineseSplit
* @author: mblank
* @date: 2012-3-6 下午8:55:38
* @Description: split chinese words
* @Marks: using ikanaylzer.jar
* @chages: add a new pos tool (ansj_seg.jar https://github.com/ansjsun/ansj_seg),this tool can give out word pos.
*/
public class ChineseSplit {
	
	
	
	private static boolean checkNature(String str){
		boolean result = true;
		Map<String,Boolean> check = new HashMap<String,Boolean>();
//		check.put("m", true);
//		check.put("q", true);
//		check.put("s", true);
//		check.put("t", true);
		if(!check.containsKey(str))
			result = false;
		if(str.indexOf("n") == 0)
			result = true;
//		if(str.indexOf("a") >= 0)
//			result = true;
		return result;
	}
	
	
	/**
     * word frequency
     * @param text 
     * @param top   the top words
     * @return 
     * @Description:get the top frequency words from input text
     */
    public static List<String> findMaxOfenWordWithIK(String text,int top){
        Map<String,Integer> words=new HashMap<String,Integer>();
        IKSegmentation seg = new IKSegmentation(new StringReader(text) , true);
        try {
            Lexeme l = null;
            while( (l = seg.next()) != null){
                if(words.containsKey(l.getLexemeText()))
                    words.put(l.getLexemeText(), words.get(l.getLexemeText())+1);
                else
                    words.put(l.getLexemeText(), 1);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        int max=0;
        String maxKey=null;
        List<String> ofenwords=new ArrayList<String>();
        for(int i=0;i<top&&i<words.size();i++){
            for(String key:words.keySet()){
                if(words.get(key)>max){
                    max=words.get(key);
                    maxKey=key;
                }
            }
            max=0;
            words.put(maxKey, -1);
            ofenwords.add(maxKey);
        }
        return ofenwords;
    }
    
    
    public static List<String> findMaxOfenWord(String text,int top){
        Map<String,Integer> words=new HashMap<String,Integer>();
        List<Term> terms = ToAnalysis.paser(text);
    	new NatureRecognition(terms).recogntion();
		for(Term term : terms){
			String nature = term.getNatrue().natureStr;
			if(!checkNature(nature))
				continue;
            if(words.containsKey(term.getName()))
                words.put(term.getName(), words.get(term.getName())+1);
            else
                words.put(term.getName(), 1);
        }
        int max=0;
        String maxKey=null;
        
        List<String> ofenwords=new ArrayList<String>();
        for(int i=0;i<top&&i<words.size();i++){
            for(String key:words.keySet()){
                if(words.get(key)>max){
                    max=words.get(key);
                    maxKey=key;
                }
            }
            max=0;
//            System.out.println(maxKey + " " + words.get(maxKey));
            words.put(maxKey, -1);
            ofenwords.add(maxKey);
            
        }
        return ofenwords;
    }
    
    /**
     * @param text
     * @param top
     * @return
     * @Description:only leave the noun words.
     */
    public static List<String> findMaxOfenWordOnlyNN(String text,int top){
        Map<String,Integer> words=new HashMap<String,Integer>();
        List<Term> terms = ToAnalysis.paser(text);
    	new NatureRecognition(terms).recogntion();        
        for(Term term : terms){
           if(term.getNatrue().natureStr.indexOf("n") != 0 || term.getNatrue().natureStr.length() <= 1 || term.getName().length() <= 1){
        	   continue;
           }
           if(words.containsKey(term.getName()))
                words.put(term.getName(), words.get(term.getName())+1);
           else
                words.put(term.getName(), 1);
        }
        int max=0;
        String maxKey=null;
        List<String> ofenwords=new ArrayList<String>();
        for(int i=0;i<top&&i<words.size();i++){
            for(String key:words.keySet()){
                if(words.get(key)>max){
                    max=words.get(key);
                    maxKey=key;
                }
            }
            max=0;
            words.put(maxKey, -1);
            ofenwords.add(maxKey);
        }
        return ofenwords;
    }
	
	
    /**
     * @param text
     * @return: the list of split words
     * @Description:split the string
     */
    public static List<String> SplitStrWithoutPOS(String text){
    	List<String> result = new ArrayList<String>();
    	IKSegmentation seg = new IKSegmentation(new StringReader(text) , true);
        try {
        	 Lexeme l = null;
			while( (l = seg.next()) != null){
				result.add(l.getLexemeText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return result;    	
    }
    
    /**
     * @param text
     * @return
     * @Description:split chinese string using ansj_seg 
     * https://github.com/ansjsun/ansj_seg/wiki
     */
    public static List<String> SplitStr(String text){
    	List<String> result = new ArrayList<String>();
    	List<Term> terms = ToAnalysis.paser(text);
    	new NatureRecognition(terms).recogntion();
		for(Term term : terms){
			String nature = term.getNatrue().natureStr;
			if(!checkNature(nature))
				continue;
			result.add(term.getName());
		}
    	return result;
    }
    
    /**
     * @param text
     * @return
     * @Description:split chinese string using ansj_seg with pos
     * https://github.com/ansjsun/ansj_seg/wiki
     */
    public static List<Word> SplitStrWithPOS(String text){
    	List<Word> result = new ArrayList<Word>();
    	List<Term> terms = ToAnalysis.paser(text);
    	new NatureRecognition(terms).recogntion() ;
    	for(Term tm : terms){
    		String nature = tm.getNatrue().natureStr;
			if(!checkNature(nature))
				continue;
    		Word wp = new Word();
    		wp.setName( tm.getName());
    		wp.setNature( nature);
    		result.add(wp);
    	}
    	return result;
    }
    
    /**
     * @param text
     * @return
     * @Description:split chinese string using ansj_seg with pos
     * https://github.com/ansjsun/ansj_seg/wiki
     */
    public static List<Word> SplitStrOnlyPerson(String text){
    	List<Word> result = new ArrayList<Word>();
    	List<Term> terms = ToAnalysis.paser(text);
    	Set<String> hs = new HashSet<String>();
    	new NatureRecognition(terms).recogntion() ;
    	for(Term tm : terms){
    		String nature = tm.getNatrue().natureStr;
//			if(!nature.equals("nr"))
    		if(nature.indexOf("n") != 0 || tm.getName().length() <= 1 || tm.getName().indexOf("网") == 2)
				continue;
    		Word wp = new Word();
    		wp.setName( tm.getName());
    		wp.setNature( nature);
    		if(!hs.contains(wp.getName())){
    			result.add(wp);
    			hs.add(wp.getName());
    		}    		
    	}
    	return result;
    }
    
    
    
}

