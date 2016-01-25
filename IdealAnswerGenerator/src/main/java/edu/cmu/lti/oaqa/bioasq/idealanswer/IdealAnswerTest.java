package edu.cmu.lti.oaqa.bioasq.idealanswer;



import java.util.ArrayList;
import java.util.List;



public class IdealAnswerTest {


	public static void main (String args[]) {
		GenerateIdealAnswer generateIdealAnswer = new GenerateIdealAnswer();
		String query = "What is the role of RhoA in bladder cancer?";

		String s1 = "Alterations in RhoA, RhoB, RhoC, Rac1 and Cdc42 expression play a significant role in the genesis and progression of UCC of the urinary bladder.";
				String s2 = "Published reports suggest that elevated RhoA/Rho-kinase signaling plays a role in the development of benign prostatic hyperplasia, erectile dysfunction, kidney failure, ejaculation disorders, prostate and bladder cancer initiation, and eventual metastasis.";
		String s3 = "The suppressive effect of Rho kinase inhibitor, Y-27632, on oncogenic Ras/RhoA induced invasion/migration of human bladder cancer TSGH cells";

		List<String> snippets = new ArrayList<String>();
		snippets.add(s1);
		snippets.add(s2);
		snippets.add(s3);

		System.out.println(generateIdealAnswer.generateIdealAnswer(query, snippets));

	}

}
