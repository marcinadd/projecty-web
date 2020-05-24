# Projecty Web
Projecty is a project management app based on spring.

## Motivation
My main objective is to create a free and open-source privacy project management application for everyone.
I know that project data are really sensitive so you feel better when you are the owner of them.
And for those who do not have a server, 
Projecty will be available on the hosted server completely for free (in the future).

## Warning v2.0
This version uses an authentication system which becomes deprecated now. It is compatible with Vue.js and Android version, however 
differences in user authentication made it **incompatible with Angular version**.
Only version v2.0 will be updated. You can find it at the branch [rest-2.0](https://github.com/marcinadd/projecty-web/tree/rest-2.0).
It uses Keycloak for identify management.

## Note
The app has been divided into two separate projects. 
You can find front-end based on Vue at [projecty-web-front-end](https://github.com/marcinadd/projecty-web-front-end).
You can find Android version at [projecty-android](https://github.com/marcinadd/projecty-android).

## Getting Started
### Via Docker
1. After changes run `./build_image.sh` in a project root directory to build application image.
1. Run`docker-compose up` in a project root directory.

Data is stored in a volume `db-data`. During first run of database container the scripts from `/sql-scripts` directory will be executed.

### Setup project manually
1. Clone repository
1. Import it to your favourite IDE
1. **Remember to edit application.properties**
    ```
    spring.datasource.url=jdbc:mysql://db_host:db_port/db_name
    spring.datasource.username=db_user
    spring.datasource.password=db_password
    ```
    Make sure that this user have full access to database
1. Prepare DB for store oauth tokens; execute this SQL
    ```sql
   CREATE TABLE `oauth_access_token` (
     `token_id` varchar(255) DEFAULT NULL,
     `token` mediumblob,
     `authentication_id` varchar(255) NOT NULL,
     `user_name` varchar(255) DEFAULT NULL,
     `client_id` varchar(255) DEFAULT NULL,
     `authentication` mediumblob,
     `refresh_token` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`authentication_id`)
   );
   CREATE TABLE `oauth_refresh_token` (
     `token_id` varchar(255) DEFAULT NULL,
     `token` mediumblob,
     `authentication` mediumblob
   );
   ``` 
1. Build the app, run it and enjoy
1. Remember to check if the timezone is set correctly

## Prerequisites
* JRE ≥ 1.8
* MySQL 8.0 compatible database

## Contributing
Your contribution is welcome. No matter who you are, you can help anyway.
The most helpful is help with coding but graphic designers are also needed.
If you are not a developer or graphic designer don't worry,
you can help with translations, post on a blog, make a video, or tell your friend about Projecty.
Your contribution will be appreciated.

## License
Projecty is licensed under GNU GPL v3.0 http://www.gnu.org/licenses/gpl-3.0.html
