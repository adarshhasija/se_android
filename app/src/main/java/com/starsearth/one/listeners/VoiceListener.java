package com.starsearth.one.listeners;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

/**
 * Created by faimac on 11/28/16.
 */

public class VoiceListener implements RecognitionListener {

    private BotResponseListener listener;

    public VoiceListener(BotResponseListener listener) {
        this.listener = listener;
    }

    public boolean isTextDefined(String text) {
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
        if (data != null && !data.isEmpty()) {
            String text = data.get(0).toString();
            if (isTextDefined(text)) {
                listener.processUserVoiceInput(text);
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
