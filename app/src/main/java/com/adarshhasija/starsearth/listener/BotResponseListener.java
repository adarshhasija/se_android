package com.adarshhasija.starsearth.listener;

import com.google.firebase.storage.StorageReference;

import ai.api.model.AIResponse;

/**
 * Created by faimac on 11/24/16.
 */

public interface BotResponseListener {

    void processUserVoiceInput(String text);
    void processBotResponse(String speech);
    void processFireBaseImage(StorageReference storageReference);

    //Soft keyboard events
    void onKeyboardClosed();
    void onEnterPressed();

    //Cancellation
    void onAICancelled();
}
