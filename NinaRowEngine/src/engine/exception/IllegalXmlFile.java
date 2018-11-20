package engine.exception;

public class IllegalXmlFile extends Exception {
    @Override
    public String getMessage() {
        return "Illegal XML file." + System.lineSeparator() + "This is not N in a Row file.";
    }
}
