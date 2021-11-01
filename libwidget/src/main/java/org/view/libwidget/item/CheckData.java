package org.view.libwidget.item;

public class CheckData {

    private boolean isPassChecked;
    private String valueStr;

    public CheckData(boolean isPassChecked, String valueStr) {
        this.isPassChecked = isPassChecked;
        this.valueStr = valueStr;
    }

    public boolean isPassChecked() {
        return isPassChecked;
    }

    public void setPassChecked(boolean passChecked) {
        isPassChecked = passChecked;
    }

    public String getValueStr() {
        return valueStr;
    }

    public void setValueStr(String isValueStr) {
        this.valueStr = isValueStr;
    }
}
