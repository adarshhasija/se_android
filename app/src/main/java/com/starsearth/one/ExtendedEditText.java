package com.starsearth.one;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.starsearth.one.listener.BotResponseListener;

/**
 * Created by faimac on 11/29/16.
 */

public class ExtendedEditText extends EditText {

    private BotResponseListener botResponseListener;

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            botResponseListener.onKeyboardClosed();
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            botResponseListener.onEnterPressed();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setBotResponseListener(BotResponseListener listener) {
        this.botResponseListener = listener;
    }
}
