public class classify {
	
	public static void main(String[] args) throws Exception { // main method
		
		// check input string for arguments and parse parameters
		if(args[0] == "knn" && args[1] != null && args[2] != null) {
			
			String charNum = args[1];
			int k = Integer.parseInt(charNum);
			String path = args[2];
			kNN knn = new kNN(k, path);
			knn.kNNaccuracy();
		}
	
		else if (args[0] == "NB" && args[1] != null) {
			
			String path = args[1];
			NaiveBayes naivebayes = new NaiveBayes();	
			int spamTotal = naivebayes.train(path + "/train.zip");
			int spamCount = naivebayes.filter(path + "/test.zip");
			naivebayes.accuracy(spamCount, spamTotal);
		}
	}
}
