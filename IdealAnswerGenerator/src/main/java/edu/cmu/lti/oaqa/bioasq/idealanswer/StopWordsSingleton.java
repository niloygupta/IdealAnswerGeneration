package edu.cmu.lti.oaqa.bioasq.idealanswer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopWordsSingleton {
	Set<String> stopWords = new HashSet<String>();

	private static StopWordsSingleton stopWordsSingleton;

	public static StopWordsSingleton getInstance()
	{
		if(stopWordsSingleton == null)
			stopWordsSingleton = new StopWordsSingleton();	

		return stopWordsSingleton;
	}
	
	public Set<String> getStopWords()
	{
		return stopWords;
	}

	private StopWordsSingleton()
	{
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(IdealAnswerConstants.stopWordFile));
			while ((sCurrentLine = br.readLine()) != null) {
				stopWords.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}


	}

}
