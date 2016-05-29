# JDrive
JDrive is application so you can mount your google drive on any platform that support Java.

|Status | Coverage | Branch |
|-------|----------|--------|
|[![Build Status](https://travis-ci.org/davidmaignan/JDrive.svg?branch=master)](https://travis-ci.org/davidmaignan/JDrive)|[![Code Coverage](https://img.shields.io/codecov/c/github/pvorb/property-providers/develop.svg)](https://codecov.io/gh/davidmaignan/JDrive?branch=master)| Master|

### Prerequisites
- Java 8
- Access to the internet and a web browser
- A Google account with Google Drive enabled

### Turn on the Drive API
- Use [this wizard] (https://console.developers.google.com/start/api?id=drive) to create or select a project in the Google Developers Console and automatically turn on the API. Click Continue, then Go to credentials.
- At the top of the page, select the OAuth consent screen tab. Select an Email address, enter a Product name if not already set, and click the Save button.
- Select the Credentials tab, click the Create credentials button and select OAuth client ID.
- Select the application type Other, enter the name "Drive API Quickstart", and click the Create button.
- Click OK to dismiss the resulting dialog.
- Click the file_download (Download JSON) button to the right of the client ID.
- Move this file to directory: main/ressources and rename it client_secret.json.

### Installation

```
git clone https://github.com/davidmaignan/JDrive.git
cd JDrive
```

configure the root folder in **resources/config.properties**

```
./gradlew run
```

### First run
The first time you run the sample, it will prompt you to authorize access:

- The sample will attempt to open to open a new window or tab in your default browser. If this fails, copy the URL from the console and manually open it in your browser.

- If you are not already logged into your Google account, you will be prompted to log in. If you are logged into multiple Google accounts, you will be asked to select one account to use for the authorization.
- Click the Accept button.
- The sample will proceed automatically, and you may close the window/tab.

###The current version only supports pull requests.
```
To be added in future versions 
- push modifications
- versionning
- shared documents
- push notifications (if free of charge)
```
