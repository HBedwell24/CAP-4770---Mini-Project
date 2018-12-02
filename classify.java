public class classify {
	
	public static void main(String[] args) throws Exception {
		
		// check input string for prefix "java classify knn" and parse parameters
		if(args[0] == "knn" && args[1] != null && args[2] != null) {
			
			String charNum = args[1];
			int k = Integer.parseInt(charNum);
			String path = args[2];
			kNN knn = new kNN(k);
			
			knn.loadtrainData(path + "/train.zip");
			knn.loadtestData(path + "/test.zip");
		    knn.cosineSimilarity();
		}
	
		// check input string for prefix "java classify NB" and parse parameters
		else if (args[0] == "NB" && args[1] != null) {
			
			String path = args[1];
			NaiveBayes naivebayes = new NaiveBayes();	
			int spamTotal = naivebayes.train(path + "/train.zip");
			int spamCount = naivebayes.filter(path + "/test.zip");
			naivebayes.accuracy(spamCount, spamTotal);
		}
	}
}