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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


// Layout tipkovnice -- izgled i ponasanje
public class KeyboardLayout extends LinearLayout {

    Context context;
    LayoutInflater inflater;
    InputConnection inputConn;
    int currentIMEaction;

    boolean LOWERCASE = true;
    float KEYBOARD_HEIGHT_SCALE_FACTOR = 0.285f;
    float SCALE_FACTOR_SINGLE_BUTTON = 0.2f;
    float SCALE_FACTOR_DEL_SHRINKED = 0.15f;
    float SCALE_FACTOR_SINGLE_ROW = 0.40f;

    ArrayList<Button> buttonList; 	// kolekcija svih buttona
    LinearLayout rootLL;			// vrsni layout tipkovnice

    public class Podatak {

        public Podatak(String first, String second, float percentage) throws IOException {
            Scanner input = new Scanner(new File("stats.txt"));
            while (input.hasNextLine()) {
                //System.out.println(input.nextLine());
                inputConn.commitText(input.nextLine(), 1);
            }
        }
    }


    public KeyboardLayout(LayoutInflater inflater, Context ctx,
                          InputConnection inputConn,
                          int currentIMEaction,
                          float kbHeight, boolean lettercase, float scaleButton, float scaleRow) {

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
        //int availableWidth = dm.widthPixels;

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
                if (LOWERCASE){
                    ((Button)iElement).setText(((Button)iElement).getText().toString().toLowerCase());
                } else {
                    ((Button)iElement).setText(((Button)iElement).getText().toString().toUpperCase());
                }
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

    // Registracija TOUCH i HOVER listenera za sve buttone u glavnoj kolekciji:
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

                    /*if (action == MotionEvent.ACTION_MOVE) {
                        // Ako postoji "klizanje" po tipkovnici, onda na takvo "klizanje" skaliraj
                        // trenutni prikaz redaka i konkretnog slova:
                        int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
                        // System.out.println("***** Button index found: " + insideIndex);
                        if (insideIndex != -1){
                            scaleButtons(insideIndex);
                        }
                        return false;
                    }*/

                    if (action == MotionEvent.ACTION_UP) {
                        // Na event "UP" treba se obaviti konkretno upisivanje slova/znaka u input stream.
                        // Naravno, to ima smisla samo ako je odgovarajuci event ("UP") aktivan za neki button:
                        int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
                        if (insideIndex == 20){ //START
                            //TU TREBA DODATI DA SE VRATI POČETNI DIZAJN
                        }
                        else if (insideIndex == 28){ // DEL
                            // brisanje:
                            inputConn.deleteSurroundingText(1, 0);
                        }
                        else if (insideIndex == 30) { // SPACE
                            // prazan (blank) znak:
                            inputConn.commitText(" ", 1);
                        }
                        else if (insideIndex == 31) { // ENTER
                            // ili prelazak u novi redak ili odgovarajuca akcija editora:
                            if (KeyboardLayout.this.currentIMEaction ==
                                    EditorInfo.IME_ACTION_UNSPECIFIED) {
                                inputConn.commitText("\n", 1);
                            } else {
                                inputConn.performEditorAction(currentIMEaction);
                            }
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
                            Button target = buttonList.get(insideIndex);
                            inputConn.commitText(target.getText(), 1);
                            scaleButtons(insideIndex);
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

            ////// HOVER listeneri:
            iButton.setOnHoverListener(new View.OnHoverListener() {
                // Skaliranje na validni hover_move event
                @Override
                public boolean onHover(View view, MotionEvent motionEvent) {
                    final int action = motionEvent.getAction();
                    if (action == MotionEvent.ACTION_HOVER_MOVE) {
                        int insideIndex =
                                checkInsideButton((int)motionEvent.getRawX(),
                                        (int)motionEvent.getRawY());

                        if (insideIndex != -1){
                            scaleButtons(insideIndex);
                        }
                        return false;
                    }
                    return false;
                }
            });
        }
    }


    // Metoda koja skalira ciljani button, a posljedicno i sve ostale buttone u aktivnom retku,
    // kao i sve retke tipkovnice
    // ovdje sad treba promijenit povećavanje drugih slova
    public void scaleButtons(int targetButton) {
        Button mButton = buttonList.get(targetButton);

        // Odredjivanje gdje se tocno (u konfiguraciji tipkovnice) nalazi doticni button:
        // odredjuje se redak u kojem se button nalazi, kao i granicni buttoni u tome retku.
        int lowerBoundButton = 0, upperBoundButton = 0;
        int targetRow = 0;

        // Prvi redak --> indeksi [0...9]
        if (targetButton <= 9) {
            lowerBoundButton = 0;
            upperBoundButton = 9;
            targetRow = 1;
        }
        // Drugi redak --> indeksi [10...19]
        else if ((targetButton >= 10) && (targetButton <= 19)) {
            lowerBoundButton = 10;
            upperBoundButton = 19;
            targetRow = 2;
        }
        // Treci redak --> indeksi [20...28]
        else if ((targetButton >= 20) && (targetButton <= 28)) {
            lowerBoundButton = 20;
            upperBoundButton = 28;
            targetRow = 3;
        }
        // Cetvrti redak --> indeksi [29...31]
        else if ((targetButton >= 29) && (targetButton <= 31)) {
            lowerBoundButton = 29;
            upperBoundButton = 31;
            targetRow = 4;
        }

        // Racunaju tezinskih faktora za buttone u svakom retku.
        // Podsjetnik: inicijalni tezinski faktor za "obican" button (slovo) jest 0.1.
        float scalefactorOthers = 0f;
        float scalefactorOthersWhenDelEnlarged = 0f;

        if (targetRow == 1) {
            // Ukupno 10 buttona, bez placeholdera:
            scalefactorOthers = (1f - SCALE_FACTOR_SINGLE_BUTTON) / 9;
        }
        else if (targetRow == 2) {
            // Ukupno 9 buttona i 2 placeholdera s faktorima 0.05:
            scalefactorOthers = (1f - SCALE_FACTOR_SINGLE_BUTTON) / 9;
        }
        else if (targetRow == 3) {
            // Ukupno 7 buttona, START i DEL /default 0.15)
            scalefactorOthers =
                    (1f - SCALE_FACTOR_SINGLE_BUTTON - 0.15f - SCALE_FACTOR_DEL_SHRINKED) / 6;
            scalefactorOthersWhenDelEnlarged = (1f - SCALE_FACTOR_SINGLE_BUTTON - 0.15f) / 7;
        }

        // Apliciranje skaliranja svih redaka:
        //scaleRows(targetRow, SCALE_FACTOR_SINGLE_ROW);

        // Postav novih tezinskih faktora za buttone:::
        // Button koji ce se povecati:
        LinearLayout.LayoutParams paramsEnlargeButton = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                SCALE_FACTOR_SINGLE_BUTTON);

        // Button koji ce se smanjiti:
        LinearLayout.LayoutParams paramsShrinkButton = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                scalefactorOthers);

