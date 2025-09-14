package uk.me.eastmans.security.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import uk.me.eastmans.base.ui.component.ViewToolbar;

@Route("persona-list")
@RolesAllowed("PERSONAS")
@PageTitle("Personas List")
@Menu(order = 1, icon = "vaadin:user-check", title = "Persona List")
class PersonaListView extends Main {

    PersonaListView() {
        add(new ViewToolbar("Persona List"));

    }

}