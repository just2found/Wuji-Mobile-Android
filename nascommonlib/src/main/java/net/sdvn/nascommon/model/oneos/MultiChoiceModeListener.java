package net.sdvn.nascommon.model.oneos;

public interface MultiChoiceModeListener {
    boolean isMultiChoiceMode();

    void setMultiChoiceMode(boolean isMultiChoiceMode);

    void selectAll();

    void clearSelected();


}
