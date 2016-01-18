# Engine
AugurWorks UI Engine 2.0

## Local Development
### Dependencies
- [Grails 2.4.5](https://grails.org/download.html) (SKDMAN! which is mentioned on that page is highly recommended)
- [Docker](https://docs.docker.com/engine/installation/) (Optional)

### Setup
After cloning the repo copy **UserConfig.groovy.example** to **UserConfig.groovy** and move it one folder up from this project's root folder. Fill in the appropriate values. Optional config items are marked.

**Engine** uses GitHub OAuth for authentication, so you'll need to set up a [new GitHub OAuth application](https://github.com/settings/applications/new) and place the keys in your **UserConfig.groovy**. The **Authorization callback URL** should be [http://[local-machine-IP]:8080/oauth/github/callback](http://[local-machine-IP]:8080/oauth/github/callback).

### Running
Run the application with `grails run-app`. After the app starts go to [http://[local-machine-IP]:8080](http://[local-machine-IP]:8080), authorize the GitHub OAuth application, then you'll be redirected back to the running application.
