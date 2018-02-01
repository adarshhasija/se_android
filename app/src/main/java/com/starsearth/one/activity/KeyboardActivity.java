package com.starsearth.one.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.starsearth.one.R;
import com.starsearth.one.activity.auth.LoginActivity;

public class KeyboardActivity extends AppCompatActivity implements View.OnKeyListener, View.OnTouchListener {

    public static String LOG_TAG = "KeyboardActivity";

    private TextView tvInstruction;
    private TextView tvKey;
    private EditText etFake;
    private Button btnContinueToLoginScreen;

    private boolean hardwareKeyboard = false;

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    private void onRightToLeft() {
        goToLoginScreen();
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(KeyboardActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Full screen
      /*  getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );  */
        setContentView(R.layout.activity_keyboard);

     /*   final InputManager im = (InputManager) getSystemService(Context.INPUT_SERVICE);
        //im.getInputDeviceIds();
        im.registerInputDeviceListener(new InputManager.InputDeviceListener() {
            @Override
            public void onInputDeviceAdded(int deviceId) {
                InputDevice device = im.getInputDevice(deviceId);
                Toast.makeText(KeyboardActivity.this, "Input device added "+Integer.toString(device.getKeyboardType()), Toast.LENGTH_SHORT).show();
                Log.d("Input", "InputDeviceAdded: " + deviceId);
                tvInstruction.setText(getString(R.string.press_any_key));
                etFake.setVisibility(View.VISIBLE);
                btnContinueToLoginScreen.setVisibility(View.VISIBLE);
                hardwareKeyboard=true;
            }

            @Override
            public void onInputDeviceRemoved(int deviceId) {
                Toast.makeText(KeyboardActivity.this, "Input device removed "+Integer.toString(deviceId), Toast.LENGTH_SHORT).show();
                Log.d("Input", "InputDeviceRemoved: " + deviceId);
                tvInstruction.setText(getString(R.string.keyboard_removed));
                etFake.setVisibility(View.GONE);
                btnContinueToLoginScreen.setVisibility(View.GONE);
                hardwareKeyboard=false;
            }

            @Override
            public void onInputDeviceChanged(int deviceId) {
                Log.d("Input", "InputDeviceChanged: " + deviceId);
                Toast.makeText(KeyboardActivity.this, "device changed", Toast.LENGTH_SHORT).show();
            }
        }, null);   */

        tvInstruction = (TextView) findViewById(R.id.tv_instruction);
        tvKey = (TextView) findViewById(R.id.tv_key);
        tvInstruction.setOnKeyListener(this);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "config changed + "+newConfig.keyboard, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        hardwareKeyboard = true;
        setResult(RESULT_OK);
        switch (keyCode) {
            //letters
         /*   case KeyEvent.KEYCODE_A:
                tvInstruction.announceForAccessibility(String.valueOf((char) keyCode));
                break;
            case KeyEvent.KEYCODE_B:
                chatBot.playAudio("Letter B");
                break;
            case KeyEvent.KEYCODE_C:
                chatBot.playAudio("Letter C");
                break;
            case KeyEvent.KEYCODE_D:
                chatBot.playAudio("Letter D");
                break;
            case KeyEvent.KEYCODE_E:
                chatBot.playAudio("Letter E");
                break;
            case KeyEvent.KEYCODE_F:
                chatBot.playAudio("Letter F");
                break;
            case KeyEvent.KEYCODE_G:
                chatBot.playAudio("Letter G");
                break;
            case KeyEvent.KEYCODE_H:
                chatBot.playAudio("Letter H");
                break;
            case KeyEvent.KEYCODE_I:
                chatBot.playAudio("Letter I");
                break;
            case KeyEvent.KEYCODE_J:
                chatBot.playAudio("Letter J");
                break;
            case KeyEvent.KEYCODE_K:
                chatBot.playAudio("Letter K");
                break;
            case KeyEvent.KEYCODE_L:
                chatBot.playAudio("Letter L");
                break;
            case KeyEvent.KEYCODE_M:
                chatBot.playAudio("Letter M");
                break;
            case KeyEvent.KEYCODE_N:
                chatBot.playAudio("Letter N");
                break;
            case KeyEvent.KEYCODE_O:
                chatBot.playAudio("Letter O");
                break;
            case KeyEvent.KEYCODE_P:
                chatBot.playAudio("Letter P");
                break;
            case KeyEvent.KEYCODE_Q:
                chatBot.playAudio("Letter Q");
                break;
            case KeyEvent.KEYCODE_R:
                chatBot.playAudio("Letter R");
                break;
            case KeyEvent.KEYCODE_S:
                chatBot.playAudio("Letter S");
                break;
            case KeyEvent.KEYCODE_T:
                chatBot.playAudio("Letter T");
                break;
            case KeyEvent.KEYCODE_U:
                chatBot.playAudio("Letter U");
                break;
            case KeyEvent.KEYCODE_V:
                chatBot.playAudio("Letter V");
                break;
            case KeyEvent.KEYCODE_W:
                chatBot.playAudio("Letter W");
                break;
            case KeyEvent.KEYCODE_X:
                chatBot.playAudio("Letter X");
                break;
            case KeyEvent.KEYCODE_Y:
                chatBot.playAudio("Letter Y");
                break;
            case KeyEvent.KEYCODE_Z:
                chatBot.playAudio("Letter Z");
                break;

            //numbers
            case KeyEvent.KEYCODE_1:
                chatBot.playAudio("Number 1");
                break;
            case KeyEvent.KEYCODE_2:
                chatBot.playAudio("Number 2");
                break;
            case KeyEvent.KEYCODE_3:
                chatBot.playAudio("Number 3");
                break;
            case KeyEvent.KEYCODE_4:
                chatBot.playAudio("Number 4");
                break;
            case KeyEvent.KEYCODE_5:
                chatBot.playAudio("Number 5");
                break;
            case KeyEvent.KEYCODE_6:
                chatBot.playAudio("Number 6");
                break;
            case KeyEvent.KEYCODE_7:
                chatBot.playAudio("Number 7");
                break;
            case KeyEvent.KEYCODE_8:
                chatBot.playAudio("Number 8");
                break;
            case KeyEvent.KEYCODE_9:
                chatBot.playAudio("Number 9");
                break;
            case KeyEvent.KEYCODE_0:
                chatBot.playAudio("Number 0");
                break;  */


            //F1-F9
            case KeyEvent.KEYCODE_F1:
                tvInstruction.announceForAccessibility("F1");
                tvKey.setText("F1");
                break;
            case KeyEvent.KEYCODE_F2:
                tvInstruction.announceForAccessibility("F2");
                tvKey.setText("F2");
                break;
            case KeyEvent.KEYCODE_F3:
                tvInstruction.announceForAccessibility("F3");
                tvKey.setText("F3");
                break;
            case KeyEvent.KEYCODE_F4:
                tvInstruction.announceForAccessibility("F4");
                tvKey.setText("F4");
                break;
            case KeyEvent.KEYCODE_F5:
                tvInstruction.announceForAccessibility("F5");
                tvKey.setText("F5");
                break;
            case KeyEvent.KEYCODE_F6:
                tvInstruction.announceForAccessibility("F6");
                tvKey.setText("F6");
                break;
            case KeyEvent.KEYCODE_F7:
                tvInstruction.announceForAccessibility("F7");
                tvKey.setText("F7");
                break;
            case KeyEvent.KEYCODE_F8:
                tvInstruction.announceForAccessibility("F8");
                tvKey.setText("F8");
                break;
            case KeyEvent.KEYCODE_F9:
                tvInstruction.announceForAccessibility("F9");
                tvKey.setText("F9");
                break;
            case KeyEvent.KEYCODE_F10:
                tvInstruction.announceForAccessibility("F10");
                tvKey.setText("F10");
                break;
            case KeyEvent.KEYCODE_F11:
                tvInstruction.announceForAccessibility("F11");
                tvKey.setText("F11");
                break;
            case KeyEvent.KEYCODE_F12:
                tvInstruction.announceForAccessibility("F12");
                tvKey.setText("F12");
                break;


            //arrow keys
            case KeyEvent.KEYCODE_DPAD_UP:
                tvInstruction.announceForAccessibility("Up");
                tvKey.setText("Up");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                tvInstruction.announceForAccessibility("Down");
                tvKey.setText("Down");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                tvInstruction.announceForAccessibility("Left");
                tvKey.setText("Left");
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                tvInstruction.announceForAccessibility("Right");
                tvKey.setText("Right");
                break;


            //other buttons
            case KeyEvent.KEYCODE_SPACE:
                tvInstruction.announceForAccessibility("Space");
                tvKey.setText("Space");
                break;
            case KeyEvent.KEYCODE_ENTER:
                tvInstruction.announceForAccessibility("Enter");
                tvKey.setText("Enter");
                break;
            case KeyEvent.KEYCODE_BACK:
                tvInstruction.announceForAccessibility("Backspace");
                tvKey.setText("Backspace");
                break;
            case KeyEvent.KEYCODE_ESCAPE:
                tvInstruction.announceForAccessibility("Escape "+ getResources().getString(R.string.escape_key_description));
                tvKey.setText("Escape "+ getResources().getString(R.string.escape_key_description));
                break;
            case KeyEvent.KEYCODE_SYSRQ:
                tvInstruction.announceForAccessibility("Print Screen. "+ getResources().getString(R.string.print_screen_description));
                tvKey.setText("Print Screen. "+getResources().getString(R.string.print_screen_description));
            case KeyEvent.KEYCODE_CAPS_LOCK:
                tvInstruction.announceForAccessibility("Caps Lock");
                tvKey.setText("Caps Lock");
                break;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
                tvInstruction.announceForAccessibility("Left Shift");
                tvKey.setText("Left Shift");
                break;
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                tvInstruction.announceForAccessibility("Right Shift");
                tvKey.setText("Right Shift");
                break;
            case KeyEvent.KEYCODE_TAB:
                tvInstruction.announceForAccessibility("Tab");
                tvKey.setText("Tab");
                break;
            case KeyEvent.KEYCODE_CTRL_LEFT:
                tvInstruction.announceForAccessibility("Left control");
                tvKey.setText("Left control");
                break;
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                tvInstruction.announceForAccessibility("Right control");
                tvKey.setText("Right control");
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
                tvInstruction.announceForAccessibility("Left alt");
                tvKey.setText("Left alt");
                break;
            case KeyEvent.KEYCODE_ALT_RIGHT:
                tvInstruction.announceForAccessibility("Right alt");
                tvKey.setText("Right alt");
                break;
            case KeyEvent.KEYCODE_PAGE_UP:
                tvInstruction.announceForAccessibility("Page up");
                tvKey.setText("Page up");
                break;
            case KeyEvent.KEYCODE_PAGE_DOWN:
                tvInstruction.announceForAccessibility("Page down");
                tvKey.setText("Page down");
                break;
         /*   case KeyEvent.KEYCODE_LEFT_BRACKET:
                chatBot.playAudio("Left bracket");
                break;
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
                chatBot.playAudio("Right bracket");
                break;
            case KeyEvent.KEYCODE_SLASH:
                chatBot.playAudio("Slash button");
                break;
            case KeyEvent.KEYCODE_PLUS:
                chatBot.playAudio("Plus");
                break;
            case KeyEvent.KEYCODE_MINUS:
                chatBot.playAudio("Minus");
                break;
            case KeyEvent.KEYCODE_COMMA:
                chatBot.playAudio("Comma");
                break;
            case KeyEvent.KEYCODE_PERIOD:
                chatBot.playAudio("Full stop");
                break;
            case KeyEvent.KEYCODE_FUNCTION:
                chatBot.playAudio("Function button");
                break;
            case KeyEvent.KEYCODE_SEMICOLON:
                chatBot.playAudio("Semicolon button");
                break;
            case KeyEvent.KEYCODE_EQUALS:
                chatBot.playAudio("Equals button");
                break;
            case KeyEvent.KEYCODE_BACKSLASH:
                chatBot.playAudio("Backslash button");
                break;  */
            case KeyEvent.KEYCODE_INSERT:
                tvInstruction.announceForAccessibility("Insert");
                tvKey.setText("Insert");
                break;
            case KeyEvent.KEYCODE_DEL:
                tvInstruction.announceForAccessibility("Delete");
                tvKey.setText("Delete");
                break;
            default:
                String key = Character.toString((char) event.getUnicodeChar());
                tvInstruction.announceForAccessibility(key);
                tvKey.setText(key);
                break;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hardwareKeyboard) {
            setResult(RESULT_OK);
        }
        else {
            setResult(RESULT_CANCELED);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
