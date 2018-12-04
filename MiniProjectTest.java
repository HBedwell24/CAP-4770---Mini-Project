import java.util.Scanner;

public class classify {
	
	public static void main(String[] args) throws Exception { // main method
		
		Scanner scanner = new Scanner(System.in);
		String algorithm = scanner.nextLine();
		
		System.out.print("Please enter an algorithm for spam classifcation [NB/knn]: ");
		algorithm = scanner.nextLine();
		
		if(algorithm.equals("knn")) {
			System.out.print("Please enter an integer for k: ");
			String kVal = scanner.nextLine();
			
			while(isNumeric(kVal) != true) {
				System.out.println("'k' value is not of type int. Please try again!");
				System.out.print("Please enter an integer for k: ");
				kVal = scanner.nextLine();
			}
			int k = Integer.parseInt(kVal);
			System.out.print("Please enter the relative file path of the dataset: ");
			String path = scanner.nextLine();
			System.out.print("\n");
			kNN knn = new kNN(k, path);
			knn.kNNaccuracy();
		}
		
		else if(algorithm.equals("NB")) {
			System.out.print("Please enter the relative file path of the dataset: ");
			String path = scanner.nextLine();
			System.out.print("\n");
			
			// check for correct argument (path)			
			NaiveBayes naivebayes = new NaiveBayes(path);	
			naivebayes.train();
			naivebayes.test();
		}
		
		else {
			System.out.println("Incorrect program input provided! Please re-run the program and follow the format: "
					+ "'knn' or 'NB', followed by parameters for 'k' and/or 'relative file path' as necessary!");
		}
		scanner.close();
	}
	
	public static boolean isNumeric(String s) {  
	    return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}  
}
