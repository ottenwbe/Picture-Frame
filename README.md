# Picture-Frame
Smart Picture Frame 

[![Build Status](https://travis-ci.org/ottenwbe/smart-picture-frame.svg?branch=master)](https://travis-ci.org/ottenwbe/smart-picture-frame)
[![codecov](https://codecov.io/gh/ottenwbe/smart-picture-frame/branch/master/graph/badge.svg)](https://codecov.io/gh/ottenwbe/smart-picture-frame)
[![Known Vulnerabilities](https://snyk.io/test/github/ottenwbe/smart-picture-frame/badge.svg)](https://snyk.io/test/github/ottenwbe/ottenwbe.github.io)

WIP project: The backend service for an IoT enabled picture frame!

Current Status: __Prototype__

# Build

    mvn install

# Use

1. Configure the picture folder in t he application.yml:
        
    ```yaml
    images:
      sync:
        path: "<your image location>"
    ```
    
2. Start the service, e.g.:
    
    ```bash
    mvn spring-boot:run
    ```
    
3. Open your browser at:

    http://localhost:8080
    
# Test
    
    mvn verify