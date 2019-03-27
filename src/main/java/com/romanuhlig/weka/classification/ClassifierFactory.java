package com.romanuhlig.weka.classification;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Produces Weka compatible classifiers
 *
 * @author Roman Uhlig
 */
public class ClassifierFactory {

    public enum ClassifierType {
        J48, NaiveBayes, RandomForest, ZeroR, OneR, SMO, DecisionTable, GaussianProcess, M5P, KStar, LMT, BayesNet,
        JRip, SimpleLogistic, LinearRegression, VotedPerceptron, SGD, Logistic, MultilayerPerceptron, REPTree,
        IBk, RandomTree, SMOreg, LibSVM, LibLinear, SMOfeatureSelected
    }


    /**
     * Returns a list of classifiers based on the requested types
     *
     * @param classifierTypes
     * @return
     */
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
                case SMOfeatureSelected:
                    classifiers.add(getSMOfeatureSelected());
                    break;
                default:
                    System.err.println(classifierType.toString() + "  classifier not implemented!");
            }
        }

        return classifiers;
    }

    /**
     * The J48 classifier
     *
     * @return
     */
    private Classifier getJ48() {
        Classifier classifier = new J48();
        return classifier;
    }

    /**
     * The Naive Bayes classifier
     *
     * @return
     */
    private Classifier getNaiveBayes() {
        Classifier classifier = new NaiveBayes();
        return classifier;
    }

    /**
     * The Random Forest classifier
     *
     * @return
     */
    private Classifier getRandomForest() {
        Classifier classifier = new RandomForest();
        return classifier;
    }

    /**
     * The ZeroR classifier
     *
     * @return
     */
    private Classifier getZeroR() {
        Classifier classifier = new ZeroR();
        return classifier;
    }

    /**
     * The OneR classifier
     *
     * @return
     */
    private Classifier getOneR() {
        Classifier classifier = new OneR();
        return classifier;
    }

    /**
     * The SMO classifier
     *
     * @return
     */
    private Classifier getSMO() {
        SMO classifier = new SMO();

        return classifier;
    }

    /**
     * The SMO classifier with additional built in feature selection
     * Does not work with the thesis data set
     *
     * @return
     */
    private Classifier getSMOfeatureSelected() {
        AttributeSelectedClassifier asClassifier = new AttributeSelectedClassifier();
        asClassifier.setClassifier(getSMO());

        // potential settings:

        // GreedyStepwise search = new GreedyStepwise();
        // search.setNumToSelect(50);
        // asClassifier.setSearch(search);

        // WrapperSubsetEval evaluator = new WrapperSubsetEval();
        // evaluator.setClassifier(getSMO());
        // asClassifier.setEvaluator(evaluator);

        // ConsistencySubsetEval evaluator = new ConsistencySubsetEval();
        // asClassifier.setEvaluator(evaluator);

        // GainRatioAttributeEval evaluator = new GainRatioAttributeEval();
        // asClassifier.setEvaluator(evaluator);
        // Ranker search = new Ranker();
        // search.setNumToSelect(50);
        // asClassifier.setSearch(search);

        // OneRAttributeEval evaluator = new OneRAttributeEval();
        // asClassifier.setEvaluator(evaluator);
        // Ranker search = new Ranker();
        // search.setNumToSelect(50);
        // asClassifier.setSearch(search);

        return asClassifier;
    }

    /**
     * The Decision Table classifier
     *
     * @return
     */
    private Classifier getDecisionTable() {
        Classifier classifier = new DecisionTable();
        return classifier;
    }

    /**
     * The Gaussian Process classifier
     *
     * @return
     */
    private Classifier getGaussianProcess() {
        Classifier classifier = new GaussianProcesses();
        return classifier;
    }

    /**
     * The M5P classifier
     *
     * @return
     */
    private Classifier getM5P() {
        Classifier classifier = new M5P();
        return classifier;
    }

    /**
     * The KStar classifier
     *
     * @return
     */
    private Classifier getKStar() {
        Classifier classifier = new KStar();
        return classifier;
    }

    /**
     * The LMT classifier
     *
     * @return
     */
    private Classifier getLMT() {
        Classifier classifier = new LMT();
        return classifier;
    }

    /**
     * The Bayes Net classifier
     *
     * @return
     */
    private Classifier getBayesNet() {
        Classifier classifier = new BayesNet();
        return classifier;
    }

    /**
     * The JRip classifier
     *
     * @return
     */
    private Classifier getJRip() {
        Classifier classifier = new JRip();
        return classifier;
    }

    /**
     * The Simple Logistic classifier
     *
     * @return
     */
    private Classifier getSimpleLogistic() {
        Classifier classifier = new SimpleLogistic();
        return classifier;
    }

    /**
     * The Linear Regression classifier
     *
     * @return
     */
    private Classifier getLinearRegression() {
        Classifier classifier = new LinearRegression();
        return classifier;
    }

    /**
     * The Voted Perceptron classifier
     *
     * @return
     */
    private Classifier getVotedPerceptron() {
        MultiClassClassifier classifier = new MultiClassClassifier();
        classifier.setClassifier(new VotedPerceptron());
        return classifier;
    }

    /**
     * The SGD classifier
     *
     * @return
     */
    private Classifier getSGD() {
        MultiClassClassifier classifier = new MultiClassClassifier();
        classifier.setClassifier(new SGD());
        return classifier;
    }

    /**
     * The Logistic classifier
     *
     * @return
     */
    private Classifier getLogistic() {
        Classifier classifier = new Logistic();
        return classifier;
    }

    /**
     * The Multilayer Perceptron classifier
     *
     * @return
     */
    private Classifier getMultilayerPerceptron() {
        Classifier classifier = new MultilayerPerceptron();
        return classifier;
    }

    /**
     * The RepTree classifier
     *
     * @return
     */
    private Classifier getREPTree() {
        Classifier classifier = new REPTree();
        return classifier;
    }

    /**
     * The IBK classifier
     *
     * @return
     */
    private Classifier getIBk() {
        Classifier classifier = new IBk();
        return classifier;
    }

    /**
     * The Random Tree classifier
     *
     * @return
     */
    private Classifier getRandomTree() {
        Classifier classifier = new RandomTree();
        return classifier;
    }

    /**
     * The SMOreg classifier
     *
     * @return
     */
    private Classifier getSMOreg() {
        Classifier classifier = new SMOreg();
        return classifier;
    }


    /**
     * The LibSVM classifier wrapper
     *
     * @return
     */
    private Classifier getLibSVM() {
        // removed for now due to maven build issues if the required repository is included
        System.out.println("LibSVM removed for now due to maven build issues if required repository is included");

        // potential settings:

        // LibSVM libSVM = new LibSVM();
        // libSVM.setNormalize(true);
        // libSVM.setEps(0.000000000001);
        // libSVM.setSVMType(new SelectedTag(LibSVM.SVMTYPE_NU_SVC, LibSVM.TAGS_SVMTYPE));
        // libSVM.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
        // libSVM.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_POLYNOMIAL, LibSVM.TAGS_KERNELTYPE));
        // libSVM.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_SIGMOID, LibSVM.TAGS_KERNELTYPE));

        // return libSVM;

        return new ZeroR();

    }

    /**
     * The LibLinear classifier wrapper
     *
     * @return
     */
    private Classifier getLibLinear() {
        // removed for now due to maven build issues if required repository is included
        System.out.println("LibLinear removed for now due to maven build issues if required repository is included");

        // potential settings:

        // LibLINEAR libLinear = new LibLINEAR();
        // libLinear.setNormalize(true);
        // String[] options = {"-S", "4", "-C", "1.0", "-E", "0.001", "-B 1.0", "-Z", "-L", "0.1", "-I", "1000"};
        // try {
        //     libLinear.setOptions(options);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }


//    	return libLinear;
        return new ZeroR();
    }

}
