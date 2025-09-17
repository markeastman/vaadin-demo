# vaadin-demo
Experiment with vaadin to test out certain features and the
aspect of components, dialogs and the ability to create decent
business applications.

To run the program just load the project into IntelliJ and click the execute button,
or from the command line use:
```./mvnw```

## Security
The security for the systems is based upon simple username and password 
authentication but I want to test the ability for users to have personas
(similar to groups) and for them to select the persona they want to see
a menu for and work as. So a user could act as an administrator sometimes but generally as say a credit controller the rest of the time.
He can move between these two personas as he wants so as not to get flooded with menu
options for personas he uses rarely.

So a User can have many Personas and each persona has a set of roles that
someone with this persona can do. The roles govern the low level access to menu options
etc. The Persona is an application wide name and has a set of roles which anyone with that persona will 
be able to run as. The administrator can create and change the associations of Personas to roles
so as to create Personas that are meaningful to their business, but the underlying set
of roles is hard coded within the application.

For the demo application we have two deployed test users:
* username: admin, password: admin
* username: cc, password: cc

The admin user can act as an 'Administrator' persona and the cc user can act as either a
credit controller or a supplier manager. If the user switches between the personas that he has
available the application will rebuild the menu and provide the user with the appropriate
operations.

## CRUD Processing

For the Persona entity I have implemented a list based page which shows all the Personas
without any filters as there is not expected to be such a great number. Against
each row I have an edit button and a delete button. The delete button asks for confirmation 
and if accepted it will delete the Persona entity and update all the relevant
users. Within the header there is also a plus button to create a new Persona. Both 
the edit and the new should go to the same editing dialog.


