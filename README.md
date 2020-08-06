# Projecty Web v2.0
Projecty is a project management app based on spring.

## Motivation
My main objective is to create a free and open-source privacy project management application for everyone.
I know that project data are really sensitive, so you feel better when you are the owner of them.
And for, those who do not have a server, 
Projecty will be available on the hosted server completely for free (in the future).

## Features
### Tasks
* Tasks management (mark tasks as To do, In progress, Done)
* Assign tasks to users
* Task importance levels
* Group tasks into projects
### Projects
* Project user management
* Project roles (Admin/User)
### Teams
* Team user management
* Team Roles (Manager/Member)
### Chat
* Real time chat via WebSockets
### Messages
* Send messages to users
* Reply to messages
* Send messages with multiple attachments
### Notifications
* Notify user about important activities
* Optionally send E-mail notifications
### Dashboard
* Get assigned tasks for current user
### Users
* Avatars
* Username completion
* Personalize notifications
* Block adding to new projects/teams

### …and more


## Note
**Projecty v2.0 is compatible only with Angular front-end so far. You cannot use this version with 
Android or Vue.js clients due to differences in user authentication (and other minor things).**       
[Projecty Angular](https://github.com/marcinadd/projecty-angular) 

## Getting Started
### Via Docker
1. After changes run `./build_image.sh` in a project root directory to build application image.
1. Run`docker-compose up` in a project root directory.

Data is stored in a volume `db-data`.

### Set up project manually
You can use `development` profile to set up project manually.
1. Switch profile to development in `application.properties`:  
    ```
    spring.profiles.active=development
    ```

#### Datasource and Keycloak configuration
Check configuration and edit if you need.
1. Set database credentials in `application-development.properties`:
    ```
   spring.datasource.url=jdbc:mysql://localhost:3306/projecty
   spring.datasource.username=root
   spring.datasource.password=password
    ``` 
1. Set Keycloak server url:  
     ```
     keycloak.auth-server-url=http://localhost:8081/auth
    ```
## Prerequisites
* JRE ≥ 11
* Docker or (MySQL 8.0 compatible database and Keycloak 10.0.0 server)  

## Contributing
Your contribution is welcome. No matter who you are, you can help anyway.
The most helpful is help with coding but graphic designers are also needed.
If you are not a developer or graphic designer don't worry,
you can help with translations, post on a blog, make a video, or tell your friend about Projecty.
Your contribution will be appreciated.

## License
Projecty is licensed under GNU GPL v3.0 http://www.gnu.org/licenses/gpl-3.0.html
