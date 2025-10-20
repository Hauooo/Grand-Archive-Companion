package my.edu.utar.grandarchivecompanion.ui.counter;

public class LogEntry {
    private final String logText;
    private final int playerIndex; // 0 for Player 1, 1 for Player 2

    public LogEntry(String logText, int playerIndex) {
        this.logText = logText;
        this.playerIndex = playerIndex;
    }

    public String getLogText() {
        return logText;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }
}