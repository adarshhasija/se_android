package com.adarshhasija.starsearth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

public class MainTalkActivity extends AppCompatActivity  implements View.OnClickListener, View.OnLongClickListener{

    public static String LOG_TAG = "MainTalkActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final String APIAI_ACCESS_TOKEN = "81cc2b49f298485eaa3820a55071e422";
    private AIDataService aiDataService;
    private String lastBotResponse;
    private FirebaseAnalytics mFirebaseAnalytics;

    private RelativeLayout rlMainView;
    private TextView tvUserInput;
    private TextView tvBotResponse;
    private TextView tvRepeatResponseLabel;


    private TextToSpeech textToSpeech=null;
    private void setupTextToSpeech() {
        textToSpeech=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            textToSpeech.setLanguage(Locale.US);
                        }
                    }
                });
    }
    private void playAudio(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
        }
        else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private SpeechRecognizer speechRecognizer=null;
    private void setupSpeechToText() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new VoiceListener());
    }

    private void setupApiai() {
        final AIConfiguration config = new AIConfiguration(APIAI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(this, config);
    }

    public boolean isTalkbackOn() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        //Log.d(LOG_TAG, "************ACCESSIBILITY ENABLED: "+am.isEnabled());
        //Log.d(LOG_TAG, "*************TOUCH EXPLORATION ENABLED :"+am.isTouchExplorationEnabled());
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_talk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rlMainView = (RelativeLayout) findViewById(R.id.rlMainView);
        tvUserInput = (TextView) findViewById(R.id.tvUserInput);
        tvBotResponse = (TextView) findViewById(R.id.tvBotResponse);
        tvRepeatResponseLabel = (TextView) findViewById(R.id.tvRepeatResponse);

        setupSpeechToText();
        setupTextToSpeech();
        setupApiai();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }
        rlMainView.setOnClickListener(this);
        rlMainView.setOnLongClickListener(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        rlMainView.setOnClickListener(null);
        rlMainView.setOnLongClickListener(null);
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(R.string.no_internet);
        alertDialog.setNeutralButton(android.R.string.ok, null);
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.show();
    }

    private void stateStartTalking() {
        tvUserInput.setText(R.string.talk_prompt);
        tvBotResponse.setVisibility(View.GONE);
        tvRepeatResponseLabel.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (isNetworkAvailable()) {
            vibrate(500);
            stateStartTalking();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizer.startListening(intent);
        }
        else {
            showNoInternetDialog();
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if (lastBotResponse != null && !lastBotResponse.isEmpty()) {
            setBotResponse(lastBotResponse);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, Integer.toString(lastBotResponse.length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content-length");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        else {
            //Vibration when there is nothing to respond
            vibrate(500);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, "0");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content-length");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        return true;
    }

    public void vibrate(long timeMillis) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(timeMillis);
    }

    private void apiaiQuery(String text) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, text);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "voice");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(text);

        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                    Log.d(LOG_TAG, "*******ERROR: "+e.getMessage());
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    apiaiProcessResponse(aiResponse);

                }
            }
        }.execute(aiRequest);
    }

    private void apiaiProcessResponse(AIResponse response) {
        Result result = response.getResult();
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
        }
    }

    private void userQuery(String text) {
        setUserInput(text);
        apiaiQuery(text);
    }

    private void setUserInput(String text) {
        TextView tvUserInput = (TextView) findViewById(R.id.tvUserInput);
        tvUserInput.setText(text);
    }

    private void setBotResponse(String text) {
        tvBotResponse.setText(text);
        tvBotResponse.setVisibility(View.VISIBLE);
        tvRepeatResponseLabel.setVisibility(View.VISIBLE);
        playAudio(text);
        lastBotResponse = text;
    }

    private void getSignLanguage(String item, String type) {
        getSignLanguageDescription(item, type);
    }

    private DatabaseReference getFirebaseDatabaseReference(String reference) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(reference);
        return ref;
    }

    private Query getQuerySignLanguage(String item) {
        DatabaseReference ref = getFirebaseDatabaseReference("SignLanguage");
        Query query = ref.orderByChild("item").equalTo(item);
        return query;
    }

    private void getSignLanguageDescription(String item, final String type) {
        Query query = getQuerySignLanguage(item);
        query.addValueEventListener(new ValueEventListener() {
            boolean matchFound = false;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                while (children.iterator().hasNext()) {
                    DataSnapshot child = children.iterator().next();
                    HashMap<String, String> data = (HashMap<String, String>) child.getValue();
                    String dataType = data.get("type");
                    if (dataType.equals(type)) {
                        setBotResponse(data.get("description"));
                        matchFound = true;
                    }
                }
                if (!matchFound) {
                    setBotResponse("Sorry, we do not have the "+type+" for this");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    public class VoiceListener implements RecognitionListener {


        private boolean isTextDefined(String text) {
            return text != null && text.length() > 0;
        }

        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onResults(Bundle results) {
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = data.get(0).toString();
            if (isTextDefined(text)) {
                userQuery(text);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            userQuery("Help");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
