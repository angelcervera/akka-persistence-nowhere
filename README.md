# Akka Persistence Plugin to Nowhere

[ ![Download](https://api.bintray.com/packages/angelcervera/maven/akka-persistence-nowhere/images/download.svg) ](https://bintray.com/angelcervera/maven/akka-persistence-nowhere/_latestVersion)

The only thing that you can store in nowhere is nothing.

The idea of this plugin is to disable the persistence. 

# What is the plugin for?
Basically, the implementation is doing nothing. It means that using it, all call to persist and recover events are ignored.

# When to use it?
***Never in production***. As you can imagine, using it in production will not recover correctly in case of failure. It means, your system will not resilient.

So why this plugin? There are (weird) test cases that you want to remove the persistence.

The persistence storage system is something 100% parametrizable, so is a part that can be optimized independently and only in mature stages of development.

One example: You want to check the performance of the algorithm that changes the state of your actor system ignoring the penalty that the journal persistence. In this case, using other plugins, like the in-memory journal, is not an option because you are going to consume all memory necessary for the actor system and you will add a GC penalty.

# How to use it?
It is working as any other persistence plugin.

## Import
Two options:
- Adding to your classpath in execution time.
- In your `build.sbt`
  ```scala
  resolvers += "osm4scala repo" at "http://dl.bintray.com/angelcervera/maven" // If it's not found in the main maven repository. 
  libraryDependencies += "com.acervera.akka" %% "akka-persistence-nowhere" % "1.0.1"
  ```

## Config
Overwriting your default `application.conf`
```hoco
akka {
  persistence {
    journal.plugin = "disable-journal-store"
    snapshot-store.plugin = "disable-snapshot-store"
  }
}

disable-journal-store {
  class = "com.acervera.akka.persistence.nowhere.AkkaPersistenceNowhereJournal"
  plugin-dispatcher = "akka.persistence.dispatchers.default-plugin-dispatcher"
}

disable-snapshot-store {
  class = "com.acervera.akka.persistence.nowhere.AkkaPersistenceNowhereSnapshotStore"
  plugin-dispatcher = "akka.persistence.dispatchers.default-plugin-dispatcher"
}
```

