package edu.cmu.lti.oaqa.bioasq.idealanswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;



public class MaximanMarginalRelevance {


	final double lambda = 0.6;
	
	public List<String> getMMRSnippets(String query, List<String> snippets, Map<String, List<String>> synonyms) {

		List<String> docs = new ArrayList<String>();
		docs.addAll(snippets);
		docs.add(query);
		Map<String,Integer> vocabIndex = new HashMap<String,Integer>();
		Map<Integer,String> reverseVocabIndex = new HashMap<Integer,String>();
		INDArray tfIdf = getTFIDF(docs,vocabIndex,reverseVocabIndex,synonyms);
		INDArray simMatrix = getWordSimMatrix(vocabIndex,synonyms);

		/*Soft Cosine Similarity
		 * 
		 * 
		 * */

		INDArray docSimMatrix = Nd4j.eye(docs.size());

		for(int i=0;i<docs.size();i++)
		{
			INDArray d1 = tfIdf.getRow(i);
			double d1Sim = Math.sqrt(d1.mmul(simMatrix).mmul(d1.transpose()).getDouble(0, 0));
			for(int j=i+1;j<docs.size();j++)
			{
				INDArray d2 = tfIdf.getRow(j);
				double d2Sim = Math.sqrt(d2.mmul(simMatrix).mmul(d2.transpose()).getDouble(0, 0));
				double d1d2Sim = d1.mmul(simMatrix).mmul(d2.transpose()).getDouble(0, 0);
				double softCosineSim = d1d2Sim/(d1Sim*d2Sim);

				docSimMatrix.putScalar(new int[]{i,j}, softCosineSim);
				docSimMatrix.putScalar(new int[]{j,i}, softCosineSim);
			}
		}

		//MMR
		List<String> MMRSnippets = new ArrayList<String>();
		Set<Integer> S = new HashSet<Integer>();
		Set<Integer> R = new HashSet<Integer>();

		for(int i=0;i<docs.size()-1;i++)
			R.add(i);

		INDArray querySimVec = docSimMatrix.getRow(docs.size()-1).dup();

		INDArray relSnippetsSorted = Nd4j.sortWithIndices(querySimVec, 1, false)[0]; 
		int topRelSnippet = 0;
		int index = 0;
		while((topRelSnippet=(int)relSnippetsSorted.getDouble(index))==docs.size()-1)
			index++;
		S.add(topRelSnippet);
		R.remove(topRelSnippet);
		MMRSnippets.add(snippets.get(topRelSnippet));

		for(int i=0;i<docs.size()-1;i++)
		{
			if(R.isEmpty())
				break;

			int maxMMRSnippet = -1;
			double maxMMR = 0.0;
			for(int r:R)
			{
				double maxSim = -1.0;
				for(int s:S)
				{
					if(docSimMatrix.getDouble(r, s)>maxSim)
						maxSim = docSimMatrix.getDouble(r, s);

				}

				double mmr = lambda*docSimMatrix.getDouble(docs.size()-1,r) - (1-lambda)*maxSim;

				if(mmr>maxMMR)
				{
					maxMMRSnippet = r;
					maxMMR = mmr;
				}

			}

			if(maxMMR>0)
			{
				MMRSnippets.add(snippets.get(maxMMRSnippet));
				S.add(maxMMRSnippet);
				R.remove(maxMMRSnippet);
			}
			else
				break;

		}

		return MMRSnippets;
	}
	

	private INDArray getWordSimMatrix(Map<String,Integer> vocabIndex,Map<String, List<String>> synonyms)
	{
		Map<Integer,INDArray> wordVecMap = new HashMap<Integer,INDArray>();
		for(String word:vocabIndex.keySet())
		{
			INDArray wordVec = null;
			if(WordVectorSingleton.getInstance().getWordVectors().hasWord(word))
				wordVec = Nd4j.create(WordVectorSingleton.getInstance().getWordVectors().getWordVector(word));
			else
				wordVec = getMeanSynonyms(word,synonyms);
			
			wordVecMap.put(vocabIndex.get(word),wordVec);
		}
		
		INDArray simMatrix = Nd4j.eye(vocabIndex.keySet().size());
		
		for(int i=0;i<vocabIndex.keySet().size();i++)
		{
			for(int j=i+1;j<vocabIndex.keySet().size();j++)
			{
				double cosineSim = Transforms.cosineSim(wordVecMap.get(i), wordVecMap.get(j));
				if(Double.isNaN(cosineSim))
					cosineSim = 0;
				simMatrix.putScalar(new int[]{i,j}, cosineSim);
				simMatrix.putScalar(new int[]{j,i}, cosineSim);
			}
		}
		
		
		return simMatrix;
	}

	private INDArray getMeanSynonyms(String word,Map<String, List<String>> synonyms) {
		INDArray wordVec = Nd4j.zeros(WordVectorSingleton.getInstance().getWordVectors().getWordVector(word).length);
		
		int synonymCount = 0;
		for(String synonym:synonyms.get(word))
		{
			if(WordVectorSingleton.getInstance().getWordVectors().hasWord(synonym))
			{
				wordVec.add(Nd4j.create(WordVectorSingleton.getInstance().getWordVectors().getWordVector(synonym)));
				synonymCount++;
			}
		}
		
		if(synonymCount>0)
			wordVec = wordVec.divi(synonymCount);
		
		return wordVec;
	}


	private INDArray getTFIDF(List<String> docs,Map<String,Integer> vocabIndex, Map<Integer, String> reverseVocabIndex,Map<String, List<String>> synonyms) {
		
		
		INDArray tfIdf = Nd4j.zeros(docs.size(),synonyms.keySet().size());
		int wordIndex = 0;
		Map<Integer,Set<Integer>> wordIDF = new HashMap<Integer,Set<Integer>>();
		
		for(int docIndex = 0; docIndex<docs.size();docIndex++)
		{
			String doc = docs.get(docIndex).replaceAll(","," ,").replaceAll("\\. "," ").replaceAll("\n", "").replaceAll("\\?", "").toLowerCase().trim();	
			String[] tokens = doc.split(" ");
			for(String token:tokens)
			{
				token = token.trim();
				
				if(StopWordsSingleton.getInstance().getStopWords().contains(token))
					continue;
				if(!vocabIndex.containsKey(token))
				{
					vocabIndex.put(token,wordIndex++);
					reverseVocabIndex.put(wordIndex-1, token);
					wordIDF.put(wordIndex-1, new HashSet<Integer>());
					
				}		
				wordIDF.get(vocabIndex.get(token)).add(docIndex);
				tfIdf.putScalar(new int[]{docIndex,vocabIndex.get(token)}, tfIdf.getDouble(docIndex,vocabIndex.get(token)) + 1); //adding term frequency
			}
		}
		
		INDArray idf = Nd4j.zeros(1,synonyms.keySet().size());
		
		for(int word = 0;word<vocabIndex.keySet().size();word++)
			idf.putScalar(new int[]{0,word},Math.log(docs.size()/(wordIDF.get(word).size()+0.0)));
		
		
		tfIdf = tfIdf.mulRowVector(idf);
		
		
		return tfIdf;
	}


}
