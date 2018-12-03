public class classify {
	
	public static void main(String[] args) throws Exception { // main method
		
		// check input string for arguments and parse parameters
		if(args[0].equals("knn") && args[1] != null && args[2] != null) {
			
			String charNum = args[1];
			int k = Integer.parseInt(charNum);
			String path = args[2];
			kNN knn = new kNN(k, path);
			knn.kNNaccuracy();
		}
	
		else if (args[0].equals("NB") && args[1] != null) {
			
			String path = args[1];
			NaiveBayes naivebayes = new NaiveBayes(path);	
			naivebayes.train();
			naivebayes.test();
		}
	}
}
