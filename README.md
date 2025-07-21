# auth service

auth service that stores user and their data, also create jwt token with roles user for authorized in deal and contractor services. 
this service send token from header. the whole logic of working with an authorized user in deal and contractor repository.


for starting: 
-   mvn -B clean package -DskipTests &&  docker-compose down -v --remove-orphans && docker-compose build --no-cache app && docker-compose up
for test:
- start test in program
