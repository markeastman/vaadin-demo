package uk.me.eastmans.processmanagement.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Schedulers;
import uk.me.eastmans.backgroundjobs.SAPProcess;
import uk.me.eastmans.base.ui.component.ViewToolbar;

import java.util.function.Consumer;

@Route("process-list")
@RolesAllowed("PROCESSES")
@PageTitle("Process List")
@Menu(order = 2, icon = "vaadin:automation", title = "Process List")
class ProcessListView extends Main {

    private static final Logger log = LoggerFactory.getLogger(ProcessListView.class);

    final private Text message = new Text("Messages");
    final private UI ui = UI.getCurrent();

    ProcessListView(SAPProcess sapProcess) {

        add(new ViewToolbar("Process List"));

        FormLayout formLayout = new FormLayout();

        // Add a button to start an async process

        Consumer<String> completeConsumer =
                s -> ui.access(() -> notifyUser(s));
        Consumer<Double> statusConsumer = d -> ui.access(() -> notifyUser("Procesed "+d));
        Consumer<Exception> errorConsumer = e -> ui.access(()->notifyUserError(e));

        Button starterButton = new Button("Start a process");
        starterButton.addClickListener(
                event -> sapProcess.executeProcess(completeConsumer,statusConsumer,errorConsumer));

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.afterPropertiesSet();

        Button starterButton2 = new Button("Start a background job");
        starterButton2.addClickListener(event -> sapProcess.startBackgroundJob()
                .doOnError(this::onJobFailed)
                .doOnComplete(this::notifyUserComplete)
                .subscribeOn(Schedulers.fromExecutor(taskExecutor))
                .subscribe(this::onJobUpdate));

        formLayout.add(starterButton, starterButton2);
        formLayout.add(message);
        formLayout.setSizeFull();
        add(formLayout);
        message.setText( "Click a button");
    }

    private void onJobFailed(Throwable error) {
        ui.access(() -> message.setText(error.getMessage()));
    }

    private void onJobUpdate(String s) {
        ui.access(() -> message.setText(s));
    }

    private void notifyUser(String s) {
        // Need to show the error somehow
        //log.info( "ProcessListView received " + s);
        message.setText("Job: " + s);
    }

    private void notifyUserError(Exception e) {
        // Need to show the error somehow
        //log.info( "ProcessListView received " + s);
        message.setText("Job Error: " + e.getMessage());
    }

    private void notifyUserComplete() {
        // Need to show the error somehow
        ui.access(() -> message.setText( "Background job completed "));
    }

    private void notifyUserViaLog(String s) {
        // Need to show the error somehow
        log.info( "ProcessListView received " + s);
        //Notification.show("Job: " + s);
    }
}