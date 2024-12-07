# Digital Course Marketplace

## Prerequisite
1) Intellij
2) Java21
3) Maven

## Setup Instructions

1. Clone the Repository:
   ```bash
   git clone https://github.com/your-repo/marvel.git
   cd smit

2. Build Application: 
    ```bash
    mvn clean install

3. Run Application:
    ``` bash 
    mvn spring-boot:run

4. Build Docker Image : 
    ```bash
    docker build -t smit-api .

5. Run Docker Image: 
   ```bash 
    docker run -p 8080:8080 smit-api
