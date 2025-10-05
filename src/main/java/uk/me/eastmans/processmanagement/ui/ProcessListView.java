package uk.me.eastmans.processmanagement.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import uk.me.eastmans.backgroundjobs.JobManager;
import uk.me.eastmans.backgroundjobs.JobWrapper;
import uk.me.eastmans.backgroundjobs.SAPProcess;
import uk.me.eastmans.base.ui.component.ViewToolbar;
import uk.me.eastmans.base.ui.component.YesBlankRenderer;

@Route("process-list")
@RolesAllowed("PROCESSES")
@PageTitle("Process List")
@Menu(order = 2, icon = "vaadin:automation", title = "Process List")
class ProcessListView extends Main {

    private static final Logger log = LoggerFactory.getLogger(ProcessListView.class);

    final private Text message = new Text("Messages");
    final private UI ui = UI.getCurrent();
    final JobManager jobManager;
    final private Grid<JobWrapper> jobsGrid;

    ProcessListView(SAPProcess sapProcess, JobManager jobManager,
                    @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.jobManager = jobManager;

        add(new ViewToolbar("Process List"));

        jobsGrid = new Grid<>();
        jobsGrid.setItems(jobManager.getJobs());
        // Create the columns
        jobsGrid.addColumn(JobWrapper::getId).setHeader("Id").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(JobWrapper::getDescription).setHeader("Description").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(JobWrapper::getStatusMessage).setHeader("Status").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(new YesBlankRenderer<>(JobWrapper::isInError)).setHeader("In Error").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(JobWrapper::getErrorMessage).setHeader("Error Message").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(new YesBlankRenderer<>(JobWrapper::isCompleted)).setHeader("Is Complete").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(JobWrapper::getCompletedMessage).setHeader("Complete Message").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.addColumn(new YesBlankRenderer<>(JobWrapper::isCancelled)).setHeader("Is Cancelled").setResizable(true)
                .setAutoWidth(true).setFlexGrow(0);
        jobsGrid.setPartNameGenerator(jobWrapper -> {
            if (jobWrapper.isCompleted())
                return "high-rating";
            if (jobWrapper.isInError())
                return "low-rating";
            if (jobWrapper.isCancelled())
                return "medium-rating";
            return null;
        });
        HorizontalLayout actionsHeaderLayout = new HorizontalLayout();
        actionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        actionsHeaderLayout.add(new Text("Actions") );
        Button newButton = new Button(new Icon(VaadinIcon.PLUS));
        newButton.setTooltipText("Start Job");
        newButton.addClickListener(event -> {
            // Start a background job via the manager
            jobManager.startBackgroundJob(UI.getCurrent(),jobsGrid);
            jobsGrid.setItems(jobManager.getJobs());
        });
        actionsHeaderLayout.add(newButton);
        jobsGrid.addComponentColumn(jobWrapper -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            Button editButton = new Button(new Icon(VaadinIcon.STOP_COG));
            editButton.setTooltipText("Cancel this Job");
            editButton.addClickListener(e -> {
                jobWrapper.cancel();
                jobsGrid.getListDataView().refreshItem(jobWrapper);
            });
            actionsLayout.add(editButton);
            return actionsLayout;
        }).setHeader(actionsHeaderLayout).setWidth("150px").setFlexGrow(0);

        add(jobsGrid);
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
    }
}