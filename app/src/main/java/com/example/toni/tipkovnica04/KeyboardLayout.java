package com.example.toni.tipkovnica04;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Layout tipkovnice -- izgled i ponasanje
public class KeyboardLayout extends LinearLayout {
    Podatak[] arr;
    AirViewIME a;

    Context context;
    LayoutInflater inflater;
    InputConnection inputConn;
    int currentIMEaction;

    boolean LOWERCASE = true;

    float KEYBOARD_HEIGHT_SCALE_FACTOR = 0.285f;
    float SCALE_FACTOR_SINGLE_BUTTON = 0.25f;
    float SCALE_FACTOR_SINGLE_ROW = 0.40f;

    ArrayList<Button> buttonList; 	// kolekcija svih buttona
    LinearLayout rootLL;			// vrsni layout tipkovnice

    public KeyboardLayout(LayoutInflater inflater, Context ctx,
                          InputConnection inputConn,
                          int currentIMEaction,
                          float kbHeight, boolean lettercase, float scaleButton, float scaleRow){

        super(ctx);
        this.context = ctx;
        this.inflater = inflater;
        this.inputConn = inputConn;
        this.currentIMEaction = currentIMEaction;

        this.KEYBOARD_HEIGHT_SCALE_FACTOR = kbHeight;
        this.LOWERCASE = lettercase;
        this.SCALE_FACTOR_SINGLE_BUTTON = scaleButton;
        this.SCALE_FACTOR_SINGLE_ROW = scaleRow;


        // Postavljanje/aktualiziranje dimenzija tipkovnice (primarno visine pomocu
        // zadanog "tezinskog faktora" u odnosnu na cijelu visinu zaslona):
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        float kbScale = KEYBOARD_HEIGHT_SCALE_FACTOR * dm.heightPixels;
        int availableHeight = (int)kbScale;

        // Ucitavanje layout-a tipkovnice iz odgovarajuceg xml-a:
        inflater.inflate(R.layout.keyboard_layout, this);
        rootLL = this.findViewById(R.id.rootview);

        // Apliciranje visine tipkovnice promjenom parametra za vrsni UI-layout:
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)rootLL.getLayoutParams();
        params.height = availableHeight;
        rootLL.setLayoutParams(params);

        // Instanciranje svih buttona i generiranje odgovarajuce liste:
        create_button_list();

        // Natpis na ENTER buttonu, ovisno o kontekstu:
        //((Button)buttonList.get(30)).setText(get_enter_button_title());
        //((Button)buttonList.get(29)).setText(R.string.button_settings); // force uppercase

        // Regitracija svih potrebnih listenera za sve buttone
        register_button_listeners();

