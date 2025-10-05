package uk.me.eastmans.base.ui.component;

import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

public class YesBlankRenderer<SOURCE> extends BasicRenderer<SOURCE, Boolean> {

    public YesBlankRenderer(ValueProvider<SOURCE, Boolean> valueProvider) {
        super(valueProvider);
    }

    protected String getFormattedValue(Boolean value) {
        if (value != null && value) {
            return "Yes";
        }
        return "";
    }
}