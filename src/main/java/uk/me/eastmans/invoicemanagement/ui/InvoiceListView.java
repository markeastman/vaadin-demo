package uk.me.eastmans.invoicemanagement.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ConfirmDeleteDialog;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.invoicemanagement.Invoice;
import uk.me.eastmans.invoicemanagement.InvoiceService;

import java.util.List;
import java.util.function.Consumer;

@Route("invoice-list")
@RolesAllowed("INVOICES")
@PageTitle("Invoice List")
@Menu(order = 3, icon = "vaadin:invoice", title = "Invoices List")
class InvoiceListView extends Main {

    final AuthenticationContext authenticationContext;
    final InvoiceService invoiceService;
    Grid<Invoice> invoicesGrid;
    GridListDataView<Invoice> dataView;

    InvoiceListView(AuthenticationContext authenticationContext,
                    InvoiceService expenseService) {

        this.authenticationContext = authenticationContext;
        this.invoiceService = expenseService;

        invoicesGrid = new Grid<>();
        Grid.Column<Invoice> descriptionColumn = invoicesGrid.addColumn(Invoice::getDescription);
        invoicesGrid.addColumn(Invoice::getInvoiceDate);
        invoicesGrid.addColumn(Invoice::getMimeType);
        Grid.Column<Invoice> actionsColumn = invoicesGrid.addColumn(new ComponentRenderer<>(header -> new Span()));
        List<Invoice> invoices = invoiceService.listAll();
        dataView = invoicesGrid.setItems(invoices);
        InvoiceFilter invoiceFilter = new InvoiceFilter(dataView);

        invoicesGrid.getHeaderRows().clear();
        HeaderRow headerRow = invoicesGrid.appendHeaderRow();
        headerRow.getCell(descriptionColumn).setComponent(
                createFilterHeader("Description", invoiceFilter::setDescription));

        HorizontalLayout actionsHeaderLayout = new HorizontalLayout();
        actionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsHeaderLayout.add(new Text("Actions"));
        Button newButton = new Button(new Icon(VaadinIcon.PLUS));
        newButton.setTooltipText("Create a new Invoice");
        newButton.addClickListener(event -> {
            // Create a new User
            createInvoice();
        });
        actionsHeaderLayout.add(newButton);
        headerRow.getCell(actionsColumn).setComponent(actionsHeaderLayout);

        invoicesGrid.addComponentColumn(invoice -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setTooltipText("Edit this Invoice");
            editButton.addClickListener(event -> InvoiceEditView.editInvoice(invoice.getId()));
            actionsLayout.add(editButton);
            actionsLayout.add(createRemoveButton(invoice));
            return actionsLayout;
        }).setHeader(actionsHeaderLayout).setWidth("150px").setFlexGrow(0);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Invoice List", ViewToolbar.group()));
        add(invoicesGrid);
    }

    private Button createRemoveButton(Invoice invoice) {
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.setTooltipText("Remove this Invoice");
        removeButton.addClickListener(e -> ConfirmDeleteDialog.show(
                "Delete Invoice", "This will permanently delete '" + invoice.getDescription() + "'",
                event -> removeInvoice(invoice)));
        return removeButton;
    }

    private void removeInvoice(Invoice invoice) {
        // Remove the expense and update grid.
        invoiceService.deleteInvoice(invoice);
        invoicesGrid.setItems(invoiceService.listAll());

        Notification.show("Invoice '" + invoice.getDescription() + "' deleted", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void createInvoice() {
        // We need to create a user page
        InvoiceEditView.editInvoice(0L);
    }

    private static Component createFilterHeader(String labelText) {
        return createFilterHeader(labelText, null);
    }

    private static Component createFilterHeader(String labelText,
                                                Consumer<String> filterChangeConsumer) {
        NativeLabel label = new NativeLabel(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)");
        TextField textField = new TextField();
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        if  (filterChangeConsumer != null) {
            textField.setValueChangeMode(ValueChangeMode.EAGER);
            textField.setClearButtonVisible(true);
            textField.setWidthFull();
            textField.getStyle().set("max-width", "100%");
            textField.addValueChangeListener(
                    e -> filterChangeConsumer.accept(e.getValue()));
        } else {
            textField.setValue("");
            textField.setReadOnly(true);
        }
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private static class InvoiceFilter {
        private final GridListDataView<Invoice> dataView;

        private String description;

        public InvoiceFilter(GridListDataView<Invoice> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setDescription(String description) {
            this.description = description;
            this.dataView.refreshAll();
        }

        public boolean test(Invoice header) {
            return matches(header.getDescription(), description);
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }

}