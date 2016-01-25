package edu.cmu.lti.oaqa.bioasq.idealanswer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class LSTMNetworkSingleton {

	MultiLayerNetwork savedNetwork = null;
	

	private static LSTMNetworkSingleton lstmNetworkSingleton;

	public static LSTMNetworkSingleton getInstance()
	{
		if(lstmNetworkSingleton == null)
			lstmNetworkSingleton = new LSTMNetworkSingleton();	

		return lstmNetworkSingleton;
	}
	
	public MultiLayerNetwork getMultiLayerNetwork()
	{
		return savedNetwork;
	}

	private LSTMNetworkSingleton()
	{
		MultiLayerConfiguration confFromJson = null;
		INDArray newParams = Nd4j.zeros(new int[]{1,644002});
		try {
			confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(IdealAnswerConstants.LSTMNetworkConfig)));
			File file = new File(IdealAnswerConstants.LSTMParametersConfig);
			Scanner scanner = new Scanner(file);

			int i=0;
			while (scanner.hasNext()) {
				newParams.putScalar(new int[]{0,i++}, Double.parseDouble(scanner.next()));
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		savedNetwork = new MultiLayerNetwork(confFromJson);
		savedNetwork.init();
		savedNetwork.setParameters(newParams);


	}
}
