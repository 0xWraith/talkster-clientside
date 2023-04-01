package com.client.talkster.interfaces;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public interface IAPIResponseHandler
{
    void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl);
    void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl);
}
