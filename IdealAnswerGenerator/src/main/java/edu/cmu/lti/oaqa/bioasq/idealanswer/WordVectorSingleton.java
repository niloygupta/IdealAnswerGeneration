package edu.cmu.lti.oaqa.bioasq.idealanswer;

import java.io.File;
import java.io.FileNotFoundException;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

public class WordVectorSingleton {
	

	private static WordVectorSingleton wordVectorSingleton;
	WordVectors wordVec;
	
	
	public static WordVectorSingleton getInstance(){
		if(wordVectorSingleton==null)
			wordVectorSingleton = new WordVectorSingleton();
		
		return wordVectorSingleton;
	}
	
	public WordVectors getWordVectors(){
		return wordVec;
	}
	
	private WordVectorSingleton(){
		try {
			wordVec =  WordVectorSerializer.loadTxtVectors(new File(IdealAnswerConstants.wordVectorFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
