// Reference URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes

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
	
	// relative file path of input data
	String file_path;
	
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

	// uses a train-file to make a hashmap containing all words, and their probability of being spam
	public void train() throws IOException {		
		transformTrainData();
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
			line = in.readLine();	
		}
		in.close();
		// loop through each word in the Hash Map, and add the probability of ham/spam to each word
		for (String key : words.keySet()) {			
			words.get(key).calculateWordSpamProbability(wordSpamCountInEmails, words.size());
			words.get(key).calculateWordHamProbability(wordHamCountInEmails, words.size());
		}
	}
	
	// takes the text to be analyzed as input, and produces predictions by form of 'spam' or 'ham'
	public void test() throws IOException {
		final ZipFile testZipFile = new ZipFile(file_path + "/test.zip");
		
		try {
			final Enumeration<? extends ZipEntry> entries = testZipFile.entries();
		    while (entries.hasMoreElements()) {
		    	
		        final ZipEntry entry = entries.nextElement();
		        InputStream stream = testZipFile.getInputStream(entry);
		        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		        
		        // if file name starts with "spmsg", add "spam" label to text file
		        if (entry.getName().startsWith("spmsg")) {
		        	// increment number of spam email files
		        	actualSpamEmailTotal++;
		        }
		        // if file name doesn't start with "spmsg", add "ham" label to text file
		        else {
		        	// increment number of ham email files
		        	actualHamEmailTotal++;
		        }
		        // increment number of email files
				actualEmailTotal++;
				
				String line;
				while ((line = in.readLine()) != null) {
					if (!line.equals("")) {
						ArrayList<Word> sms = makeWordList(line);
						boolean isSpam = calculateBayes(sms);
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
	
	// applying Bayes rule and calculating probability of ham or spam. Return true if spam, false if ham
	public boolean calculateBayes(ArrayList<Word> sms) {
		
		probHam = (float) Math.log(actualHamEmailTotal/actualEmailTotal);
		probSpam = (float) Math.log(actualSpamEmailTotal/actualEmailTotal);
		
		for (int i = 0; i < sms.size(); i++) {
			Word word = (Word) sms.get(i);
			probEmailGivenSpam += word.getProbWordGivenSpam();
			probEmailGivenHam += word.getProbWordGivenHam();		
		}
		
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
		
		// spam results from test data
		System.out.println("The actual number of spam files found within the test data was: " + actualSpamEmailTotal);
		System.out.println("The predicted number of spam files found within the test data was: " + predictedSpamEmailTotal);
		
		// ham results from test data
		System.out.println("The actual number of ham files found within the test data was: " + actualHamEmailTotal);
		System.out.println("The predicted number of ham files found within the test data was: " + predictedHamEmailTotal);
		
		// print the accuracy of the algorithm (number of correctly classified messages/total number of messages in the set)
		float accuracyMeasure = (float) (((correctClassification)/(correctClassification + incorrectClassification + 0.0))*100);
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		System.out.println("The Naive Bayes algorithm successfully predicted " + df.format(accuracyMeasure) + "% of the emails found in the test set!");
	}
}
