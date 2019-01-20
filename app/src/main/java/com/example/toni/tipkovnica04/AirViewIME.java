package com.example.toni.tipkovnica04;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

// Custom IME (Input Method)
// Tipkovnica kao "regularni servis" koji ce se vizualizirati unutar svake aplikacije i
// odgovarajuceg slucaja koristenja kod kojeg se zahtijeva unos teksta.
public class AirViewIME extends InputMethodService
        /*implements KeyboardView.OnKeyboardActionListener*/ {

    Context context;
    LayoutInflater inflater;
    KeyboardLayout layout;
    boolean LOWERCASE = true;                   // character case (lower/upper => true/false)
    boolean START = false;
    float KEYBOARD_HEIGHT = 0.285f;             // velicina tipkovnice - deafult
    float SCALE_FACTOR_SINGLE_BUTTON = 0.2f;   // skalirani (uvecani) button - default
    float SCALE_FACTOR_SINGLE_ROW = 0.40f;      // skalirani (uvecani) redak - deafult

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        inflater = this.getLayoutInflater();

        // Sprjecavanje zatamnjivanja ekrana (dimming) nakon odredjenog perioda neaktivnosti:
        if (getWindow().getWindow() != null)
        {
            getWindow().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Sva aplikacijska logika tipkovnice smjestena je u razred koji opisuje/definira
        // izgled i ponasanje te tipkovnice (KeyboardLayout.java).

        // Instanciranje objekta iz ovoga razreda mozemo napraviti u metodi onWindowShown:
        // @Android API:
        // onWindowShown -- Called when the input method window has been shown to the user,
        // after previously not being visible.
        // [Napomena: pogledati i zivotni ciklus IME-a za vazne metode u tom ciklusu]:
        // https://developer.android.com/guide/topics/text/creating-input-method.html
    }

    @Override
    public void onWindowShown() {

        enableKeyboard();
    }

    public void onStartClicked() {

    }

    // Instanciranje layout-a tipkovnice sa svim potrebnim postavkama
    // i konkretno apliciranje tih postavki
    public void enableKeyboard(){
        // Ucitavanje aktualnih postavki tipkovnice iz Shared Preferences:
        loadSettingsFromSP();

        // Instranciranje layouta tipkovnice:
        layout = new KeyboardLayout(inflater, context,
                this.getCurrentInputConnection(),
                this.getImeAction(this.getCurrentInputEditorInfo().imeOptions),
                KEYBOARD_HEIGHT, LOWERCASE, SCALE_FACTOR_SINGLE_BUTTON, SCALE_FACTOR_SINGLE_ROW, START);

        // Apliciranje custom tipkovnice:
        setInputView(layout);
        updateInputViewShown();
    }


    // Ucitavanje postavki tipkovnice iz SharedPreferences:
    public void loadSettingsFromSP(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        LOWERCASE = sp.getBoolean("lettercase", true);
        KEYBOARD_HEIGHT = Float.valueOf(sp.getString("keyboard_height_percentage", "0.285"));
        SCALE_FACTOR_SINGLE_BUTTON = Float.valueOf(sp.getString("enlarge_key_percentage", "0.2"));
        SCALE_FACTOR_SINGLE_ROW = Float.valueOf(sp.getString("enlarge_row_percentage", "0.40"));

        System.out.println("***** KB_SCALE_FACTOR = " + KEYBOARD_HEIGHT);
    }



    // Dohvat IME akcije za doticni editor na kojeg se unosna metoda trenutno odnosi.
    // Korisno za implementaciju RETURN (ENTER) tipke koja moze imati razlicite ucinke
    // za razlicite editore (ovisno o vrsti aplikacije).
    // Primjerice, u Viberu bi to mogao biti Send, u Google pretrazivacu "Search", za neki
    // obrazac s vise EditText elemenata - "Next", itd.
    private int getImeAction(int imeAction) {
        int action;
        switch (imeAction & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_DONE:
                action = EditorInfo.IME_ACTION_DONE;
                break;
            case EditorInfo.IME_ACTION_GO:
                action = EditorInfo.IME_ACTION_GO;
                break;
            case EditorInfo.IME_ACTION_NEXT:
                action = EditorInfo.IME_ACTION_NEXT;
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                action = EditorInfo.IME_ACTION_SEARCH;
                break;
            case EditorInfo.IME_ACTION_SEND:
                action = EditorInfo.IME_ACTION_SEND;
                break;
            case EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS:
                enableKeyboard();
            default:
                action = EditorInfo.IME_ACTION_UNSPECIFIED;
                break;
        }
        return action;
    }

    @Override
    public EditorInfo getCurrentInputEditorInfo() {
        return super.getCurrentInputEditorInfo();
    }

    // @Android API:
    // Called to inform the input method that text input has started in an editor.
    // You should use this callback to initialize the state of your input to match the
    // state of the editor given to it.
    @Override
    public void onStartInput (EditorInfo attribute, boolean restarting){
        // Azuriranje input connection objekta, tako da se input stream
        // moze aplicirati prilikom (re)starta bilo kojeg editora u bilo kojoj aplikaciji.
        // Primjerice, bez ove metode bi u Viber-u mogli poslati neku poruku, no nakon
        // slanja, novi tekst se ne bi aplicirao u Viber editoru...
        if (layout != null) {
            layout.setInputConnection(this.getCurrentInputConnection());
            int imeAction = this.getImeAction(attribute.imeOptions);
            layout.setIMEaction(imeAction);

        }
    }


    // Promjena konfiguracije (tipicno: promjena orijentacije uredjaja)
    // "re-inicijalizacija" tipkovnice:
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableKeyboard();
    }


    @Override
    public void onFinishInputView(boolean finishingInput) {
        // Oslobadjanje resursa, npr. ako se koriste neki listeneri, timeri i slicno...
    }


/*
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }
*/

}