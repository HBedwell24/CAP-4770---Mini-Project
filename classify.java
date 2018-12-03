// command line test driver for kNN and Naive Bayes

public class classify {
	
	public static void main(String[] args) throws Exception { // main method
		
		// check input string for arguments and parse parameters
		if(args[0].equals("knn") && args[1] != null && args[2] != null) {
			// check for correct arguments (k and path)
			if((args[1]).matches(".*\\d+.*")) {
				String charNum = args[1];
				int k = Integer.parseInt(charNum);
				String path = args[2];
				kNN knn = new kNN(k, path);
				knn.kNNaccuracy();
			}
			else {
				System.out.println("The k value provided as a parameter is not an integer!");
			}
		}	
		else if (args[0].equals("NB") && args[1] != null) {
			// check for correct argument (path)
			String path = args[1];
			NaiveBayes naivebayes = new NaiveBayes(path);	
			naivebayes.train();
			naivebayes.test();
		}
		else {
			System.out.println("Incorrect program input provided! Please re-run the program and follow the format: "
					+ "'java classify knn' or 'java classify NB', including parameters for 'k' and/or 'relative file path' as necessary!");
		}
	}
}