        int stats_cnt = 0;
        String [] stats_arr;
        String stats_row;
        InputStream TxtFileInputStream = getResources().openRawResource(R.raw.stats);
        Scanner stats_txt = new Scanner(TxtFileInputStream);
        stats_txt.useDelimiter("\n");
        arr = new Podatak[567];
        while(stats_txt.hasNextLine()) {
            arr[stats_cnt] = new Podatak();
            stats_row = stats_txt.nextLine();
            stats_arr = stats_row.split("\\s+");
            arr[stats_cnt].setFirst(stats_arr[0]);
            arr[stats_cnt].setSecond(stats_arr[1]);
            arr[stats_cnt].setPercentage(Float.parseFloat(stats_arr[2]));
            stats_cnt++;
        }

    }

    // Instanciranje svih button objekata i generiranje odgovarajuce kolekcije pomocu pretrage
    // hijerarhije UI elemenata:
    private void create_button_list(){

        // Pronadji sve UI elemente u root view-u (to je vrsni LinearLayout u slucaju ove aplikacije):
        List<View> allElements = getAllChildrenBFS(findViewById(R.id.rootview));

        // Filtriranje buttona i dodavanje u odnosnu kolekciju:
        buttonList = new ArrayList<>();

        for (int i = 0; i < allElements.size(); i++) {
            final View iElement = allElements.get(i);
            if (iElement instanceof Button) {
                ((Button)iElement).setPaddingRelative(0, 0, 0, 0);
                ((Button)iElement).setTransformationMethod(null);
                ((Button)iElement).setSoundEffectsEnabled(true);
                buttonList.add((Button)iElement);
            }
        }
    }


    // Pronadji sve View elemente u nekom root View elementu (UI hijerarhija);
    // (koristi se Breadth-first search):
    private List<View> getAllChildrenBFS(View v) {
        List<View> visited = new ArrayList<>();
        List<View> unvisited = new ArrayList<>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);
            if (!(child instanceof ViewGroup)) continue;
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i=0; i<childCount; i++) unvisited.add(group.getChildAt(i));
        }
        return visited;
    }

    // Registracija TOUCH listenera za sve buttone u glavnoj kolekciji:
    private void register_button_listeners(){

        for(Button iButton : buttonList){
            ////// TOUCH listeneri:
            ((View)iButton).setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int action = event.getAction();

                    if (action == MotionEvent.ACTION_DOWN) {
                        return false;
                    }

                    if (action == MotionEvent.ACTION_UP) {
                        // Na event "UP" treba se obaviti konkretno upisivanje slova/znaka u input stream.
                        // Naravno, to ima smisla samo ako je odgovarajuci event ("UP") aktivan za neki button:
                        int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
                        if (insideIndex == 20){ //START
                            resetButtons();
                        }
                        else if (insideIndex == 28){ // DEL
                            // brisanje:
                            inputConn.deleteSurroundingText(1, 0);
                        }
                        else if (insideIndex == 30) { // SPACE
                            // prazan (blank) znak:
                            inputConn.commitText(" ", 1);
                            resetButtons();
                        }
                        else if (insideIndex == 31) { // ENTER
                            // ili prelazak u novi redak ili odgovarajuca akcija editora:
                            if (KeyboardLayout.this.currentIMEaction ==
                                    EditorInfo.IME_ACTION_UNSPECIFIED) {
                                inputConn.commitText("\n", 1);
                            } else {
                                inputConn.performEditorAction(currentIMEaction);
                            }
                            resetButtons();
                        }
                        else if (insideIndex == 29) { // SETINGS
                            // Eksplicitno pozivanje settings-a,
                            // preporucljivo implementirati kada se radi IME!
                            Intent mIntent = new Intent(context, ImePreferences.class);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            KeyboardLayout.this.context.startActivity(mIntent);
                        }
                        else if (insideIndex != -1) { // REGULAR CHAR
                            // slovo:
                            //resetButtons();
                            Button target = buttonList.get(insideIndex);
                            inputConn.commitText(target.getText(), 1);
                            if(LOWERCASE) {
                                scaleButtons(insideIndex);
                            }
                        }

                        // Zvucni feedback
                        if (insideIndex != -1){
                            v.performClick();
                            v.playSoundEffect(SoundEffectConstants.CLICK);
                        }
                    }
                    return false;
                }
            });
        }
    }

    public void resetButtons() {
        LinearLayout.LayoutParams defaultParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.1f);
        for (Button iButton : buttonList) {
            int currIndex = buttonList.indexOf(iButton);
            if ((currIndex > 31)
                    || (currIndex < 0)
                    || (currIndex == 20)
                    || (currIndex == 28)
                    || (currIndex == 29)
                    || (currIndex == 30)
                    || (currIndex == 31)) {
                continue;
            }
            iButton.setLayoutParams(defaultParams);
            iButton.setTextSize(14f);
        }
    }


    // Metoda koja skalira ciljani button, a posljedicno i sve ostale buttone u aktivnom retku,
    // kao i sve retke tipkovnice
    // ovdje sad treba promijenit povećavanje drugih slova
    public void scaleButtons(int targetButton) {
        ArrayList<String> possible_buttons;
        possible_buttons = new ArrayList<>();
        Button mButton = buttonList.get(targetButton);
        int cnt_row1 = 0, cnt_row2 = 0, cnt_row3 = 0;

        // Odredjivanje gdje se tocno (u konfiguraciji tipkovnice) nalazi doticni button:
        // odredjuje se redak u kojem se button nalazi, kao i granicni buttoni u tome retku.
        int lowerBoundButton = 0, upperBoundButton = 31;
        int targetRow = 0;

        float percentage_max = 0.25f;
        for (int i = 0; i < arr.length; i++) {
            if (possible_buttons.size() > 9) break;
            if (mButton.getText().toString().equals(arr[i].getFirst())) {
                System.out.println(mButton.getText().toString());

                if ((arr[i].getPercentage() > percentage_max) && possible_buttons.size() < 9) {
                    possible_buttons.add(arr[i].getSecond());
                }
            }
        }

        for (Button iButton : buttonList) {
            for (int i = 0; i < possible_buttons.size(); i++) {
                if (iButton.getText().toString().equals(possible_buttons.get(i))) {
                    if (buttonList.indexOf(iButton) <= 9) { cnt_row1++; }
                    else if (buttonList.indexOf(iButton) <= 19) { cnt_row2++; }
                    else if (buttonList.indexOf(iButton) <= 28) { cnt_row3++; }
                }
            }
        }

        // Racunaju tezinskih faktora za buttone u svakom retku.
        // Podsjetnik: inicijalni tezinski faktor za "obican" button (slovo) jest 0.1.
        float scalefactorOthers1 = 0f, scalefactorOthers2 = 0f, scalefactorOthers3 = 0f;

        scalefactorOthers1 = (1f - cnt_row1*SCALE_FACTOR_SINGLE_BUTTON) / (10-cnt_row1);
        scalefactorOthers2 = (1f - cnt_row2*SCALE_FACTOR_SINGLE_BUTTON) / (10-cnt_row2);
        scalefactorOthers3 = (1f - 0.3f - cnt_row3*SCALE_FACTOR_SINGLE_BUTTON) / (7-cnt_row3);

        // Postav novih tezinskih faktora za buttone:::
        // Button koji ce se povecati:
        LinearLayout.LayoutParams paramsEnlargeButton = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                SCALE_FACTOR_SINGLE_BUTTON);

        // Buttoni koji ce se smanjiti:
        LinearLayout.LayoutParams paramsShrinkButton1 = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                scalefactorOthers1);
        LinearLayout.LayoutParams paramsShrinkButton2 = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                scalefactorOthers2);
        LinearLayout.LayoutParams paramsShrinkButton3 = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                scalefactorOthers3);

        // Apliciranje skaliranja na button elemente:

        for (Button iButton : buttonList) {
            // Iz glavne kolekcije buttona ima smisla skalirati samo one koje spadaju u
            // trenutno 'aktivni' redak (gore izracunate granice);
            // dodatno, 'space', 'enter' i 'settings' se nece skalirati u horizontalnom smjeru.
            int currIndex = buttonList.indexOf(iButton);
            if ((currIndex > upperBoundButton)
                    || (currIndex < lowerBoundButton)
                    || (currIndex == 20)
                    || (currIndex == 28)
                    || (currIndex == 29)
                    || (currIndex == 30)
                    || (currIndex == 31)) {
                continue;
            }

            // Ako smo u aktivnom retku, i ako je rijec o buttonu nad kojim je
            // aktivan 'hover/touch--move event', taj doticni button cemo povecati...
            //if (iButton == mButton)
            if (possible_buttons.contains(iButton.getText())) {
                iButton.setLayoutParams(paramsEnlargeButton);
                iButton.setTextSize(24f);
                // ...u protivnom, ako smo u aktivnom retku, a rijec je o buttonu nad kojim nije
                // aktivan hover/touch--move, tada sve takve buttone treba smanjiti:
            } else {
                if (buttonList.indexOf(iButton) <= 9 && cnt_row1 > 0) {
                    iButton.setLayoutParams(paramsShrinkButton1);
                    iButton.setTextSize(12f);
                } if (buttonList.indexOf(iButton) > 9 && buttonList.indexOf(iButton) <= 19 && cnt_row2 > 0) {
                    iButton.setLayoutParams(paramsShrinkButton2);
                    iButton.setTextSize(12f);
                } if (buttonList.indexOf(iButton) > 19 && buttonList.indexOf(iButton) <= 28 && cnt_row3 > 0) {
                    iButton.setLayoutParams(paramsShrinkButton3);
                    iButton.setTextSize(12f);
                }
            }
        }
    }

    // Provjera: nalazi li se tocka trenutnog dodira (na zaslonu) unutar nekog od buttona na
    // tipkovnici?
    // Ulaz: kordinata dodira (pointerX, pointerY)
    // Izlaz: indeks buttona u glavnoj kolekciji (ako sadrzi tu tocku), -1 ako takav button ne postoji
    private int checkInsideButton(int pointerX, int pointerY){
        for (Button iButton : buttonList) {
            int[] loc = new int[2];https://www.360logica.com/blog/how-to-set-path-environmental-variable-for-sdk-in-windows/
            iButton.getLocationOnScreen(loc);
            if (isInside(pointerX, pointerY,
                    loc[0], loc[1],
                    loc[0] + iButton.getWidth(),
                    loc[1] + iButton.getHeight())){
                return buttonList.indexOf(iButton);
            }
        }
        return -1;
    }


    // Pomocna metoda za provjeru polozaja tocke dodira u geometriji buttona;
    // konkretno -> nalazi li se tocka (x, y) u pravokutniku (left, top, right, bottom):
    private boolean isInside(int pointerX, int pointerY,
                             int boundLeft, int boundTop, int boundRight, int boundBottom){
        return ((pointerX >= boundLeft) && (pointerX <= boundRight) &&
                (pointerY >= boundTop) && (pointerY <= boundBottom));
    }


    // Naslov ENTER buttona koji je ovisan o podlezecem Editoru:
    private String get_enter_button_title() {
        switch (currentIMEaction) {
            case EditorInfo.IME_ACTION_DONE:
                return "Done";
            case EditorInfo.IME_ACTION_GO:
                return "Go";
            case EditorInfo.IME_ACTION_NEXT:
                return "Next";
            case EditorInfo.IME_ACTION_SEARCH:
                return "Search";
            case EditorInfo.IME_ACTION_SEND:
                return "Send";
            default:
                return "ENTER";
        }
    }


    // Azuriranje InputConnection objekta ("exposano" za glavni servis [AirViewIME])
    public void setInputConnection(InputConnection ic){
        this.inputConn = ic;
    }

    public void setResetLayout() {

    }

    // Azuriranje IME action-a ("exposano" za glavni servis [AirViewIME])
    public void setIMEaction(int imeAction){
        this.currentIMEaction = imeAction;
        ((Button)buttonList.get(31)).setText(get_enter_button_title());
    }
}