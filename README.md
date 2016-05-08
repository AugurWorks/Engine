# Engine
AugurWorks UI Engine 2.0

## Deployment
A production environment can be built which uses environment variables for all customizable config values. The environment variables are listed below:

- **SERVER_URL** (default: http://[local-ip]:8080) - Full deployed application URL
- **BUCKET** (default: aw-files-dev) - Bucket for log file placement
- **CHANNEL** (default: #testing) - Slack channel for prediction output
- **RDS_USERNAME** (default: root) - MySQL username
- **RDS_PASSWORD** - MySQL password
- **RDS_HOSTNAME** - MySQL host
- **RDS_PORT** (default: 3306) - MySQL port number
- **RDS_DB_NAME** (default: engine) - MySQL DB name
- **ML_MAX** - Maximum simultaneous machine learning runs

## Local Development
### Dependencies
- [Grails 2.4.5](https://grails.org/download.html) (SKDMAN! which is mentioned on that page is highly recommended)
- [Docker](https://docs.docker.com/engine/installation/) (Optional)

### Setup
After cloning the repo copy **UserConfig.groovy.example** to **UserConfig.groovy** and move it one folder up from this project's root folder. Fill in the appropriate values. Optional config items are marked.

**Engine** uses GitHub OAuth for authentication, so you'll need to set up a [new GitHub OAuth application](https://github.com/settings/applications/new) and place the keys in your **UserConfig.groovy**. The **Authorization callback URL** should be [http://[local-machine-IP]:8080/oauth/github/callback](http://[local-machine-IP]:8080/oauth/github/callback).

### Database
Run a local MySQL instance with Docker by running the following:
```bash
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=docker -e MYSQL_DATABASE=mysql mysql
```

#### Database Migrations
Database migration diffs can be created with a local MySQL instances using the following command:
```bash
grails prod -DRDS_PASSWORD=[MySQL root password] -DRDS_HOSTNAME=[MySQL host ip] dbm-gorm-diff --add [filename].groovy
```

Migrations can be tested on production data by exporting the prod database to SQL, loading it into a local DB, and executing the migrations with the following:
```bash
mysqldump -h [prod DB host] -u [username] -p [password] [database] > backup.sql
mysql -h [prod DB host] -u [username] -p [password] [database] < backup.sql
grails prod -DRDS_PASSWORD=[password] -DRDS_HOSTNAME=[database hostname] dbm-changelog-sync
```

### Local Docker
An example local Docker `run` command is:
```bash
docker run -d --name=alfred -p 8080:8080 274685854631.dkr.ecr.us-east-1.amazonaws.com/alfred:latest
```

### Running
Run the application with `grails run-app`. After the app starts go to [http://[local-machine-IP]:8080](http://[local-machine-IP]:8080), authorize the GitHub OAuth application, then you'll be redirected back to the running application.

## Running with Alfred
**Alfred** is most easily run locally using Docker. Follow the steps below to configure **Alfred**:
- Run **Alfred** locally using the [Platform](https://github.com/AugurWorks/Platform) repo **Run Without Building** steps
- Add the `alfred.url` config item with the appropriate URL
- Run the application normally
