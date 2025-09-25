package uk.me.eastmans.base.ui.component;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class MessageDialog extends ConfirmDialog {

    public MessageDialog(String header, String message) {
        setHeader(header);
        setText(new Html(message));
        setConfirmText("OK");
        // Show the dialog
        open();
    }
}