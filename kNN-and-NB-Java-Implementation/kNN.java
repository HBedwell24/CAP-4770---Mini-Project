/* Author: Junshuai Feng
   Repository Name: EmailSpamChecker
   URL: https://github.com/JunshuaiFeng/EmailSpamChecker/blob/master/KNN.java 
*/

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class kNN {

  public static ArrayList<String> train = new ArrayList<String>();
  public static ArrayList<String> test = new ArrayList<String>();
  public static Map<String, Double> words = new HashMap<String, Double>();
  public static List<List<Double>> listOfMapsTest = new ArrayList<List<Double>>();
  public static List<List<Double>> listOfMapsTrain = new ArrayList<List<Double>>();
  public static ArrayList<Double> finalList = new ArrayList<Double>();
  public static ArrayList<Integer> accuracy = new ArrayList<Integer>();

  // Count number of spam emails
  public static int spamCountTrain = 0;
  public static int spamCountTest = 0;
  public static int emailCountTest = 0;
  public static int emailCountTrain = 0;
  public static int testCount = 0;
  public static int trainCount = 0;
  String file_path;
  int k_value;
  
  // kNN constructor
  public kNN (int k, String path) {
	  file_path = path;
	  k_value = k;
  }
  
  public void kNNaccuracy() throws IOException {

	  // Read in training and testing data (in the form of strings)
	  train = read(file_path + "/train.zip");
	  test = read(file_path + "/test.zip");
    
	  emailCountTrain = trainCount - spamCountTrain;
	  emailCountTest = testCount - spamCountTest;

	  // Create the HashMap WORDS from the training data, for the purpose of storing 
	  for (int i = 0; i < train.size(); i++) { // all unique words found and their frequency
		  String str = train.get(i);
		  String[] strings = str.split(" ");
		  count(strings);
	  }

	  // Filter words that occur 100 or less times within the HashMap
	  for (Iterator<Map.Entry<String, Double>> it = words.entrySet().iterator(); it.hasNext();) {
		  Map.Entry<String, Double> entry = it.next(); // Get next entry from words
		  if (entry.getValue() <= 100) {
			  it.remove(); // Remove values through iterator
		  }
	  }

	  // Create a list of doubles for training data
	  for (int i = 0; i < train.size(); i++) {
		  String str = train.get(i);
      
		  // Split the document into an array of words
		  String[] strings = str.split(" "); 
      
		  // Add a list which contains word count for each document
		  listOfMapsTrain.add(countOccurence(strings));
	  }

	  // Create a list of doubles for testing data
	  for (int i = 0; i < test.size(); i++) {
		  String str = test.get(i);
      
		  // Split the document into an array of words
		  String[] strings = str.split(" "); 
      
		  // Add a list which contains word counts for each document
		  listOfMapsTest.add(countOccurence(strings));
	  }
    
	  // Get cosine similarity and accuracy for each K value
	  cosineSimilarity(k_value);
    
	  // Print result
	  System.out.print("When k = " + k_value + ", the accuracy of kNN relative to the test data is ");
	  getAccuracy(file_path);
  }
  
  public static ArrayList<String> read(String path) throws IOException {

	  ArrayList<String> temp = new ArrayList<String>();
	  final ZipFile zip = new ZipFile(path);

	  final Enumeration<? extends ZipEntry> entries = zip.entries();
	  while (entries.hasMoreElements()) {
		  final ZipEntry entry = entries.nextElement();
		  InputStream stream = zip.getInputStream(entry);
		  BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

		  // If it is training data and if the name contains spam
		  if (path.contains("train") && entry.getName().contains("spm")) {
			  spamCountTrain++; // Increment spam count for training data
		  }
	
		  // If it is testing data and if the file name contains spam
		  if (path.contains("test") && entry.getName().contains("spm")) {
			  spamCountTest++; // Increment spam count for testing data
		  }
	
		  // Get total number of testing emails
		  if (path.contains("test")) {
			  testCount++;
		  }
	      
		  // Get total number of training emails
		  if (path.contains("train")) {
			  trainCount++;
		  }
	
		  // Convert each email to string
		  String s = "";
		  String sCurrentLine;
		  while ((sCurrentLine = in.readLine()) != null) {
			  String replaceSpecial = sCurrentLine.replaceAll("[^A-Za-z0-9 ]", "");
			  String replaceWhitespace = replaceSpecial.replaceAll("\\s+", " ");
			  s += replaceWhitespace;
		  }
		  temp.add(s); // Add each string to arrayList
	  }
	  return temp; // return every email in the form of a string
  }

  public static void getAccuracy(String path) throws IOException {

	  int correct = 0; // Count total number of correct predictions
	  int counter = 0; // Count each test file index
	  final ZipFile zip = new ZipFile(path + "/test.zip");
	  final Enumeration<? extends ZipEntry> entries = zip.entries();
    
	  while (entries.hasMoreElements()) {
		  final ZipEntry entry = entries.nextElement();

		  // Check if it's a regular file
		  if (accuracy.get(counter) == 0 && !entry.getName().startsWith("sp")) {
			  correct++;
		  }
		 
		  // Check if it's a spam file
		  if (accuracy.get(counter) == 1 && entry.getName().startsWith("sp")) {
			  correct++;
		  }
		  counter++;
	  }
	  zip.close();
	  DecimalFormat df = new DecimalFormat();
	  df.setMaximumFractionDigits(2);
	  double acc = correct / (double) (accuracy.size()); // Calculate accuracy
	  System.out.println(df.format(acc * 100) + "%."); // Print result
	  accuracy = new ArrayList<Integer>(); // Reset array of true and false
  }

  public static void cosineSimilarity(int k) {

	  // Cycle through each test record and compare it individually with each training record
	  for (int i = 0; i < listOfMapsTest.size(); i++) {
		  for (int j = 0; j < listOfMapsTrain.size(); j++) {

			  // Obtain each test term vector
			  List<Double> x = listOfMapsTest.get(i);

			  // Obtain each train term vector
			  List<Double> y = listOfMapsTrain.get(j);

			  // Calculate the cosine similarity
			  double cosSim = (dotProd(x, y) / getLength(x, y));
			  finalList.add(cosSim); // Adds each cosine value to list
		  }
		 
		  // Get K nearest neighbors, by finding greatest values in finalList
		  getKNN(k);
		  finalList = new ArrayList<Double>(); // Reset finalList
	  }
  }

  public static void getKNN(int k) {

	  // Create clone of final list
	  List<Double> clone = new ArrayList<Double>(finalList);
	  // Create a list to hold the indices of the nearest neighbors
	  List<Integer> indexes = new ArrayList<Integer>();

	  // Assemble list of indexes for closest neighbors
	  for (int i = 0; i < k; i++) {
		  double temp = Collections.max(clone); // Get max from clone of finalList
		  int index = finalList.indexOf(temp); // Get index of max from finalList
		  indexes.add(index); // Add to list of indexes
		  clone.set(index, 0.0); // Change top value to 0 to find the next neighbor
	  }

	  int spamCount = 0; // Count number of spam
	  for (int i = 0; i < indexes.size(); i++) {

		  int x = indexes.get(i); // Get index
		  if (x <= spamCountTrain) { 
			  // Not Spam
		  } 
		  else { 
			  // Spam
			  spamCount++;
		  }
	  }
	  int emailCount = k - spamCount; // Total number of non-spam

	  // If spam, insert a 1 for that test record; If email, insert a 0
	  if (spamCount > emailCount)
		  accuracy.add(1);
	  else
		  accuracy.add(0);
  }

  public static double dotProd(List<Double> a, List<Double> b) {
	  double sum = 0; // Total dotProd
	  for (int i = 0; i < a.size(); i++) {
		  // Multiply each element in Vector by one another
		  sum += a.get(i) * b.get(i);
	  }
	  return sum;
  }

  public static double getLength(List<Double> a, List<Double> b) {

	  // Length from testing set
	  double sum1 = 0.0;
	  for (int i = 0; i < a.size(); i++) {
		  sum1 += a.get(i) * a.get(i);
	  }
	  sum1 = Math.pow(sum1, .5); // Sum Raised to .5 power

	  // Length from training set
	  double sum2 = 0.0;
	  for (int i = 0; i < b.size(); i++) {
		  sum2 += b.get(i) * b.get(i);
	  }
	  sum2 = Math.pow(sum2, .5); // Sum Raised to .5 power
	  double sum3 = sum1 * sum2; // Multiply sums

	  return sum3; // returns denominator
  }

  public static Map<String, Double> makeZero(Map<String, Double> wordClone) {

	  for (Map.Entry<String, Double> entry : wordClone.entrySet()) {
		  wordClone.put(entry.getKey(), 0.0); // Changes double to 0
	  }
	  return wordClone;
  }

  private static void count(String[] arr) {

	  for (int i = 0; i < arr.length; i++) {
		  // Filters out the special characters
		  arr[i] = arr[i].replaceAll("[^a-zA-Z0-9]+", "");

		  if (!words.containsKey(arr[i])) {
			  words.put(arr[i], 1.0); // Adds new word to list
		  } 
		  else if (words.containsKey(arr[i])) {
			  // Updates count of already added word
			  words.put(arr[i], words.get(arr[i]) + 1);
		  }
	  }
  }

  private static ArrayList<Double> countOccurence(String[] arr) {
	  // Clones hashmap of all words
	  Map<String, Double> wordClone = new HashMap<String, Double>(words);
	  wordClone = makeZero(wordClone); // Makes all double values zero

	  for (int i = 0; i < arr.length; i++) {

		  if (words.containsKey(arr[i])) {
			  // Counts up word occurrences per document
			  wordClone.put(arr[i], wordClone.get(arr[i]) + 1);
		  }
	  }
	  // Returns list of only doubles. Doubles represent word occurrence
	  ArrayList<Double> values = new ArrayList<Double>(wordClone.values());
	  return values;
  }
}