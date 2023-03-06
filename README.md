**RECIPE APPLICATION**

**ARCHITECTURE**

Programming Language: Java17

Framework: Spring Boot 3

Database: MongoDB

Queue: Kafka

Search: Elasticsearch

Integration Test: Testcontainers

**Why This Architecture**

I chose Java17 and Spring Boot to develop rest service. I preferred MongoDB to store json as is for performance reasons. Elasticsearch was chosen because requirements contain some complex searching operations. I need to sync MongoDB and Elasticsearch. I chose Kafka queue to make this integration async. I used Testcontainers to test integrations to see actual behaviours.

![architecture.png](src%2Fmain%2Fresources%2Farchitecture.png)

**HOW TO RUN**

docker-compose up -d

I have found recipes json for sample data. In order to 
import it call below request:

`curl --location --request GET 'localhost:8080/search?serving=4&ingredientInc=potatoes' \
--data-raw ''`

CRUD Samples

**CREATE**

`curl --location --request POST 'localhost:8080/recipe' \
--header 'Content-Type: application/json' \
--data-raw '{
"name": "QWERT",
"instructions": "QWERTY",
"ingredients": ["Q", "W"],
"vegetarian": false,
"serving": 1
}'`

**READ**

`curl --location --request GET 'localhost:8080/recipe/6405b33454fb5c26be046559'`

**UPDATE**

`curl --location --request PUT 'localhost:8080/recipe/6405b33454fb5c26be046559' \
--header 'Content-Type: application/json' \
--data-raw '{
"name": "QWERT",
"vegetarian": false,
"serving": 1,
"ingredients": [
"Q",
"W"
],
"instructions": "QWERT"`

**DELETE**

`curl --location --request DELETE 'localhost:8080/recipe/6405b33454fb5c26be046559'`

**SEARCH**

`curl --location --request GET 'localhost:8080/search?vegetarian=false&serving=4&instructions=oven&ingredientInc=potatoes' \
--data-raw ''`