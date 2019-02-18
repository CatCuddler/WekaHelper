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
import weka.core.SelectedTag;
import weka.core.WekaEnumeration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ClassifierFactory {

    public enum ClassifierType {
        J48, NaiveBayes, RandomForest, ZeroR, OneR, SMO, DecisionTable, GaussianProcess, M5P, KStar, LMT, BayesNet,
        JRip, SimpleLogistic, LinearRegression, VotedPerceptron, SGD, Logistic, MultilayerPerceptron, REPTree,
        IBk, RandomTree, SMOreg, LibSVM, LibLinear
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
                    break;
                case RandomForest:
                    classifiers.add(getRandomForest());
                    break;
                case ZeroR:
                    classifiers.add(getZeroR());
                    break;
                case OneR:
                    classifiers.add(getOneR());
                    break;
                case LibSVM:
                    classifiers.add(getLibSVM());
                    break;
                case LibLinear:
                    classifiers.add(getLibLinear());
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
        return classifier;
    }

    private Classifier getSGD() {
        MultiClassClassifier classifier = new MultiClassClassifier();
        classifier.setClassifier(new SGD());
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

    private Classifier getLibSVM() {
        LibSVM libSVM = new LibSVM();
        libSVM.setNormalize(true);
//        libSVM.setEps(0.000000000001);
//        libSVM.setSVMType(new SelectedTag(LibSVM.SVMTYPE_NU_SVC, LibSVM.TAGS_SVMTYPE));
//        libSVM.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
//        libSVM.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_POLYNOMIAL, LibSVM.TAGS_KERNELTYPE));
//        libSVM.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_SIGMOID, LibSVM.TAGS_KERNELTYPE));


        // normalize: 2-3h, 0.92 - ALL_1 (lLeg-rForearm)
        // normalize, linear kernel: 10min, 0.93-ALL_2 (identical to SMO results)
        // normalize, polynomial kernel: expected 15 hours, ~.35 in first two test subject, aborted
        // normalize, nu, eps=0.000000000001: over-night-long, ~.82 in first two complete tests, aborted
        // normalize, sigmoid kernel: 3h, .91-All_1

        return libSVM;
    }

    private Classifier getLibLinear() {
        LibLINEAR libLinear = new LibLINEAR();
        libLinear.setNormalize(true);
//        String[] options = {"-S", "4", "-C", "1.0", "-E", "0.001", "-B 1.0", "-Z", "-L", "0.1", "-I", "1000"};
//        try {
//            libLinear.setOptions(options);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // normalize, Cramer/Singer SVM: 13min, .91-.99
        // normalize: 10min, .84-ALL_1
        return libLinear;
    }

}
