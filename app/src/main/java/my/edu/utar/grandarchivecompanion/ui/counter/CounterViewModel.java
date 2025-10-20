package my.edu.utar.grandarchivecompanion.ui.counter;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CounterViewModel extends ViewModel {
    //Use LiveData to hold the counter state
    private final MutableLiveData<Integer> lifeA = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> lifeB = new MutableLiveData<>(0);
    private final MutableLiveData<List<LogEntry>> logEntries = new MutableLiveData<>(new ArrayList<>());


    //Logic for Batching Log Entries
    public static final long LOG_DELAY_MS = 1500; //1.5 seconds delay
    private final Handler logHandler = new Handler(Looper.getMainLooper());
    private Runnable logRunnableA, logRunnableB;

    private int pendingChangeA = 0;
    private int pendingChangeB = 0;
    private int lastLoggedLifeA = 0;
    private int lastLoggedLifeB = 0;

    //Public getters expose the immutable LiveData to the Fragment
    public LiveData<Integer> getLifeA() {
        return lifeA;
    }
    public LiveData<Integer> getLifeB() {
        return lifeB;
    }
    public LiveData<List<LogEntry>> getLogEntries() { return logEntries; }

    //Public methods for the Fragment to call in response to user actions
    public int changeLife(boolean isPlayerA, int amount) {
        MutableLiveData<Integer> lifeData = isPlayerA ? lifeA : lifeB;
        int currentValue = lifeData.getValue() != null ? lifeData.getValue() :0;
        int originalValue = currentValue; // Store the value before the change

        if (amount < 0) { // If taking damage
            currentValue = Math.max(0, currentValue + amount); // Prevents going below 0
        } else { // If healing
            currentValue += amount;
        }
        final int actualChange = currentValue - originalValue;

        // If no change happened, do nothing further and return 0
        if (actualChange == 0) {
            return 0;
        }

        lifeData.setValue(currentValue);

        // Return the actual difference

        if (isPlayerA){
            if(pendingChangeA == 0){
                lastLoggedLifeA = originalValue;
            }
            pendingChangeA += actualChange;
            if (logRunnableA != null) {
                logHandler.removeCallbacks(logRunnableA);
            }
            logRunnableA = () -> commitLogEntry(true);
            logHandler.postDelayed(logRunnableA, LOG_DELAY_MS);
        }else{
            if(pendingChangeB == 0){
                lastLoggedLifeB = originalValue;
            }
            pendingChangeB += actualChange;
            if (logRunnableB != null) {
                logHandler.removeCallbacks(logRunnableB);
            }
            logRunnableB = () -> commitLogEntry(false);
            logHandler.postDelayed(logRunnableB, LOG_DELAY_MS);
        }
        return actualChange;
    }

    public void resetLife() {
        lifeA.setValue(0);
        lifeB.setValue(0);
        //Clear log as well
        List<LogEntry> currentLogs = logEntries.getValue();
        if (currentLogs != null) {
            currentLogs.clear();
            logEntries.setValue(currentLogs);
        }
    }

    private void addLogEntry(String player, int totalChange, int initialLife, int finalLife, int playerIndex) {
        List<LogEntry> currentLogs = logEntries.getValue();
        if (currentLogs == null) return;

        String changeSign = totalChange > 0 ? "+" : "";
        String text = String.format("%s: %d -> %d (%s%d)", player, initialLife, finalLife, changeSign, totalChange);

        // Create the new LogEntry object
        LogEntry newEntry = new LogEntry(text, playerIndex);

        currentLogs.add(0, newEntry);
        logEntries.setValue(currentLogs);
    }

    private void commitLogEntry(boolean isPlayerA) {
        if (isPlayerA) {
            if (pendingChangeA == 0) return;
            Integer finalLife = lifeA.getValue();
            if(finalLife == null) return;

            // MODIFIED: Pass the player index (0 for Player A)
            addLogEntry("Player 1", pendingChangeA, lastLoggedLifeA, finalLife, 0);
            pendingChangeA = 0;
        } else {
            if (pendingChangeB == 0) return;
            Integer finalLife = lifeB.getValue();
            if(finalLife == null) return;

            // MODIFIED: Pass the player index (1 for Player B)
            addLogEntry("Player 2", pendingChangeB, lastLoggedLifeB, finalLife, 1);
            pendingChangeB = 0;
        }
    }
}
