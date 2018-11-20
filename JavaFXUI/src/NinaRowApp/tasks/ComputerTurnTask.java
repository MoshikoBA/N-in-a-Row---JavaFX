package NinaRowApp.tasks;

import engine.GameStatus;
import engine.NinaRowEngine;
import javafx.application.Platform;
import javafx.concurrent.Task;
import turn.Turn;

public class ComputerTurnTask extends Task<Boolean> {

    private NinaRowEngine m_Engine;

    public ComputerTurnTask(NinaRowEngine i_Engine) {
        this.m_Engine = i_Engine;
    }

    @Override
    protected Boolean call() throws Exception {
        updateMessage("Calculating Move...");
        Thread.sleep(800);
        m_Engine.playTurnByComputer();

        Turn computerTurn = m_Engine.getTurnsHistory().get(m_Engine.getTurnsHistory().size() - 1);

        Platform.runLater(() -> m_Engine.getIncrementPlayerTurnsCounterDelegate().action());
        Platform.runLater(() -> m_Engine.getTurnAddedDelegate().accept(computerTurn));
        Platform.runLater(() -> m_Engine.getShowBoardDelegate().action(computerTurn.getColumn()));

        if (!m_Engine.getGameStatus().equals(GameStatus.RUNNING))
        {
            Platform.runLater(() -> m_Engine.getUpdateStatusDelegate().action());
            Platform.runLater(() -> m_Engine.getGameOverDelegate().action());
        }
        else {
            Platform.runLater(() -> m_Engine.getChangeTurnDelegate().action());
            Platform.runLater(() -> m_Engine.getUpdateCurrentPlayerNameDelegate().action());
        }

        updateMessage("");
        return Boolean.TRUE;
    }
}