package com.romanuhlig.weka.classification;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;

import java.util.ArrayList;
import java.util.List;

public class ClassifierFactory {

    public enum ClassifierType {
        J48, NaiveBayes, RandomForest, ZeroR, OneR, SMO, DecisionTable, GaussianProcess, M5P, KStar, LMT, BayesNet,
        JRip, SimpleLogistic, LinearRegression, VotedPerceptron, SGD, Logistic, MultilayerPerceptron, REPTree,
        IBk, RandomTree, SMOreg
    }


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
                case SMO:
                    classifiers.add(getSMO());
                    break;
                case DecisionTable:
                    classifiers.add(getDecisionTable());
                    break;
                case GaussianProcess:
                    classifiers.add(getGaussianProcess());
                    break;
                case M5P:
                    classifiers.add(getM5P());
                    break;
                case KStar:
                    classifiers.add(getKStar());
                    break;
                case LMT:
                    classifiers.add(getLMT());
                    break;
                case BayesNet:
                    classifiers.add(getBayesNet());
                    break;
                case JRip:
                    classifiers.add(getJRip());
                    break;
                case SimpleLogistic:
                    classifiers.add(getSimpleLogistic());
                    break;
                case LinearRegression:
                    classifiers.add(getLinearRegression());
                    break;
                case VotedPerceptron:
                    classifiers.add(getVotedPerceptron());
                    break;
                case SGD:
                    classifiers.add(getSGD());
                    break;
                case Logistic:
                    classifiers.add(getLogistic());
                    break;
                case MultilayerPerceptron:
                    classifiers.add(getMultilayerPerceptron());
                    break;
                case REPTree:
                    classifiers.add(getREPTree());
                    break;
                case IBk:
                    classifiers.add(getIBk());
                    break;
                case RandomTree:
                    classifiers.add(getRandomTree());
                    break;
                case SMOreg:
                    classifiers.add(getSMOreg());
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

    private Classifier getSMO() {
        Classifier classifier = new SMO();
        return classifier;
    }

    private Classifier getDecisionTable() {
        Classifier classifier = new DecisionTable();
        return classifier;
    }

    private Classifier getGaussianProcess() {
        Classifier classifier = new GaussianProcesses();
        return classifier;
    }

    private Classifier getM5P() {
        Classifier classifier = new M5P();
        return classifier;
    }

    private Classifier getKStar() {
        Classifier classifier = new KStar();
        return classifier;
    }

    private Classifier getLMT() {
        Classifier classifier = new LMT();
        return classifier;
    }

    private Classifier getBayesNet() {
        Classifier classifier = new BayesNet();
        return classifier;
    }

    private Classifier getJRip() {
        Classifier classifier = new JRip();
        return classifier;
    }

    private Classifier getSimpleLogistic() {
        Classifier classifier = new SimpleLogistic();
        return classifier;
    }

    private Classifier getLinearRegression() {
        Classifier classifier = new LinearRegression();
        return classifier;
    }

    private Classifier getVotedPerceptron() {
        MultiClassClassifier classifier = new MultiClassClassifier();
        classifier.setClassifier(new VotedPerceptron());
//        Classifier classifier = new VotedPerceptron();
        return classifier;
    }

    private Classifier getSGD() {
        MultiClassClassifier classifier = new MultiClassClassifier();
        classifier.setClassifier(new SGD());
//        Classifier classifier = new SGD();
        return classifier;
    }

    private Classifier getLogistic() {
        Classifier classifier = new Logistic();
        return classifier;
    }

    private Classifier getMultilayerPerceptron() {
        Classifier classifier = new MultilayerPerceptron();
        return classifier;
    }

    private Classifier getREPTree() {
        Classifier classifier = new REPTree();
        return classifier;
    }

    private Classifier getIBk() {
        Classifier classifier = new IBk();
        return classifier;
    }

    private Classifier getRandomTree() {
        Classifier classifier = new RandomTree();
        return classifier;
    }

    private Classifier getSMOreg() {
        Classifier classifier = new SMOreg();
        return classifier;
    }

}
