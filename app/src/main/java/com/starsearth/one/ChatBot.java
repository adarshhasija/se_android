package com.starsearth.one;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import com.starsearth.one.listener.BotResponseListener;
import com.starsearth.one.listener.VoiceListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by faimac on 11/28/16.
 */

public class ChatBot {

    private Context context;
    private BotResponseListener listener;

    public ChatBot(Context context) {
        this.context = context;
        setupSpeechToText();
        setupTextToSpeech();
    }

    public ChatBot(Context context, BotResponseListener listener) {
        this.context = context;
        this.listener = listener;
        setupSpeechToText();
        setupTextToSpeech();
    }

    private TextToSpeech textToSpeech=null;
    private void setupTextToSpeech() {
        textToSpeech=new TextToSpeech(context,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            textToSpeech.setLanguage(Locale.US);
                        }
                    }
                });
    }
    //Play the audio
    public void playAudio(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "0");
        }
        else {
            //textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            //textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
            ttsUnder20(text);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private SpeechRecognizer speechRecognizer=null;
    public void setupSpeechToText() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new VoiceListener(listener));
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (speechRecognizer != null) {
            speechRecognizer.startListening(intent);
        }
    }

    public void cancelListening() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
    }

    public void stopTalking() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }
}
