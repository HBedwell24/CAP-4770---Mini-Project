// URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
	
	// global variables
	int spamEmailTotal = 0;
	int hamEmailTotal = 0;
	int emailTotal = 0;
	int totalSpamCount = 0;
	int totalHamCount = 0;
	int spamCount = 0;
	int hamCount = 0;
	int probEmailGivenHam = 0;
	int probEmailGivenSpam = 0;	
	
	HashMap<String, Word> words = new HashMap<String, Word>();
	
	// transforms the train data by tokenization and adds spam/ham labels according to file name
	public void transformTrainData(String path) throws IOException {
		// create file "train.txt" to format the training data
		final ZipFile trainZipFile = new ZipFile(path);		
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File("train.txt"),true));
		
		// iterate through each file in the zip folder
		final Enumeration<? extends ZipEntry> entries = trainZipFile.entries();
	    while (entries.hasMoreElements()) {
	        final ZipEntry entry = entries.nextElement();
	        // System.out.println(entry.getName());
	        
	        // if file name starts with "spmsg", add "spam" label to text file
	        if (entry.getName().startsWith("spmsg")) {
	        	pw.print("spam\t");
	        	// increment number of spam email files
	        	spamEmailTotal++;
	        }
	        // if file name doesn't start with "spmsg", add "ham" label to text file
	        else {
	        	pw.print("ham\t");
	        	// increment number of ham email files
	        	hamEmailTotal++;
	        }
	        // increment number of email files
			emailTotal++;
	        
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

	// uses a train-file to make a hashmap containing all words, and their probability of being spam
	public void train(String path) throws IOException {		
		transformTrainData(path);
		BufferedReader in = new BufferedReader(new FileReader("train.txt"));
		String line = in.readLine();
		while (line != null){
			if (!line.equals("")){
				String type = line.split("\t")[0];
				String sms = line.split("\t")[1];
				
				for (String word : sms.split(" ")) {
					word = word.replaceAll("\\W", "");
					word = word.toLowerCase();
					Word w = null;
					// if HashMap already contains word, access it
					if(words.containsKey(word)) {
						w = (Word) words.get(word);
					}
					// if HashMap doesn't contain word, add word to HashMap
					else {
						w = new Word(word);
						words.put(word,w);
					}
					if(type.equals("ham")) {
						// increment ham count for word
						w.countHam();
						// increment ham count for email
						totalHamCount++;
					}
					else if(type.equals("spam")) {
						// increment spam count for word
						w.countSpam();
						// increment spam count for email 
						totalSpamCount++;
					}		
				}
			}	
			line = in.readLine();	
		}
		in.close();
		// loop through each word in the key set
		for (String key : words.keySet()) {			
			probEmailGivenSpam += words.get(key).calculateWordSpamProbability(totalSpamCount, words.size());
			probEmailGivenHam += words.get(key).calculateWordHamProbability(totalHamCount, words.size());
		}
	}
	
	// takes the text to be analyzed as input, and produces predictions by form of 'spam' or 'ham'
	public void test(String path) throws IOException {
		final ZipFile testZipFile = new ZipFile(path);
		
		try {
			final Enumeration<? extends ZipEntry> entries = testZipFile.entries();
		    while (entries.hasMoreElements()) {
		    	
		        final ZipEntry entry = entries.nextElement();
		        InputStream stream = testZipFile.getInputStream(entry);
		        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		        
				String line;
				while ((line = in.readLine()) != null) {
					if (!line.equals("")) {
						ArrayList<Word> sms = makeWordList(line);
						boolean isSpam = calculateBayes(sms);
						if(isSpam == true) {
							// increment number of ham files in test set
							hamCount++;
						}
						else if (isSpam == false) {
							// increment number of spam files in training set
							spamCount++;
						}
					}					
				}
				in.close();
				stream.close();
			}
		    testZipFile.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		accuracy(spamCount, spamEmailTotal, hamCount, hamEmailTotal);
	}

	// make an arraylist of all words in an sms, set probability of spam to 0.4 if word is not known
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
				w.setProbOfSpam(0.4f);
			}
			wordList.add(w);
		}
		return wordList;
	}
	
	// applying Bayes rule and calculating probability of ham or spam. Return true if spam, false if ham
	public boolean calculateBayes(ArrayList<Word> sms) {
		// logarithm of P(ham|body text) which will be calculated
		int probHamGivenEmail = 0;
		// logarithm of P(spam|body text) which will be calculated
		int probSpamGivenEmail = 0;
		// logarithm of P(ham)
		int probHam = hamEmailTotal/emailTotal;
		// logarithm of P(spam)
		int probSpam = spamEmailTotal/emailTotal;
		
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
	public void accuracy(int spamCount, int spamEmailTotal, int hamCount, int hamEmailTotal) {
		
		// spam results from training data
		System.out.println("The number of spam files found within the training data was: " + spamEmailTotal);
		
		// ham results from training data
		System.out.println("The number of ham files found within the training data was: " + hamEmailTotal);
		
		// spam results from test data
		System.out.println("The number of spam files found within the test data was: " + spamCount);
		
		// ham results from test data
		System.out.println("The number of ham files found within the test data was: " + hamCount);
		
		// print the accuracy of the algorithm (number of correctly classified messages/total number of messages in the set)
		float accuracyAmount = (((float)spamCount + (float)hamCount)/(spamEmailTotal + hamEmailTotal))*100;
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		System.out.println("The Naive Bayes algorithm successfully predicted " + df.format(accuracyAmount) + "% of the spam emails found in the test set!");
	}
}
