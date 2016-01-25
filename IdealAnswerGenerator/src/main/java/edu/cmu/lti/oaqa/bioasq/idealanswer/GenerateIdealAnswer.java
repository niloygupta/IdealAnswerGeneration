package edu.cmu.lti.oaqa.bioasq.idealanswer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class GenerateIdealAnswer {

	Map<String,List<String>> synonyms;


	public String generateIdealAnswer(String query, List<String> snippets){
		
		
		synonyms = getSynonyms(query,snippets);
		
		/*
		 * Select the relevant snippets for the ideal answer using MMR
		 * "￼The Use of MMR, Diversity-Based Reranking for Reordering Documents and Producing Summaries"
		 */
		MaximanMarginalRelevance mmr = new MaximanMarginalRelevance();
		List<String> mmrSnippets = mmr.getMMRSnippets(query,snippets,synonyms);
		
		SentenceCompression sentenceCompression = new SentenceCompression();
		
		/*
		 * Compress each sentence using LSTM
		 * "Sentence Compression by Deletion with LSTMs"
		 * 
		 */
		StringBuffer idealAnswer = new StringBuffer("");
		for(String snippet:mmrSnippets)
			idealAnswer.append(sentenceCompression.compressSentence(snippet)).append(" . ");
		
		return idealAnswer.toString();

	}

	
	private Map<String, List<String>> getSynonyms(String query,List<String> snippets) {
		Map<String,List<String>> synonyms = new HashMap<String,List<String>>();

		genSynonym(query,synonyms);

		for(String snippet:snippets)
			genSynonym(snippet,synonyms);

		return synonyms;

	}

	private void genSynonym(String text, Map<String, List<String>> synonyms) {

		text = text.replaceAll(","," ,").replaceAll("\\. "," ").replaceAll("\n", "").replaceAll("\\?", "").toLowerCase().trim();

		String[] tokens = text.split(" ");

		for(String token:tokens)
		{
			token = token.trim();
			if(StopWordsSingleton.getInstance().getStopWords().contains(token) || synonyms.containsKey(token))
				continue;
			//List<String> synonymList = new ArrayList<String>();

			List<String> synonymList = UMLSService.getInstance().getSynonyms(token);
			synonyms.put(token,synonymList);
		}

	}

	
	

	

}
