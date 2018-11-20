package engine;

import java.awt.*;

public class CellChange {
    private final Point m_CellLocation;
    private final byte m_PrevValue;
    private final byte m_NextValue;

    public CellChange (Point i_CellLocation, byte i_PrevValue, byte i_NextValue) {
        this.m_CellLocation = i_CellLocation;
        this.m_PrevValue = i_PrevValue;
        this.m_NextValue = i_NextValue;
    }

    public Point getCellLocation() { return this.m_CellLocation;}

    public byte getPrevValue() { return this.m_PrevValue; }

    public byte getNextValue() { return this.m_NextValue; }
}
