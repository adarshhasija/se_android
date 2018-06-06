package com.starsearth.one.ai;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.starsearth.one.listeners.BotResponseListener;
import com.starsearth.one.domain.SignLanguage;
import com.google.gson.JsonElement;

import java.util.HashMap;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

/**
 * Created by faimac on 11/24/16.
 */

public class ApiAi {

    public static String LOG_TAG = "API-AI";

    private final String APIAI_ACCESS_TOKEN = "81cc2b49f298485eaa3820a55071e422";
    private AIDataService aiDataService;
    private BotResponseListener listener;
    private AiAsyncTask asyncTask;

    public ApiAi(Context context, BotResponseListener listener) {
        this.listener = listener;

        final AIConfiguration config = new AIConfiguration(APIAI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(context, config);
    }

    public void send(String text) {
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(text);
        asyncTask = new AiAsyncTask();
        asyncTask.execute(aiRequest);

      /*  new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    processResponse(aiResponse);

                }
            }
        }.execute(aiRequest);   */
    }

    public void cancel() {
        asyncTask.cancel(true);
    }

    private void processResponse(AIResponse response) {
        if (SignLanguage.isSignLanguage(response)) {
            if (!SignLanguage.isActionIncomplete(response)) {
                getSignLanguage(response);
            }
            else {
                sendResponse("You did not mention any letter or word");
            }
        }
        else if (isFulfillmentSpeechPresent(response)) {
            Result result = response.getResult();
            Fulfillment fulfillment = result.getFulfillment();
            String speech = fulfillment.getSpeech();
            sendResponse(speech);
        }

      /*  Result result = response.getResult();
        String action = result.getAction();
        Fulfillment fulfillment = result.getFulfillment();
        String speech = fulfillment.getSpeech();
        if (action != null && action.equals("getSignLanguage")) {
            if (speech != null) {
                setBotResponse(speech);
            }
            boolean actionIncomplete = result.isActionIncomplete();
            HashMap<String, JsonElement> parameters = result.getParameters();
            if (!actionIncomplete && parameters != null && !parameters.isEmpty()) {
                String islSign = parameters.get("islSign").isJsonNull() ? "" : parameters.get("islSign").getAsString();
                String type = parameters.get("signLanguageType").isJsonNull() ? "ISL" : parameters.get("signLanguageType").getAsString();
                islSign = islSign.toUpperCase();
                getSignLanguage(islSign, type);
            }
        }
        else if (speech != null) {
            setBotResponse(speech);
        }   */
    }

    private void getSignLanguage(AIResponse response) {
        Result result = response.getResult();
        HashMap<String, JsonElement> parameters = result.getParameters();
        String islSign = parameters.get("islSign").isJsonNull() ? "" : parameters.get("islSign").getAsString();
        String type = parameters.get("signLanguageType").isJsonNull() ? "ISL" : parameters.get("signLanguageType").getAsString();
        islSign = islSign.toUpperCase();
        SignLanguage signLanguage = new SignLanguage(islSign, type, listener);
        signLanguage.get();
    }

    private boolean isFulfillmentSpeechPresent(AIResponse response) {
        Result result = response.getResult();
        Fulfillment fulfillment = result.getFulfillment();
        String speech = fulfillment.getSpeech();
        return speech != null && !speech.isEmpty();
    }

    private void sendResponse(String speech) {
        listener.processBotResponse(speech);
    }


    public class AiAsyncTask extends AsyncTask<AIRequest, Void, AIResponse> {

        @Override
        protected AIResponse doInBackground(AIRequest... requests) {
            final AIRequest request = requests[0];
            try {
                final AIResponse response = aiDataService.request(request);
                return response;
            } catch (AIServiceException e) {
                Log.d(LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(AIResponse response) {
            super.onPostExecute(response);
            if (response != null) {
                processResponse(response);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            listener.onAICancelled();
        }
    }
}
