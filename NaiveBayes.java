/* Author: Kine Johnsrud
   Repository Name: naive-bayesian-spam-filter
   URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NaiveBayes {
	
	// relative file path of input data
	// NOTE: the file folder that contains the files MUST be in zip format
	String file_path;
	long time = System.currentTimeMillis();
	
	// actual/predicted totals of spam/ham emails in test set
	int actualSpamEmailTotal = 0;
	int actualHamEmailTotal = 0;
	int actualEmailTotal = 0;
	int predictedSpamEmailTotal = 0;
	int predictedHamEmailTotal = 0;
	
	// count of spam/ham words in emails
	int wordSpamCountInEmails = 0;
	int wordHamCountInEmails = 0;
	
	// probabilities for Naive Bayes classification
	float probEmailGivenHam = 0.0f;		// log P(email content|ham)
	float probEmailGivenSpam = 0.0f;	// log P(email content|spam)
	float probHamGivenEmail = 0.0f;		// log P(ham|email content)
	float probSpamGivenEmail = 0.0f;	// log P(spam|email content)
	float probHam;						// log P(ham)
	float probSpam; 					// log P(spam)
	
	// counters used to determine accuracy of the Naive Bayes model
	int correctClassification;
	int incorrectClassification;
	
	// hash map to store individual words and types
	HashMap<String, Word> words = new HashMap<String, Word>();
	
	// NaiveBayes constructor
	public NaiveBayes (String path) {
		file_path = path;
	}
	
	// transforms the train data by tokenization and adds spam/ham labels according to file name
	public void transformTrainData() throws IOException {
		// create file "train.txt" to format the training data
		final ZipFile trainZipFile = new ZipFile(file_path + "/train.zip");		
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File("train.txt"),true));
		
		// iterate through each file in the zip folder
		final Enumeration<? extends ZipEntry> entries = trainZipFile.entries();
	    while (entries.hasMoreElements()) {
	        final ZipEntry entry = entries.nextElement();
	        // System.out.println(entry.getName());
	        
	        // if file name starts with "spmsg", add "spam" label to text file
	        if (entry.getName().startsWith("spmsg")) {
	        	pw.print("spam\t");
	        }
	        // if file name doesn't start with "spmsg", add "ham" label to text file
	        else {
	        	pw.print("ham\t");
	        }
	        
	        // read, tokenize, and append words to "train.txt" file
	        InputStream stream = trainZipFile.getInputStream(entry);
	        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	        String line;
			while ((line = in.readLine()) != null) {
				String replaceSpecial = line.replaceAll("[^A-Za-z0-9 ]", "");
				String replaceWhitespace = replaceSpecial.replaceAll("\\s+", " ");
				pw.print(replaceWhitespace);
			}
			pw.println(System.lineSeparator());
			in.close();
			stream.close();
	    }
	    pw.close();
	    trainZipFile.close();
	}

	// uses a train-file to make a hash map containing all words, and their probability of being spam
	public void train() throws IOException {		
		transformTrainData();
		BufferedReader in = new BufferedReader(new FileReader("train.txt"));
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.equals("")){
				String type = line.split("\t")[0];
				String sms = line.split("\t")[1];
				
				for (String word : sms.split(" ")) {
					word = word.replaceAll("\\W", "");
					word = word.toLowerCase();
					Word w = null;
					// if hash map already contains word, access it
					if(words.containsKey(word)) {
						w = (Word) words.get(word);
					}
					// if hash map doesn't contain word, add word to hash map
					else {
						w = new Word(word);
						words.put(word,w);
					}
					if(type.equals("ham")) {
						// increment ham count for word
						w.countHam();
						// increment ham count for word in emails
						wordHamCountInEmails++;
					}
					else if(type.equals("spam")) {
						// increment spam count for word
						w.countSpam();
						// increment spam count for word in emails
						wordSpamCountInEmails++;
					}		
				}
			}	
		}
		in.close();
		// loop through each word in the Hash Map, and calculate the probability of ham/spam for each word
		for (String key : words.keySet()) {			
			words.get(key).calculateWordSpamProbability(wordSpamCountInEmails, words.size());
			words.get(key).calculateWordHamProbability(wordHamCountInEmails, words.size());
		}
	}
	
	// transforms the test data by tokenization and adds spam/ham labels according to file name
	public void transformTestData() throws IOException {
		// create file "test.txt" to format the training data
		final ZipFile testZipFile = new ZipFile(file_path + "/test.zip");		
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File("test.txt"),true));
			
		// iterate through each file in the zip folder
		final Enumeration<? extends ZipEntry> entries = testZipFile.entries();
		while (entries.hasMoreElements()) {
			final ZipEntry entry = entries.nextElement();
		    // System.out.println(entry.getName());
		        
		    // if file name starts with "spmsg", add "spam" label to text file
		    if (entry.getName().startsWith("spmsg")) {
		    	pw.print("spam\t");
		    }
		    // if file name doesn't start with "spmsg", add "ham" label to text file
		    else {
		    	pw.print("ham\t");
		    }
		        
		    // read, tokenize, and append words to "train.txt" file
		    InputStream stream = testZipFile.getInputStream(entry);
		    BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		    String line;
		    while ((line = in.readLine()) != null) {
		    	String replaceSpecial = line.replaceAll("[^A-Za-z0-9 ]", "");
				String replaceWhitespace = replaceSpecial.replaceAll("\\s+", " ");
				pw.print(replaceWhitespace);
			}
			pw.println(System.lineSeparator());
			in.close();
			stream.close();
		}
		pw.close();
		testZipFile.close();
	}
	
	// takes the text to be analyzed as input, and produces predictions by form of 'spam' or 'ham'
	public void test() throws IOException {
		transformTestData();
		BufferedReader in = new BufferedReader(new FileReader("test.txt"));
		String line;
		while((line = in.readLine()) != null) {
			if(!line.equals("")) {
				String type = line.split("\t")[0];
		        
				if(type.equals("spam")) {
					// increment number of spam email files
		        	actualSpamEmailTotal++;
		        	line.split("spam");
				}
				else if(type.equals("ham")) {
					// increment number of ham email files
		        	actualHamEmailTotal++;
		        	line.split("ham");
				}		
		        // increment number of email files
				actualEmailTotal++;
				
				// create word list from test set
				ArrayList<Word> sms = makeWordList(line);
				calculateWordProbabilities(sms);				
			}					
		}
		in.close();
		boolean isSpam = calculateBayesTheorem();
		if(isSpam == true) {
			// increment number of ham files in test set
			predictedHamEmailTotal++;
		}
		else if (isSpam == false) {
			// increment number of spam files in training set
			predictedSpamEmailTotal++;
		}
		if(predictedHamEmailTotal == actualHamEmailTotal) {
			correctClassification++;
		}
		else {
			incorrectClassification++;
		}		
		accuracy(correctClassification, incorrectClassification);
	}

	// make an arraylist of all words in an sms
	public ArrayList<Word> makeWordList(String sms) {
		
		ArrayList<Word> wordList = new ArrayList<Word>();
		
		for (String word : sms.split(" ")) {
			word = word.replaceAll("\\W", "");
			word = word.toLowerCase();
			Word w = null;
			if(words.containsKey(word)){
				w = (Word) words.get(word);
			}
			else {
				w = new Word(word);
			}
			wordList.add(w);
		}
		return wordList;
	}
	
	// applying Bayes rule and calculating probability of ham or spam
	// return true if email is ham, false if email is spam
	public void calculateWordProbabilities(ArrayList<Word> sms) {
		
		// loop through words in the test set
		for (int i = 0; i < sms.size(); i++) {
			Word word = (Word) sms.get(i);
			Word w = null;
			// if the hash map contains the word, add their probabilities
			if(words.containsValue(word)) {
				w = (Word) words.get(word);
				probEmailGivenSpam += w.getProbWordGivenSpam();
				probEmailGivenHam += w.getProbWordGivenHam();	
			}
			// if the hash map does not contain the word, set the 
			else {
				probEmailGivenSpam += (float) Math.log((0 + 1)/(0 + words.size()));
				probEmailGivenHam += (float) Math.log((0 + 1)/(0 + words.size()));
			}
		}		
	}
	
	public boolean calculateBayesTheorem() {
		
		probHam = (float) Math.log(actualHamEmailTotal/actualEmailTotal);
		probSpam = (float) Math.log(actualSpamEmailTotal/actualEmailTotal);
		
		probHamGivenEmail = probHam + probEmailGivenHam;
		probSpamGivenEmail = probSpam + probEmailGivenSpam;
		
		// email is ham
		if(probHamGivenEmail > probSpamGivenEmail) {
			return true;
		}	
		// email is spam
		else {
			return false;
		}
	}
	
	// print accuracy of the Naive Bayes algorithm
	public void accuracy(int correctClassification, int incorrectClassification) {
		
		// actual count results from test data
		System.out.println("The actual number of spam emails found within the test data was: " + actualSpamEmailTotal);
		System.out.println("The actual number of ham files found within the test data was: " + actualHamEmailTotal);
		System.out.println("The actual number of emails found within the test data was: " + actualEmailTotal + "\n");
		
		// predicted count results from test data
		System.out.println("The predicted number of spam emails found within the test data was: " + predictedSpamEmailTotal);
		System.out.println("The predicted number of ham files found within the test data was: " + predictedHamEmailTotal + "\n");
		
		// counts used for accuracy measure
		System.out.println("The correct number of emails classified within the test data was: " + correctClassification);
		System.out.println("The incorrect number of emails classified within the test data was: " + incorrectClassification + "\n");
		
		// print the accuracy of the algorithm (number of correctly classified messages/total number of messages in the set)
		float accuracyMeasure = (float) (((correctClassification)/(correctClassification + incorrectClassification + 0.0))*100);
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		System.out.println("The Naive Bayes algorithm successfully predicted " + df.format(accuracyMeasure) + "% of the emails found in the test set!");
		time = System.currentTimeMillis()-time;
		System.out.println("Time: " + time/1000d + "s");
		
		// delete files so data isn't appended during future executions
		File trainFile = new File("train.txt");
		trainFile.delete();
		File testFile = new File("test.txt");
		testFile.delete();
	}
}
