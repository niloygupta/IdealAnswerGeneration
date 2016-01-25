package edu.cmu.lti.oaqa.bioasq.idealanswer;

import java.util.HashSet;
import java.util.Set;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class SentenceCompression {

	private static int wordVectorLength = 200;
	public  String compressSentence(String str) {

		
		str = str.replaceAll(","," ,").replaceAll("\\. "," ").replaceAll("\n", "").replaceAll("\\?", "").toLowerCase().trim();

		String[] tokens = str.split(" ");
		int retainPrevWord = 1;

		String compressedSentence = "";
		Set<Integer> deletedWordIndex = new HashSet<Integer>();
		Set<Integer> retainWordIndex = new HashSet<Integer>();

		for(int wordIndex=0;wordIndex<tokens.length;wordIndex++)
		{
			if(!WordVectorSingleton.getInstance().getWordVectors().hasWord(tokens[wordIndex].toLowerCase()))
				retainWordIndex.add(wordIndex);
		}

		for(int k=0;k<2;k++)
		{
			int maxIndex = -1;
			double maxDelVal = 0.0;
			LSTMNetworkSingleton.getInstance().getMultiLayerNetwork().rnnClearPreviousState();

			for(int wordIndex=0;wordIndex<tokens.length;wordIndex++)
			{
				INDArray nextInput = Nd4j.zeros(1,wordVectorLength+1);

				String currentWord = tokens[wordIndex];

				double[] vecRep = WordVectorSingleton.getInstance().getWordVectors().getWordVector(currentWord.toLowerCase());
				for(int i=0;i<wordVectorLength;i++)
					nextInput.putScalar(new int[]{0,i}, vecRep[i]);

				if(wordIndex==0)
					retainPrevWord = 1; // 1--> retain word 0--> delete word

				nextInput.putScalar(new int[]{0,wordVectorLength}, retainPrevWord);

				INDArray output = LSTMNetworkSingleton.getInstance().getMultiLayerNetwork().rnnTimeStep(nextInput);

				Double removeCurrentWord = output.getDouble(0,0);
				Double retainCurrentWord = output.getDouble(0,1);

				if(retainCurrentWord>removeCurrentWord)
					retainPrevWord = 1;
				else
					retainPrevWord = 0;

				if(deletedWordIndex.contains(wordIndex))
					retainPrevWord = 0;

				if(!retainWordIndex.contains(wordIndex) && maxDelVal<removeCurrentWord)
				{
					maxDelVal = removeCurrentWord;
					maxIndex = wordIndex;
				}		
			}
			deletedWordIndex.add(maxIndex);
		}

		deletedWordIndex.removeAll(retainWordIndex);

		for(int wordIndex=0;wordIndex<tokens.length;wordIndex++)
		{
			String currentWord = tokens[wordIndex];
			if(currentWord.startsWith("[") || "here".equals(currentWord)||"we".equals(currentWord.toLowerCase()))
				continue;
			
			if(!deletedWordIndex.contains(wordIndex))
				compressedSentence+=currentWord+" ";
		}
		return compressedSentence.trim();

	
	}

}
