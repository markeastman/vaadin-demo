package uk.me.eastmans.base.ui.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class ConfirmDeleteDialog extends ConfirmDialog {

    public ConfirmDeleteDialog(String header, String message,
                               ComponentEventListener<ConfirmEvent> listener) {
        setHeader(header);
        setText(message);
        setCancelable(true);
        setConfirmText("Delete");
        setConfirmButtonTheme("error primary");
        addConfirmListener(listener);
        open();
    }
}