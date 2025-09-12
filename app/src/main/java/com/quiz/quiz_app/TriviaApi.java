package com.quiz.quiz_app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TriviaApi {
    @GET("api.php")
    Call<TriviaResponse> getQuestions(@Query("amount") int amount);
}
