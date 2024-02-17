# doomig

Welcome to doomig.  It is a migration tool for database schemas.  
This is in response to 2 things
* Tired of migration tools costing money for commonly used features
* Wanting to dive more into Scala 3

It supports
* Postgresql (soon it will support MySql)
* migrations up and down based upon files in a directory following
a specific naming convention.  
  * This convention can be user defined but must include some named fields in the regex. 
* Dry runs that validate the schema using [doobie typechecking](https://tpolecat.github.io/doobie/docs/06-Checking.html)

It is definitely still in the beginning days so there will be some movement here and there.

## Code Wise
It uses [Scala3](https://dotty.epfl.ch/), [doobie](https://github.com/tpolecat/doobie) with a tagless final style
that uses [cats-effect](https://typelevel.org/cats-effect/).

## Run application
Add ddl files for up and down to a directory.  By default, you should follow this
naming [convention](core/src/test/resources/migrations) for the files.
You can override this naming convention by defining your own up and down [regex](core/src/main/scala/com/strad/doomig/service/FileDiscoveryService.scala).  The regex *must* have the following named groups.
* `version`
* `desc`

| req | Argument              | Value                                                          |
|-----|-----------------------|----------------------------------------------------------------|
| Y   | 0                     | up or down                                                     |
| Y   | --source-folder       | Directory to search migration files for                        |
| N   | --destination-version | Version you want to migrate to                                 |
| N   | --db-table-name       | Name of the migration table.  Defaults to doomig_version       |
| Y   | --db-url              | Jdbc url                                                       |
| Y   | --db-user             | database user name                                             |
| Y   | --db-password         | database password                                              |
| N   | --db-driver           | database driver; defaults to org.postgresql.Driver             |
| N   | --dry-run             | Only tells you what would run and checks the ddl               |
| N   | --file-regex          | Needs to be the regex for the --direction specified (up, down) |
| Y   | --help                | Prints out the help                                            |


Up to date values are defined in [Conf.scala](app/src/main/scala/com/strad/doomig/app/Conf.scala)


```shell
sbt run app/run
```

## Run Unit tests tests

```shell
sbt core/test
```
