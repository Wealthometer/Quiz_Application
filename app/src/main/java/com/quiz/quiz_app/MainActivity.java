package com.quiz.quiz_app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView questionTextView;
    TextView totalQuestionTextView;
    Button ansA, ansB, ansC, ansD;
    Button btn_submit;

    int score = 0;
    int currentQuestionIndex = 0;
    String selectedAnswer = "";

    List<TriviaResponse.Result> questions = new ArrayList<>();
    List<String> currentChoices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalQuestionTextView = findViewById(R.id.total_question);
        questionTextView = findViewById(R.id.question);
        ansA = findViewById(R.id.ans_a);
        ansB = findViewById(R.id.ans_b);
        ansC = findViewById(R.id.ans_c);
        ansD = findViewById(R.id.ans_d);
        btn_submit = findViewById(R.id.btn_submit);

        ansA.setOnClickListener(this);
        ansB.setOnClickListener(this);
        ansC.setOnClickListener(this);
        ansD.setOnClickListener(this);
        btn_submit.setOnClickListener(this);

        fetchQuestionsFromApi();
    }

    private void fetchQuestionsFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TriviaApi triviaApi = retrofit.create(TriviaApi.class);

        triviaApi.getQuestions(5).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TriviaResponse> call, @NonNull Response<TriviaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questions = response.body().results;
                    totalQuestionTextView.setText(getString(R.string.total_questions_text, questions.size()));
                    loadNewQuestion();
                } else {
                    loadMockQuestions(); // fallback if response is empty
                }
            }

            @Override
            public void onFailure(@NonNull Call<TriviaResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch questions: " + t.getMessage());
                loadMockQuestions(); // fallback if network fails
            }
        });

    }

    private void loadMockQuestions() {
        questions.clear();

        for (int i = 0; i < QuestionAnswer.question.length; i++) {
            TriviaResponse.Result q = new TriviaResponse.Result();
            q.question = QuestionAnswer.question[i];
            q.correct_answer = QuestionAnswer.correctAnswers[i];
            q.incorrect_answers = new ArrayList<>();
            for (String choice : QuestionAnswer.choices[i]) {
                if (!choice.equals(QuestionAnswer.correctAnswers[i])) {
                    q.incorrect_answers.add(choice);
                }
            }
            questions.add(q);
        }

        totalQuestionTextView.setText(getString(R.string.total_questions_text, questions.size()));
        loadNewQuestion();
    }

    private void loadNewQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        TriviaResponse.Result currentQ = questions.get(currentQuestionIndex);

        questionTextView.setText(Html.fromHtml(currentQ.question, Html.FROM_HTML_MODE_LEGACY));

        currentChoices.clear();
        currentChoices.addAll(currentQ.incorrect_answers);
        currentChoices.add(currentQ.correct_answer);
        Collections.shuffle(currentChoices);

        ansA.setText(currentChoices.get(0));
        ansB.setText(currentChoices.get(1));
        ansC.setText(currentChoices.get(2));
        ansD.setText(currentChoices.get(3));

        selectedAnswer = "";
    }

    private void finishQuiz() {
        String passStatus = (score >= questions.size() * 0.6) ? "Passed" : "Failed";

        new AlertDialog.Builder(this)
                .setTitle(passStatus)
                .setMessage(getString(R.string.score_message, score, questions.size()))
                .setPositiveButton("Restart", (dialog, i) -> restartQuiz())
                .setCancelable(false)
                .show();
    }

    private void restartQuiz() {
        score = 0;
        currentQuestionIndex = 0;
        fetchQuestionsFromApi();
    }

    @Override
    public void onClick(View view) {
        ansA.setBackgroundColor(Color.WHITE);
        ansB.setBackgroundColor(Color.WHITE);
        ansC.setBackgroundColor(Color.WHITE);
        ansD.setBackgroundColor(Color.WHITE);

        Button clickedButton = (Button) view;

        if (clickedButton.getId() == R.id.btn_submit) {
            if (!selectedAnswer.isEmpty()) {
                TriviaResponse.Result currentQ = questions.get(currentQuestionIndex);
                if (selectedAnswer.equals(currentQ.correct_answer)) {
                    score++;
                }
                currentQuestionIndex++;
                loadNewQuestion();
            }
        } else {
            selectedAnswer = clickedButton.getText().toString();
            clickedButton.setBackgroundColor(Color.YELLOW);
        }
    }
}
