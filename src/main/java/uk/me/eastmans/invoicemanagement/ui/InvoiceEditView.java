package uk.me.eastmans.invoicemanagement.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.invoicemanagement.Invoice;
import uk.me.eastmans.invoicemanagement.InvoiceService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Route("invoice-edit")
@RolesAllowed("INVOICES")
@PageTitle("Invoice Edit")
public class InvoiceEditView  extends Main implements HasUrlParameter<String> {
    private Invoice invoice;
    final InvoiceService invoiceService;
    final ViewToolbar viewToolbar;
    final TextField description;
    final DatePicker invoiceDate;
    final Button saveButton;
    final FormLayout formLayout;
    final Binder<Invoice> editBinder = new Binder<>(Invoice.class);
    private byte[] byteData;
    private String filename;
    private String mimeType;

    public static void editInvoice(Long invoiceId) {
        UI.getCurrent().navigate(InvoiceEditView.class, String.valueOf(invoiceId));
    }

    InvoiceEditView(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        description = new TextField("Description");
        editBinder.forField(description)
                .asRequired("Every Invoice must have a description")
                .withValidator(description -> description.length() <= Invoice.DESCRIPTION_MAX_LENGTH,
                        "Description length must be less than " + (Invoice.DESCRIPTION_MAX_LENGTH+1) + ".")
                .bind( Invoice::getDescription, Invoice::setDescription );
        invoiceDate = new DatePicker("Invoice Date");
        invoiceDate.setMax(now);
        invoiceDate.setHelperText("Must be in the past");
        editBinder.forField(invoiceDate)
                .asRequired("Every invoice must have a date")
                //.withValidator(date -> {
                //        },
                //        "Description length must be less than " + (ExpenseLine.DESCRIPTION_MAX_LENGTH+1) + ".")
                .bind( Invoice::getInvoiceDate, Invoice::setInvoiceDate );

        formLayout = new FormLayout();
        formLayout.setExpandColumns(true);
        formLayout.setExpandFields(true);
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        formLayout.setAutoResponsive(true);

        FormLayout.FormRow row = new FormLayout.FormRow();
        row.add(description, 2);
        row.add(invoiceDate);
        formLayout.add(row);

        viewToolbar = new ViewToolbar("Invoice Edit", ViewToolbar.group());
        add(viewToolbar);

        //Scroller scroller = new Scroller(formLayout);
        add(formLayout);

        saveButton = new Button("Save", e -> {
            // We need to persis the changes back to the database
            saveOrCreate();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> {
            History history = UI.getCurrent().getPage().getHistory();
            history.back();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancel);
        add(buttonLayout);
        // Enable or disable save button based on validation errors
        editBinder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
        });

        setHeightFull();
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        if (invoice.getId() == null) {
            viewToolbar.setTitle("Create Invoice");
            saveButton.setText("Save");
            saveButton.setEnabled(false);

            InMemoryUploadHandler inMemoryHandler = UploadHandler.inMemory(
                    (metadata, data) -> {
                        // Get other information about the file.
                        filename = metadata.fileName();
                        mimeType = metadata.contentType();
                        //long contentLength = metadata.contentLength();
                        // Do something with the file data...
                        byteData = data;
                        // processFile(data, fileName);
                    });
            Upload upload = new Upload(inMemoryHandler);
            upload.setMaxFiles(1);

            FormLayout.FormRow row = new FormLayout.FormRow();
            row.add(upload,3);
            formLayout.add(row);
        } else {
            // Show a preview
            StreamResource resource = new StreamResource(
                    invoice.getFileName(), () -> new ByteArrayInputStream(invoice.getImageData()));
            Image image = new Image(resource, invoice.getFileName());
            image.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST);

            FormLayout.FormRow row = new FormLayout.FormRow();
            row.add(image,3);
            formLayout.add(row);
        }

        this.invoice = invoice;
        editBinder.readBean(invoice);

        description.focus();
    }

    private void saveOrCreate() {
        try {
            editBinder.writeBean(invoice);
            // Set the byte array
            invoice.setFileName(filename);
            invoice.setMimeType(mimeType);
            invoice.setImageData(byteData);
            if (editBinder.validate().isOk()) {
                invoiceService.saveOrCreate(invoice);
                Notification.show("Invoice saved", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                History history = UI.getCurrent().getPage().getHistory();
                history.back();
            }
        } catch (Exception e) {
            // Something went wrong so inform the user
            notifyValidationErrors(e);
        }
    }

    private void notifyValidationErrors(Exception e) {
        // Need to show the error somehow
        Notification.show(e.getMessage(), 5000, Notification.Position.TOP_STRETCH)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String aLong) {
        // Get expense with the passed id, or create a new expense
        Invoice passedInvoice;
        if (aLong == null) {
            passedInvoice = new Invoice("");
        } else {
            Optional<Invoice> u = invoiceService.getInvoice(Long.parseLong(aLong));
            passedInvoice = u.orElseGet(() -> new Invoice( ""));
        }
        setInvoice(passedInvoice);
    }
}