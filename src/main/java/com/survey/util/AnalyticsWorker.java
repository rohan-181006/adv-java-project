package com.survey.util;

import com.survey.dao.ResponseDAO;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * AnalyticsWorker
 * Demonstrates multithreading using ExecutorService.
 * Runs analytics computation in the background.
 */
public class AnalyticsWorker implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(AnalyticsWorker.class.getName());

    public interface AnalyticsCallback {
        void onComplete(AnalyticsResult result);
        void onError(Exception e);
    }

    private final int surveyId;
    private final ResponseDAO responseDAO;
    private final AnalyticsCallback callback;

    // Fixed thread pool for background tasks
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AnalyticsWorker(int surveyId, AnalyticsCallback callback) {
        this.surveyId = surveyId;
        this.responseDAO = new ResponseDAO();
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting analytics for survey ID: " + surveyId +
                        " | Thread: " + Thread.currentThread().getName());

            // Simulating processing delay
            Thread.sleep(300);

            AnalyticsResult result = new AnalyticsResult();
            result.surveyId = surveyId;
            result.respondentCount = responseDAO.getRespondentCount(surveyId);
            result.overallScore = responseDAO.getOverallScore(surveyId);
            result.averageRatings = responseDAO.getAverageRatings(surveyId);
            result.choiceDistribution = responseDAO.getChoiceDistribution(surveyId);
            result.textResponses = responseDAO.getTextResponses(surveyId);

            LOGGER.info("Analytics completed successfully for survey ID: " + surveyId);
            callback.onComplete(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Analytics task interrupted for survey ID: " + surveyId, e);
            callback.onError(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while processing analytics for survey ID: " + surveyId, e);
            callback.onError(e);
        }
    }

    /**
     * Submits the worker to the thread pool for execution
     */
    public static void submit(AnalyticsWorker worker) {
        executor.submit(worker);
    }

    /**
     * Gracefully shuts down the thread pool
     */
    public static void shutdown() {
        executor.shutdown();
    }

    /**
     * Holds analytics result data
     */
    public static class AnalyticsResult {
        public int surveyId;
        public int respondentCount;
        public double overallScore;
        public Map<String, Double> averageRatings = new LinkedHashMap<>();
        public Map<String, Map<String, Integer>> choiceDistribution = new LinkedHashMap<>();
        public List<String> textResponses = new ArrayList<>();
    }
}
