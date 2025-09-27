package uk.me.eastmans.base.ui.component;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class MessageDialog extends ConfirmDialog {

    private MessageDialog(String header, String message) {
        setHeader(header);
        setText(new Html(message));
        setConfirmText("OK");
    }

    public static void show(String header, String message) {
        MessageDialog dialog = new MessageDialog(header, message);
        dialog.open();
    }
}