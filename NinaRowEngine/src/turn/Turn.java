package turn;

import java.io.Serializable;
import engine.NinaRowEngine;
import player.Player;

public class Turn implements Serializable {
    private final Player m_Player;
    private final MoveType m_MoveType;
    private int m_Column = NinaRowEngine.UNDEFINED;
    private int m_Row = NinaRowEngine.UNDEFINED;

    public Turn(Player i_Player, MoveType i_MoveType, int i_Column, int i_Row) {
        this.m_Player = i_Player;
        this.m_MoveType = i_MoveType;
        this.m_Column = i_Column;
        this.m_Row = i_Row;
    }

    public Turn (Player i_Player, MoveType i_MoveType) {
        this.m_Player = i_Player;
        this.m_MoveType = i_MoveType;
    }

    @Override
    public String toString() {
        StringBuilder turnAsString = new StringBuilder();
        turnAsString.append(m_Player.getName()).append(", ").append(m_MoveType);
        if (!m_MoveType.equals(MoveType.RETIRE))
        {
            turnAsString.append(", column: ").append((m_Column + 1)) .append(", row: ").append((m_Row + 1));
        }

        return turnAsString.toString();
    }

    public Player getPlayer() {return m_Player;}

    public MoveType getMoveType() {
        return m_MoveType;
    }

    public int getColumn() {
        return m_Column;
    }

    public int getRow() {
        return m_Row;
    }

    public void setColumn(int i_Value) {
        m_Column = i_Value;
    }

    public void setRow(int i_Value) {
        this.m_Row = i_Value;
    }

}
