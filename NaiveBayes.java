// URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NaiveBayes {
	HashMap<String, Word> words = new HashMap<String, Word>();
	
	// NaiveBayes Constructor
	public NaiveBayes() {
		
	}
	
	// transforms the train data by tokenization and adding spam/ham labels according to file name
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
	public int train(String path) throws IOException {
		
		int totalSpamCount = 0;
		int totalHamCount = 0;
		
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
					if(words.containsKey(word)) {
						w = (Word) words.get(word);
					}
					else {
						w = new Word(word);
						words.put(word,w);
					}
					if(type.equals("ham")) {
						w.countHam();
						totalHamCount++;
					}
					else if(type.equals("spam")) {
						w.countSpam();
						totalSpamCount++;
					}		
				}
			}	
			line = in.readLine();	
		}
		in.close();
		for (String key : words.keySet()) {
			words.get(key).calculateProbability(totalSpamCount, totalHamCount);
		}
		return totalSpamCount;
	}
	
	// takes the text to be analyzed as input, and produces predictions by form of 'spam' or 'ham'
	public int filter(String path) throws IOException {
		final ZipFile testZipFile = new ZipFile(path);
		int spamCount = 0;
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
		return spamCount;
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
				w.setProbOfSpam(0.40f);
			}
			wordList.add(w);
		}
		return wordList;
	}
	
	// applying Bayes rule and calculating probability of ham or spam. Return true if spam, false if ham
	public boolean calculateBayes(ArrayList<Word> sms) {
		
		float probabilityOfPositiveProduct = 1.0f;
		float probabilityOfNegativeProduct = 1.0f;
		
		for (int i = 0; i < sms.size(); i++) {
			Word word = (Word) sms.get(i);
			probabilityOfPositiveProduct *= word.getProbOfSpam();
			probabilityOfNegativeProduct *= (1.0f - word.getProbOfSpam());
		}
		
		float probOfSpam = probabilityOfPositiveProduct / (probabilityOfPositiveProduct + probabilityOfNegativeProduct);
		
		if(probOfSpam > 0.9f) {
			return true;
		}		
		else {
			return false;
		}
	}
	
	public void accuracy(int spamCount, int spamTotal) {
		
		float accuracy;
		
		// results from test data
		System.out.println(spamCount);
		// results from training data
		System.out.println(spamTotal);
		accuracy = (spamCount/spamTotal)*100;
		System.out.println("The Naive Bayes algorithm successfully predicted " + accuracy + "% of the spam emails found in the test set!");
	}
}