        // (posebni slucaj): Button koji ce se smanjiti ako je pri tome DEL povecan:
        LinearLayout.LayoutParams paramsShrinkButtonSpecial = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                scalefactorOthersWhenDelEnlarged);

        // Apliciranje skaliranja na button elemente:
        boolean delEnlarged = false;

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
            if (iButton == mButton)
            {
                iButton.setLayoutParams(paramsEnlargeButton);
                iButton.setTextSize(24f);
                if (targetButton == 28) {
                    delEnlarged = true;
                }
                // ...u protivnom, ako smo u aktivnom retku, a rijec je o buttonu nad kojim nije
                // aktivan hover/touch--move, tada sve takve buttone treba smanjiti:
            } else {
                if (!delEnlarged) {
                    // Smanjivanje buttona koji nije DEL:
                    if (iButton != buttonList.get(28)) {
                        iButton.setLayoutParams(paramsShrinkButton);
                        iButton.setTextSize(12f);
                    } else {
                        // Smanjivanje buttona koji *jest* DEL =>
                        // "forsiranje" njegove inicijalne velicine (0.15f):
                        iButton.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                SCALE_FACTOR_DEL_SHRINKED));
                    }
                } else {
                    // DEL button je vec povecan, pa ostale buttone u tom retku treba
                    // skalirati prema geometriji iz "specijalnog slucaja":
                    if (iButton != buttonList.get(28)) {
                        iButton.setLayoutParams(paramsShrinkButtonSpecial);
                        iButton.setTextSize(12f);
                    }
                }
            }
        }
    }


    // Metoda koja skalira ISKLJUCIVO retke (dio koji se ionako mora odraditi prije skaliranja
    // buttona u doticnom retku):
    public void scaleRows(int targetRow, float SCALE_FACTOR_SINGLE_ROW){
        LinearLayout firstLL = rootLL.findViewById(R.id.firstView);
        LinearLayout secondLL = rootLL.findViewById(R.id.secondView);
        LinearLayout thirdLL = rootLL.findViewById(R.id.thirdView);
        LinearLayout fourthLL = rootLL.findViewById(R.id.fourthView);

        // Skaliranje redaka -> izracun i postav novih tezinskih faktora za retke.
        // Podsjetnik: inicijalne visine redaka su 25% visine cijele TIPKOVNICE (tezinski faktor 0.25)
        // Prije skaliranja: 0.25 - 0.25 - 0.25 - 0.25
        // Nakon skaliranja: ovisi o postovakam korisnika
        LinearLayout.LayoutParams paramsEnlargeRow = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                SCALE_FACTOR_SINGLE_ROW);

        LinearLayout.LayoutParams paramsShrinkRow = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                (1f - SCALE_FACTOR_SINGLE_ROW) / 3);

        // Apliciranje novih tezinskih faktora na retke tipkovnice:
        if (targetRow == 1) {
            firstLL.setLayoutParams(paramsEnlargeRow);
            secondLL.setLayoutParams(paramsShrinkRow);
            thirdLL.setLayoutParams(paramsShrinkRow);
            fourthLL.setLayoutParams(paramsShrinkRow);
        } else if (targetRow == 2) {
            secondLL.setLayoutParams(paramsEnlargeRow);
            firstLL.setLayoutParams(paramsShrinkRow);
            thirdLL.setLayoutParams(paramsShrinkRow);
            fourthLL.setLayoutParams(paramsShrinkRow);
        } else if (targetRow == 3) {
            thirdLL.setLayoutParams(paramsEnlargeRow);
            firstLL.setLayoutParams(paramsShrinkRow);
            secondLL.setLayoutParams(paramsShrinkRow);
            fourthLL.setLayoutParams(paramsShrinkRow);
        } else if (targetRow == 4) {
            fourthLL.setLayoutParams(paramsEnlargeRow);
            firstLL.setLayoutParams(paramsShrinkRow);
            secondLL.setLayoutParams(paramsShrinkRow);
            thirdLL.setLayoutParams(paramsShrinkRow);
        }
    }


    // Provjera: nalazi li se tocka trenutnog dodira (na zaslonu) unutar nekog od buttona na
    // tipkovnici?
    // Ulaz: kordinata dodira (pointerX, pointerY)
    // Izlaz: indeks buttona u glavnoj kolekciji (ako sadrzi tu tocku), -1 ako takav button ne postoji
    private int checkInsideButton(int pointerX, int pointerY){
        for (Button iButton : buttonList) {
            int[] loc = new int[2];
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


    // Azuriranje IME action-a ("exposano" za glavni servis [AirViewIME])
    public void setIMEaction(int imeAction){
        this.currentIMEaction = imeAction;
        ((Button)buttonList.get(31)).setText(get_enter_button_title());
    }

}