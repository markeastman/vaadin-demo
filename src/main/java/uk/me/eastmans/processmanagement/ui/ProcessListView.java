package uk.me.eastmans.processmanagement.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;

@Route("process-list")
@RolesAllowed("PROCESSES")
@PageTitle("Process List")
@Menu(order = 2, icon = "vaadin:automation", title = "Process List")
class ProcessListView extends Main {

    ProcessListView() {
        add(new ViewToolbar("Process List"));

    }

}

