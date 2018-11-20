package engine.exception;

public class GamePropertiesException extends Exception {

    private String m_Message;

    public GamePropertiesException(String i_Message) {
        m_Message = i_Message;
    }

    @Override
    public String getMessage() {
        return m_Message;
    }
}
