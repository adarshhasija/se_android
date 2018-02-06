package com.starsearth.one;

/**
 * Created by faimac on 11/28/16.
 */

public class BotStateMachine {

    public enum State {
        IDLE, //Nothing happening
        TALKING, //User is talking to the app
        TYPING, //User is in typing mode
        INPUT_PROCESSING_TALKING, //Processing the speech input. Waiting for reply
        INPUT_PROCESSING_TYPING, //Processing the typing input. Waiting for reply
        VIEWING_BOT_QUESTIONS, //What can I ask? button pressed
    }

    State state;
    String currentUserInput;
    String currentBotTextResponse;

    public BotStateMachine() {
        this.state = State.IDLE;
        this.currentUserInput = null;
        this.currentBotTextResponse = null;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getCurrentUserInput() {
        return currentUserInput;
    }

    public void setCurrentUserInput(String currentUserInput) {
        this.currentUserInput = currentUserInput;
    }

    public String getCurrentBotTextResponse() {
        return currentBotTextResponse;
    }

    public void setCurrentBotTextResponse(String currentBotTextResponse) {
        this.currentBotTextResponse = currentBotTextResponse;
    }
}
