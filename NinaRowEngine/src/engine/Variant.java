package engine;

public enum Variant {
    REGULAR ("Regular"),
    CIRCULAR ("Circular"),
    POPOUT ("Popout");

    private final String m_Name;

    Variant(String i_Name) {

        m_Name = i_Name;
    }

    @Override
    public String toString() {
        return m_Name;
    }
}
