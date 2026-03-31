To create the file .war and the package target, we need to always do this command (after changing the code, etc):

```bash
mvn clean package
```

Then we proceed for the next instructions

## Running locally

### Pre start

First, we need to start the emulators in one terminal:

```bash
 gcloud beta emulators datastore start
```

Then, in the other terminal, we have to do this commands (in CMD):

```bash
gcloud config set project adc-individual-project-68231
```

```bash
set DATASTORE_USE_PROJECT_ID_AS_APP_ID=true
set DATASTORE_DATASET=adc-individual-project-68231
set DATASTORE_EMULATOR_HOST=localhost:8081
set DATASTORE_EMULATOR_HOST_PATH=localhost:8081/datastore
set DATASTORE_HOST=http://localhost:8081
set DATASTORE_PROJECT_ID=adc-individual-project-68231
```

### Start the local App Engine dev server with:

```bash
mvn appengine:run
```

Then open your browser and go to:

```
http://localhost:8080/
```

### Testing with curl (alternative on terminal)

```bash
# Successful login
curl -i -X POST http://localhost:8080/rest/login/ \
  -H "Content-Type: application/json" \
  -d '{"username":"jleitao","password":"password"}'

# Failed login
curl -i -X POST http://localhost:8080/rest/login/ \
  -H "Content-Type: application/json" \
  -d '{"username":"someother","password":"pass"}'

# Check username availability
curl http://localhost:8080/rest/login/jleitao
curl http://localhost:8080/rest/login/someother
```

## Deploying to Google App Engine

```bash
gcloud auth login
```

and then

```bash
gcloud config set project adc-individual-project-68231
```

### Deploy

```bash
mvn appengine:deploy -Dapp.deploy.projectId=adc-individual-project-68231 -Dapp.deploy.version=1
```

After a successful deploy, your app will be live at:

```
https://adc-individual-project-68231.appspot.com/
```
