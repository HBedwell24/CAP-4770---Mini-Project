import java.util.Scanner;

public class Test {
	
	public static void main(String[] args) throws Exception {
		
		Scanner scanner = new Scanner(System.in);
		System.out.print(">> ");
		String input = scanner.nextLine();
		
		// while input is invalid, prompt to re-enter input
		while ((!(input.startsWith("java classify knn"))) && (!(input.startsWith("java classify NB")))) {
			System.out.println("'" + input + "' is not recognized as an internal or external command, \n" +
					"operable program or batch file.");
			scanner = new Scanner(System.in);
			System.out.print(">> ");
			input = scanner.nextLine();
		}
		
		// check input string for prefix "java classify knn" and parse parameters
		if(input.startsWith("java classify knn")) {
			
			while (input.length() <= 17) {
				System.out.println("Arguments 'k' and 'path' were not provided. Please try again!");
				scanner = new Scanner(System.in);
				System.out.print(">> ");
				input = scanner.nextLine();
			}
			
			char charNum = input.charAt(18);
			int k = Character.getNumericValue(charNum);
			String path = input.substring(20);
			kNN knn = new kNN(k);
			
			knn.loadtrainData(path + "/train.zip");
			knn.loadtestData(path + "/test.zip");
		    knn.cosineSimilarity();
		}
	
		// check input string for prefix "java classify NB" and parse parameters
		else if (input.startsWith("java classify NB")) {
			
			while (input.length() <= 19) {
				System.out.println("Argument 'path' was not provided. Please try again!");
				scanner = new Scanner(System.in);
				System.out.print(">> ");
				input = scanner.nextLine();
			}
			
			String path = input.substring(17);
			NaiveBayes naivebayes = new NaiveBayes();	
			int spamTotal = naivebayes.train(path + "/train.zip");
			int spamCount = naivebayes.filter(path + "/test.zip");
			naivebayes.accuracy(spamCount, spamTotal);
		}
		scanner.close();
	}
}