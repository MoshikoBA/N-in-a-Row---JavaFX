package turn;

public enum MoveType {
    INSERT_DISC ("Insert Disc"),
    POPOUT_DISC ("Pop out Disc"),
    RETIRE("Retire");

    private String name;

    MoveType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}