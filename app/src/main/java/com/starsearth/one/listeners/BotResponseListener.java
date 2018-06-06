package com.starsearth.one.listeners;

import com.google.firebase.storage.StorageReference;

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
