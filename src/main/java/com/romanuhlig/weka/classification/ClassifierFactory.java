package com.romanuhlig.weka.classification;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

import java.util.ArrayList;
import java.util.List;

public class ClassifierFactory {

    public enum ClassifierType {J48, NaiveBayes, RandomForest, ZeroR, OneR}


    public ArrayList<Classifier> getClassifiers(List<ClassifierType> classifierTypes) {

        ArrayList<Classifier> classifiers = new ArrayList<>();

        for (ClassifierType classifierType : classifierTypes) {

            switch (classifierType) {
                case J48:
                    classifiers.add(getJ48());
                    break;
                case NaiveBayes:
                    classifiers.add(getNaiveBayes());
                    break; // optional
                case RandomForest:
                    classifiers.add(getRandomForest());
                    break;
                case ZeroR:
                    classifiers.add(getZeroR());
                    break;
                case OneR:
                    classifiers.add(getOneR());
                    break;
                default:
                    System.err.println(classifierType.toString() + "  classifier not implemented!");
            }
        }

        return classifiers;
    }

    private Classifier getJ48() {
        Classifier classifier = new J48();
        return classifier;
    }

    private Classifier getNaiveBayes() {
        Classifier classifier = new NaiveBayes();
        return classifier;
    }

    private Classifier getRandomForest() {
        Classifier classifier = new RandomForest();
        return classifier;
    }

    private Classifier getZeroR() {
        Classifier classifier = new ZeroR();
        return classifier;
    }

    private Classifier getOneR() {
        Classifier classifier = new OneR();
        return classifier;
    }


}
