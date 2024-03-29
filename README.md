# Engine
AugurWorks UI Engine 2.0

## Requirements
Requires **alfred:2.0.0** or later

## Deployment
A production environment can be built which uses environment variables for all customizable config values. The environment variables are listed below:

- **BARCHART_KEY** - Barchart API key
- **BUCKET** (default: aw-files-dev) - Bucket for log file placement
- **CHANNEL** (default: #testing) - Slack channel for prediction output
- **ENV** (default: DEV) - Environment field for logging and RabbitMQ channel postfix
- **LOGDNA_INGEST_KEY** - LogDNA API key
- **HOSTNAME** - Hostname for logs
- **ML_MAX** (default: 10) - Maximum simultaneous machine learning runs
- **OAUTH_KEY** - GitHub OAuth key
- **OAUTH_SECRET** - GitHub OAuth secret
- **RABBITMQ_USERNAME** (default: guest) - RabbitMQ username
- **RABBITMQ_PASSWORD** (default: guest) - RabbitMQ password
- **RABBITMQ_HOST** (default: rabbitmq) - RabbitMQ hostname
- **RABBITMQ_PORT** (default: 5672) - RabbitMQ port
- **RDS_USERNAME** (default: root) - MySQL username
- **RDS_PASSWORD** - MySQL password
- **RDS_HOSTNAME** - MySQL host
- **RDS_PORT** (default: 3306) - MySQL port number
- **RDS_DB_NAME** (default: engine) - MySQL DB name
- **SERVER_URL** (default: http://[local-ip]:8080) - Full deployed application URL
- **SNS_TOPIC_ARN** - SNS Topic Name, only used if **LAMBDA_ON** is true
- **SQS_NAME** - SQS Queue Name, only used if **LAMBDA_ON** is true
- **TD_KEY** - TD API key
- **RAPI_URL** - RAPI server location

## Local Development
### Dependencies
- [Grails 2.4.5](https://grails.org/download.html) (SKDMAN! which is mentioned on that page is highly recommended)
- [Docker](https://docs.docker.com/engine/installation/) (Optional)

### Setup
**Engine** uses GitHub OAuth for authentication, so you'll need to set up a [new GitHub OAuth application](https://github.com/settings/applications/new) and add the keys as environment variables matching those above. The **Authorization callback URL** should be [http://[local-machine-IP]:8080/oauth/github/callback](http://[local-machine-IP]:8080/oauth/github/callback).

### Database
Run a local MySQL instance with Docker by running the following:
```bash
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=docker -e MYSQL_DATABASE=engine mysql
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

A fresh Docker container can be built by running the following:
```bash
docker build -t engine .
```

### Running
Run the application with `grails run-app`. After the app starts go to [http://[local-machine-IP]:8080](http://[local-machine-IP]:8080), authorize the GitHub OAuth application, then you'll be redirected back to the running application.

## Running with Alfred
**Alfred** is most easily run locally using Docker. Follow the steps below to configure **Alfred**:
- Run **Alfred** locally using the [Platform](https://github.com/AugurWorks/Platform) repo **Run Without Building** steps
- Run the application normally
