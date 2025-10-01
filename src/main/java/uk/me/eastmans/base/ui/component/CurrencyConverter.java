package uk.me.eastmans.base.ui.component;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CurrencyConverter implements Converter<String, BigDecimal> {

    @Override
    public Result<BigDecimal> convertToModel(String fieldValue, ValueContext context) {
        // Produces a converted value or an error
        try {
            // remove undesired dollar signs and commas
            String simpleString = fieldValue.replaceAll("[€$,]","");
            // ok is a static helper method that creates a Result
            return Result.ok(new BigDecimal(simpleString));
        } catch (NumberFormatException e) {
            // error is a static helper method that creates a Result
            return Result.error("Enter a number.");
        }
    }

    // number output format that inserts commas and guarantees two decimal places
    /* NOTE: I insert a dollar sign icon at the start of the TextField with:
             import com.vaadin.flow.component.icon.Icon;
             import com.vaadin.flow.component.icon.VaadinIcon;
             textFieldWithUSDAmount.setPrefixComponent(new Icon(VaadinIcon.DOLLAR));
     */
    private static final DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###,###.00");

    @Override
    public String convertToPresentation(BigDecimal bigDecimal, ValueContext context) {
        // Converting to the field type should
        // always succeed, so there is no support for
        // returning an error Result.
        return decimalFormat.format(bigDecimal);
    }
}