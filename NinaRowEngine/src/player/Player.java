package player;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import turn.Turn;

import java.io.Serializable;

public class Player implements Serializable {
    private final StringProperty  m_Name = new SimpleStringProperty();
    private final BooleanProperty m_IsHuman = new SimpleBooleanProperty();
    private final IntegerProperty m_ID = new SimpleIntegerProperty();
    private BooleanProperty m_IsRetire = new SimpleBooleanProperty(false);
    private IntegerProperty m_TurnsCounter = new SimpleIntegerProperty(0);
    private boolean m_IsWinner = false;

    public Player(String i_PlayerName, boolean i_IsPlayerHuman, short i_PlayerID) {
        this.m_Name.setValue(i_PlayerName);
        this.m_IsHuman.setValue(i_IsPlayerHuman);
        this.m_ID.setValue(i_PlayerID);
    }

    public StringProperty getNameProperty() {
        return this.m_Name;
    }

    public BooleanProperty getIsHumanProperty() {
        return this.m_IsHuman;
    }

    public IntegerProperty getIDProperty() { return this.m_ID; }

    public BooleanProperty getIsRetireProperty() {return m_IsRetire; }

    public void setIsRetire(boolean i_Value) {m_IsRetire.setValue(i_Value);}

    public IntegerProperty getTurnsCounterProperty() {return this.m_TurnsCounter;}

    public void setTurnsCounter(int i_Value) {this.m_TurnsCounter.setValue(i_Value);}

    public String getName() {return m_Name.get();}

    public boolean getIsHuman() {return m_IsHuman.get();}

    public int getId() {return m_ID.get();}

    public boolean getIsRetire() {return m_IsRetire.get();}

    public int getTurnsCounter() {return m_TurnsCounter.get();}

    public boolean getIsWinner() {return m_IsWinner;}

    public void setIsWinner(boolean i_Value) {m_IsWinner = i_Value;}

    public void incrementTurnsCounter() {
        m_TurnsCounter.setValue(m_TurnsCounter.getValue() + 1);
    }

    public void resetTurnsCounter() {
        m_TurnsCounter.setValue(0);
    }

    @Override
    public String toString() {
        StringBuilder playerAsString = new StringBuilder();

        playerAsString.append("Name: ").append(m_Name).append(System.lineSeparator()).append("ID: ").append
                (m_ID).append(System.lineSeparator()).append("Turns played: ").append(m_TurnsCounter);
        return playerAsString.toString();
    }
}
