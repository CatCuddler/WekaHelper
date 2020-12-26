package com.romanuhlig.weka.classification;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.*;
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
        IBk, RandomTree, SMOreg, LibSVM, LibLinear, SMOfeatureSelected,
        AdaBoost, Bagging, RandomCommittee, Stacking, Vote
    }

    /**
     * Check for specific meta classifier
     * @param classifierTypes
     * @param meta
     * @return
     */
    private boolean checkForMetaClassifier(List<ClassifierType> classifierTypes, ClassifierType meta) {
        for (ClassifierType classifierType : classifierTypes) {
            if (classifierType == meta) {
                return true;
            }
        }
        return false;
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
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getJ48()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getJ48()));
                    }
                    break;
                case NaiveBayes:
                    classifiers.add(getNaiveBayes());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getNaiveBayes()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getNaiveBayes()));
                    }
                    break;
                case RandomForest:
                    classifiers.add(getRandomForest());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getRandomForest()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getRandomForest()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.RandomCommittee)) {
                        classifiers.add(getRandomCommittee(getRandomForest()));
                    }
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
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getSMO()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getSMO()));
                    }
                    break;
                case DecisionTable:
                    classifiers.add(getDecisionTable());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getDecisionTable()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getDecisionTable()));
                    }
                    break;
                case GaussianProcess:
                    classifiers.add(getGaussianProcess());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getGaussianProcess()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getGaussianProcess()));
                    }
                    break;
                case M5P:
                    classifiers.add(getM5P());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getM5P()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getM5P()));
                    }
                    break;
                case KStar:
                    classifiers.add(getKStar());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getKStar()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getKStar()));
                    }
                    break;
                case LMT:
                    classifiers.add(getLMT());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getLMT()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getLMT()));
                    }
                    break;
                case BayesNet:
                    classifiers.add(getBayesNet());
                    /*if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getBayesNet()));
                    }*/
                    /*if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getBayesNet()));
                    }*/
                    break;
                case JRip:
                    classifiers.add(getJRip());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getJRip()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getJRip()));
                    }
                    break;
                case SimpleLogistic:
                    classifiers.add(getSimpleLogistic());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getSimpleLogistic()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getSimpleLogistic()));
                    }
                    break;
                case LinearRegression:
                    classifiers.add(getLinearRegression());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getLinearRegression()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getLinearRegression()));
                    }
                    break;
                case VotedPerceptron:
                    classifiers.add(getVotedPerceptron());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getVotedPerceptron()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getVotedPerceptron()));
                    }
                    break;
                case SGD:
                    classifiers.add(getSGD());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getSGD()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getSGD()));
                    }
                    break;
                case Logistic:
                    classifiers.add(getLogistic());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getLogistic()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getLogistic()));
                    }
                    break;
                case MultilayerPerceptron:
                    classifiers.add(getMultilayerPerceptron());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getMultilayerPerceptron()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getMultilayerPerceptron()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.RandomCommittee)) {
                        classifiers.add(getRandomCommittee(getMultilayerPerceptron()));
                    }
                    break;
                case REPTree:
                    classifiers.add(getREPTree());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getREPTree()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getREPTree()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.RandomCommittee)) {
                        classifiers.add(getRandomCommittee(getREPTree()));
                    }
                    break;
                case IBk:
                    classifiers.add(getIBk());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getIBk()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getIBk()));
                    }
                    break;
                case RandomTree:
                    classifiers.add(getRandomTree());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getRandomTree()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getRandomTree()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.RandomCommittee)) {
                        classifiers.add(getRandomCommittee(getRandomTree()));
                    }
                    break;
                case SMOreg:
                    classifiers.add(getSMOreg());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getSMOreg()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getSMOreg()));
                    }
                    break;
                case SMOfeatureSelected:
                    classifiers.add(getSMOfeatureSelected());
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.AdaBoost)) {
                        classifiers.add(getAdaBoost(getSMOfeatureSelected()));
                    }
                    if (checkForMetaClassifier(classifierTypes, ClassifierType.Bagging)) {
                        classifiers.add(getBagging(getSMOfeatureSelected()));
                    }
                    break;
                case Stacking:
                    classifiers.add(getStacking());
                    break;
                case Vote:
                    classifiers.add(getVote());
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
     * Meta classifier
     * @return
     */
    private Classifier getAdaBoost(Classifier classifier) {
        AdaBoostM1 adaBoost = new AdaBoostM1();
        adaBoost.setClassifier(classifier);
        return adaBoost;
    }

    /**
     * Meta classifier
     * @return
     */
    private Classifier getBagging(Classifier classifier) {
        Bagging bagging = new Bagging();
        bagging.setClassifier(classifier);
        return bagging;
    }

    /**
     * Meta classifier
     * @return
     */
    private Classifier getRandomCommittee(Classifier classifier) {
        RandomCommittee randomCommittee = new RandomCommittee();
        randomCommittee.setClassifier(classifier);
        return randomCommittee;
    }

    /**
     * Meta classifier
     * @return
     */
    private Classifier getStacking() {
        Stacking stacking = new Stacking();
        stacking.setMetaClassifier(getSMO());
        Classifier[] classifiers = { getRandomForest(), getREPTree(), getJRip(), getBayesNet(),
                getNaiveBayes(), getJ48(), getRandomTree(), getOneR() };
        stacking.setClassifiers(classifiers);
        return stacking;
    }

    /**
     * Meta classifier
     * @return
     */
    private Classifier getVote() {
        Vote vote = new Vote();
        Classifier[] classifiers = { getSMO(), getRandomForest(), getREPTree(), getJRip(),
                getBayesNet(), getNaiveBayes(), getJ48(), getRandomTree(), getOneR(), getZeroR() };
        vote.setClassifiers(classifiers);
        return vote;
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
        VotedPerceptron classifier = new VotedPerceptron();
        return classifier;
    }

    /**
     * The SGD classifier
     *
     * @return
     */
    private Classifier getSGD() {
        SGD classifier = new SGD();
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
