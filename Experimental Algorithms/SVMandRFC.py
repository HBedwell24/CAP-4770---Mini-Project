def make_Dictionary(train_dir):
    all_words = []   
    with ZipFile(train_dir, "r") as z:
        for filename in z.namelist():
            if not os.path.isdir(filename):
                with z.open(filename) as f:
                    for i, line in enumerate(f):
                        if i >= 2 and i != None: # Body of email is 3rd line on
                            words = line.split()
                            all_words += words
    
        dictionary = Counter(all_words)
        
        # Transform the data
        for item in list(dictionary):
            if item.isalpha() == False: 
                del dictionary[item]
            elif len(item) == 1:
                del dictionary[item]
    dictionary = dictionary.most_common(3000)
    return dictionary

def extract_features(test_dir): 
    docID = 0;
    all_words = []   
    with ZipFile(test_dir, "r") as z:
        features_matrix = np.zeros((len(z.namelist()),3000))
        for filename in z.namelist():
            if not os.path.isdir(filename):
                with z.open(filename) as f:
                    for i, line in enumerate(f):
                        if i >= 2 and i != None: # Body of email is 3rd line on
                            words = line.split()
                            for word in words:
                                wordID = 0
                                for i,d in enumerate(dictionary):
                                    if d[0] == word:
                                        wordID = i
                                        features_matrix[docID,wordID] = words.count(word)
                    docID = docID + 1     
        return features_matrix

# Main method
from zipfile import ZipFile
from __future__ import print_function
import os
import numpy as np
from collections import Counter
from sklearn.naive_bayes import MultinomialNB, GaussianNB, BernoulliNB
from sklearn.svm import SVC, NuSVC, LinearSVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import confusion_matrix

algorithm = input("Please select an algorithm to classify the data (SVM/RFC): ")

if algorithm == "SVM":
    
    # Create a dictionary of words with its frequency
    directory = input("Please list a directory that contains both your train and test data: ")
    train_dir = directory + "/train.zip"
    dictionary = make_Dictionary(train_dir)

    # Prepare feature vectors per training mail and its labels
    train_labels = np.zeros(300)
    train_labels[150:299] = 1
    train_matrix = extract_features(train_dir)

    # Training SVM
    model = LinearSVC()
    model.fit(train_matrix,train_labels)

    # Test the unseen mails for Spam
    test_dir = directory + "/test.zip"
    test_matrix = extract_features(test_dir)
    test_labels = np.zeros(201)
    test_labels[100:200] = 1
    result = model.predict(test_matrix)
    print ("The Support Vector Machine successfully predicted " + confusion_matrix(test_labels, result) + 
           "% of the files located within the testing data.")

elif argument == "RFC":
    
    # Create a dictionary of words with its frequency
    train_dir = input("Please list a directory that contains both your train and test data: ")
    train_dir = train_dir + "/train.zip"
    dictionary = make_Dictionary(train_dir)
    
    test_dir = train_dir + "/test.zip"
    features_matrix, labels = extract_features(train_dir)
    test_feature_matrix, test_labels = extract_features(test_dir)
    model = RandomForestClassifier()
    
    # Test the unseen mails for Spam
    model.fit(features_matrix, labels)
    predicted_labels = model.predict(test_feature_matrix)
    print ("The Random Forest Classifier successfully predicted " + accuracy_score(test_labels, predicted_labels) + 
           "% of the files located within the testing data.")
