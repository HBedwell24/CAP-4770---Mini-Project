// test driver for kNN and Naive Bayes algorithms

import java.util.Scanner;

public class classify {
	
	public static void main(String[] args) throws Exception { // main method
		
		// instantiate scanner object for input
		Scanner scanner = new Scanner(System.in);
		
		// prompt user to enter an algorithm (kNN or NaiveBayes classifier)
		System.out.print("Please enter an algorithm for spam classifcation [NB/knn]: ");
		String algorithm = scanner.nextLine();
		
		// if algorithm selected is kNN classifier
		if(algorithm.equals("knn")) {
			// prompt user for k value
			System.out.print("Please enter an integer for k: ");
			String kVal = scanner.nextLine();
			
			// check k value for numerical input
			while(isNumeric(kVal) != true) {
				System.out.println("'k' value is not of type int. Please try again!");
				System.out.print("Please enter an integer for k: ");
				kVal = scanner.nextLine();
			}
			// if k value is numerical, convert it to an integer
			int k = Integer.parseInt(kVal);
			// prompt user for relative file path of dataset
			System.out.print("Please enter the relative file path of the dataset: ");
			String path = scanner.nextLine();
			System.out.print("\n");
			// instantiate an object of the kNN class with parameters k and path
			kNN knn = new kNN(k, path);
			knn.kNNaccuracy();
		}
		
		// if algorithm selected is Naive Bayes classifier
		else if(algorithm.equals("NB")) {
			// prompt user for relative file path of dataset
			System.out.print("Please enter the relative file path of the dataset: ");
			String path = scanner.nextLine();
			System.out.print("\n");
			
			// instantiate an object of the NaiveBayes class with parameters k and path
			NaiveBayes naivebayes = new NaiveBayes(path);	
			naivebayes.train();
			naivebayes.test();
		}
		
		// if algorithm selected does not match the selection
		else {
			// prompt user to re-run the program and provide correct input
			System.out.println("Incorrect program input provided! Please re-run the program and follow the format: "
					+ "'knn' or 'NB', followed by parameters for 'k' and/or 'relative file path' as necessary!");
		}
		scanner.close();
	}
	
	// check if input string is numerical
	public static boolean isNumeric(String s) {  
	    return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}  
}
