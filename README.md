# CS166-B22-Summer2020
Note: Template code for create.sql, and MechanicShop.java as well as .csv data files to test code was provided by the University of California, Riverside.

# Phase 3 Compaliation Instruction

1. Setup server and table
    1. cd code
    2. cd postgresql
    3. chmod +x *.sh
    4. ./startPostgreSQL.sh
    5. ./createPostgreDB.sh
2. Run Java Script
    1. cd code
    2. cd java
    3. chmod +x *.sh
    4. ./compile.sh
    5. ./run.sh $LOGNAME"_DB" 5432 $USER
3. Exit Server
    1. cd code
    2. cd postgresql
    3. ./stopPostgreDB.sh
