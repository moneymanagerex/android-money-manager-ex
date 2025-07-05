package com.money.manager.ex.about;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WhatNewManager {
    private static final String TAG = "WhatNewManager";
    private static final String PREFS_NAME = "WhatNewPrefs"; // Nome del file di preferenze
    private static final String KEY_LAST_SEEN_VERSION = "last_seen_version"; // Chiave per salvare l'ultima versione vista

    private final Context context; // Contesto dell'applicazione

    /**
     * Costruttore della classe WhatNewManager.
     *
     * @param context Il contesto dell'applicazione.
     */
    public WhatNewManager(Context context) {
        this.context = context;
    }

    /**
     * Mostra il dialogo "What's New" se la versione corrente dell'app è
     * superiore all'ultima versione che l'utente ha visto.
     *
     * @param activity L'attività (Activity) da cui viene chiamato questo metodo,
     * necessaria per mostrare il dialogo.
     */
    public void showWhatsNewIfNeeded(Activity activity) {
        int lastSeenVersion = getLastSeenVersion(); // Recupera l'ultima versione vista
        int currentVersion = getCurrentAppVersion(); // Recupera la versione corrente dell'app
        if (lastSeenVersion == 0) {
            // we don't need to notify the user on new release since it was just downloaded
            // set this as last seen version
            saveLastSeenVersion(currentVersion);
            return ;
        }

        Timber.d( TAG + ": Last seen version: " + lastSeenVersion + ", current version: " + currentVersion);

        // Se la versione corrente è minore o uguale all'ultima vista, non fare nulla.
        if (currentVersion <= lastSeenVersion) {
            Timber.d( TAG +": no news to show");
            return;
        }

        // Recupera le stringhe del changelog tra l'ultima versione vista e la corrente.
        List<String> changelogs = getChangelogStrings(lastSeenVersion + 1, currentVersion);

        // Se non ci sono changelog specifici  non mostra nulla
        if (changelogs.isEmpty()) {
            saveLastSeenVersion(currentVersion);
            return;
        }

        // Costruisce il messaggio del dialogo.
        StringBuilder message = new StringBuilder();
        for (String log : changelogs) {
            message.append(log).append("\n-------\n"); // Aggiunge un punto elenco per ogni voce
        }

        // Crea e mostra il dialogo AlertDialog.
        new AlertDialog.Builder(activity)
                .setTitle(context.getString(R.string.changelog_title)) // Titolo del dialogo
                .setMessage(message.toString()) // Messaggio con i changelog
                .setPositiveButton(context.getString(R.string.dismiss), (dialog, which) -> {
                    // Quando l'utente preme "Dimiss", salva la versione corrente come l'ultima vista.
                    saveLastSeenVersion(currentVersion);
                    Timber.d(TAG+ ": Version " + currentVersion + " saved as last seen.");
                    dialog.dismiss(); // Chiude il dialogo
                })
                .setNegativeButton(context.getString(R.string.remember_later), (dialog, which) -> {
                    // Se l'utente preme "Ricordamelo", semplicemente chiude il dialogo.
                    // La versione non viene salvata, quindi il dialogo riapparirà al prossimo avvio.
                    Timber.d(TAG + ": L'utente ha scelto 'Ricordamelo'. Il dialogo riapparirà.");
                    dialog.dismiss(); // Chiude il dialogo
                })
                .setCancelable(false) // Rende il dialogo non annullabile tramite tocco esterno o tasto indietro
                .show(); // Mostra il dialogo
    }

    /**
     * Recupera l'ultima versione dell'app che è stata visualizzata dall'utente
     * dalle SharedPreferences.
     *
     * @return L'ultima versione vista, o 0 se non è mai stata salvata.
     */
    private int getLastSeenVersion() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Restituisce il valore salvato, o 0 se non esiste (prima esecuzione).
        return prefs.getInt(KEY_LAST_SEEN_VERSION, 0);
    }

    /**
     * Salva la versione corrente dell'app nelle SharedPreferences
     * come l'ultima versione vista dall'utente.
     *
     * @param version La versione da salvare.
     */
    private void saveLastSeenVersion(int version) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LAST_SEEN_VERSION, version);
        editor.apply(); // Applica le modifiche in background
    }

    /**
     * Recupera il codice della versione corrente dell'app dal PackageManager.
     *
     * @return Il codice della versione corrente dell'app, o -1 in caso di errore.
     */
    private int getCurrentAppVersion() {
        Core core = new Core(context);
        return core.getAppVersionCode();
    }

    /**
     * Recupera le stringhe del changelog da R.string.changelog_XXXX
     * per un intervallo di versioni.
     *
     * @param startVersion La versione iniziale (inclusa) da cui iniziare a cercare i changelog.
     * @param endVersion   La versione finale (inclusa) fino a cui cercare i changelog.
     * @return Una lista di stringhe di changelog trovate.
     */
    private List<String> getChangelogStrings(int startVersion, int endVersion) {
        List<String> changelogs = new ArrayList<>();
        // Itera da startVersion a endVersion per recuperare le stringhe.
        for (int i = startVersion; i <= endVersion; i++) {
            // Costruisce il nome della risorsa stringa, es. "changelog_1081".
            String resourceName = "changelog_" + i;
            // Ottiene l'ID della risorsa stringa.
            @SuppressLint("DiscouragedApi") int resourceId = context.getResources().getIdentifier(resourceName, "string", context.getPackageName());

            // Se la risorsa esiste (ID non è 0), recupera la stringa e la aggiunge alla lista.
            if (resourceId != 0) {
                changelogs.add(context.getString(resourceId));
                Timber.d(TAG+ ": Trovato changelog per versione " + i);
            } else {
                Timber.d(TAG+ ": Nessun changelog trovato per versione " + i);
            }
        }
        return changelogs;
    }

}
