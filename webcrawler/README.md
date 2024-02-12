Web crawler:

Execution:
in order to be able to run the program, you need to run docker-compose up --build
After that you can execute the java -jar web-crawler-1.0.jar command in the terminal
    
    java -jar web-crawler-1.0.jar https://www.google.com 3

About the implementation:
* Kafka is used to send the links to the consumer and the consumer is used to process the links and send the result to the database
* Redis is used to store the links and the result of the links, in order to avoid duplication 
* TsvBatchWriter is used to write the result to the file batch by batch

The program is implemented in Java 21 and uses the following libraries:
- Jsoup: for parsing the HTML content
- Docker: for running the program in a container
- Maven: for building the program
- Log4j: for logging the program
- Spring Boot: for running the program
- Lombok: for reducing the boilerplate code
- Docker Compose: for running the program in a container
- Dockerfile: for building the program in a container
- Docker Hub: for storing the container image
- GitHub: for storing the source code

Limitations:
* HTML type not enforced
* Batch size supposed to be as a config param
* Handling save the bach file at the end (not exceed the limit) 
* Known Issue - prevent duplication also when different depths
* Handle also bulk links by message broker 

Improvements:
* Exception handling

Assumptions:
