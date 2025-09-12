package com.quiz.quiz_app;

import java.util.List;

public class TriviaResponse {
    public List<Result> results;

    public static class Result {
        public String question;
        public String correct_answer;
        public List<String> incorrect_answers;
    }
}
