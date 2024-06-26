# Open Foris Collect

Easy and flexible survey design and data management

Open Foris Collect is the main entry point for data collected in field-based inventories. It provides a fast, easy, flexible way to set up a survey with a user-friendly interface.
Collect handles multiple data types and complex validation rules, all in a multi-language environment.

Developed under the [Open Foris Initiative](https://www.openforis.org)

## Key Features

* **User Friendliness**: Nice web interface; Designed based on real users’ needs, No need for technical skills to use it.
* **Rapid Data Entry**: Limited use of mouse needed; Data entry using only keyboard; Auto-complete; Species list search; Immediate feedback on errors/warnings.
* **Highly Configurable**: Design the survey from scratch or starting from a template; Data entry user interface is automatically generated and metadata driven; Validation rules (distance, comparison, pattern...); Multiple layouts (form, table, multiple columns form).
* **Multiple data types**: Basic Types – Text, Number, Boolean, Date, Time. Complex types – Range, Coordinate, File, Taxon. Plus, support for calculated values.
* **Multi-user or standalone**: It can be used in a standalone environment with no need for internet connection; Data can be exported from single/standalone installations and imported into a centralized installation to create a complete data set; In multi-user environment, users can work only on owned records.
* **Controlled QA workflow**: Record goes through different steps: Data entry, Data cleansing, Data analysis. Minimized "data cooking". 
* **Rich metadata**: XML format, Complex nested structure of the survey, Validation rules, Multiple Spatial Reference Systems.
* **Multilingual**: Define the survey in multiple languages - Tab labels, Input field labels, Validation messages, Code item labels, Element info tooltips. The user will see the survey in the language of his/her web browser or in the survey default language.
* **Multiple data export/import formats**: XML, CSV, Relational database. 

## Where to download the installer?

If you are not interested in the code but rather on the Collect features you might want to run it right away!
Go to our [website](https://www.openforis.org/tools/collect.html) and download the installer directly there. There are versions for Windows, Mac OS X and Linux 32-bit and 64-bit. 


## Install and run Collect as a Docker container

### Prerequisites

- [download and install Docker](https://www.docker.com/). Docker is an open platform for developing, shipping, and running applications.

- Create a docker network that the collect application can use to communicate with the database we will create in the next step:

```console
$ docker network create collect
```

- Install a local database (PostgreSQL) as a Docker container. Run this command from command line; it will create also a database named 'collect' and a user 'collect' with password 'collect123' and will make the DBMS listen on port 5432 (you can change those parameters as you wish):

```console
$ docker run -d --network=collect --name collect-db -p 127.0.0.1:5432:5432 -e POSTGRES_DB=collect -e POSTGRES_PASSWORD=collect123 -e POSTGRES_USER=collect postgis/postgis:12-3.4
```
You can also use an already existing PostgreSQL database installed in a different way and configure Collect to connect to it.

### Prepare a file with the parameters to pass to Collect

The file (call it **collect.env**) must be a text file with this content:

```properties
COLLECT_DB_DRIVER=org.postgresql.Driver
COLLECT_DB_URL=jdbc:postgresql://collect-db:5432/collect
COLLECT_DB_USERNAME=collect
COLLECT_DB_PASSWORD=collect123
```

### Install and run Collect
Running the following command from command line will install Collect as a Docker container:

```console
$ docker run -m 4GB --network=collect -p 8080:8080 --env-file ./collect.env openforis/collect:latest
```
You can run this command in the same folder where you have defined the collect.env file or specify its path in the command, in the '--env-file' parameter.
Collect will start on the port specified in the collect.env file (8080 by default).
You can use the same command to start up Collect again once you stop it.

### Open Collect in the web browser
Once the Collect Docker container is started, you can access Collect user interface through your web browser at the address: <http://localhost:8080/collect>

## Do you have any questions?

Please register into our [Community Support Forum](https://www.openforis.support) and raise your question or feature request there. 

## License

Collect and the rest of the Open Foris tools follow the MIT License, meaning that you can do anything you want with the code! Of course we appreciate references to our project, [Open Foris](https://www.openforis.org)
