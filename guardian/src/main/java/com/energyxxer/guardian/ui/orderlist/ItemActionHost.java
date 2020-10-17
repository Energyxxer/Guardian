package com.energyxxer.guardian.ui.orderlist;

import com.energyxxer.guardian.ui.explorer.base.StyleProvider;

import javax.swing.*;

public interface ItemActionHost {
    JComponent getComponent();
    void performOperation(int code);
    StyleProvider getStyleProvider();
}
