package NinaRowApp.tasks;

import javafx.concurrent.Task;

import java.util.function.Consumer;

public class LoadXmlTask extends Task<Boolean> {
    private Consumer<String> m_LoadXmlDelegate;
    private String m_FilePath;

    public LoadXmlTask(Consumer<String> i_LoadXmlDelegate, String i_FilePath) {
        this.m_LoadXmlDelegate = i_LoadXmlDelegate;
        this.m_FilePath = i_FilePath;
    }

    @Override
    protected Boolean call() throws Exception {

        m_LoadXmlDelegate.accept(m_FilePath);
        return Boolean.TRUE;
    }
}
