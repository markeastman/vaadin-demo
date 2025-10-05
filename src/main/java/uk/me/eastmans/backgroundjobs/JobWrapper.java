package uk.me.eastmans.backgroundjobs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;

import java.security.SecureRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JobWrapper {

    final private SAPProcess sapService;
    final private long id;
    final private String description;
    private boolean isCancelled = false;
    private String statusMessage;
    private boolean completed = false;
    private String completedMessage;
    private boolean inError = false;
    private String errorMessage;
    private UI currentUI;
    private Grid<JobWrapper> attachedGrid;

    public JobWrapper(SAPProcess sapService, String description) {
        this.sapService = sapService;
        this.description = description;
        SecureRandom secureRandom = new SecureRandom();
        id = Math.abs(secureRandom.nextLong());
        statusMessage = "About to start";
        completedMessage = "";
        errorMessage = "";
    }

    public void cancel() {
        isCancelled = true;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        updateUI();
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getCompletedMessage() {
        return completedMessage;
    }

    public void setCompletedMessage(String completedMessage) {
        this.completed =true;
        this.completedMessage = completedMessage;
        updateUI();
    }

    public boolean isInError() {
        return inError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        inError = true;
        errorMessage = message;
        updateUI();
    }

    protected void startBackgroundJob(UI ui, Grid<JobWrapper> attacheGrid) {
        currentUI = ui;
        this.attachedGrid = attacheGrid;
        Consumer<String> completeConsumer = this::setCompletedMessage;
        Consumer<Double> statusConsumer = d -> setStatusMessage("Processed "+d+"%");
        Consumer<Exception> errorConsumer = e -> setErrorMessage(e.getMessage());
        Supplier<Boolean> isCancelled = this::isCancelled;

        sapService.executeProcess(
                completeConsumer,statusConsumer,errorConsumer,isCancelled);
    }

    private void updateUI() {
        // Try to update attached grid
        if (currentUI != null)
            currentUI.access(() -> attachedGrid.getListDataView().refreshItem(this));
    }
}