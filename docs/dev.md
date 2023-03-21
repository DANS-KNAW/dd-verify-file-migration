Development
===========
This page contains information for developers about how to contribute to this project.

Set-up
------
This project can be used in combination with  [dans-dev-tools]{:target=_blank}. Before you can start it as a service
some dependencies must first be started:

### HSQL database

Open a separate terminal tab:

```commandline
start-hsqldb-server.sh
```

### dd-dtap

The service needs a Dataverse instance to talk to. For this you can use [dd-dtap]{:target=_blank} (only accessible to DANS developers):

```commandline
start-preprovisioned-box -s
```

After start-up:

```commandline
vagrant ssh
curl -X PUT -d s3kretKey http://localhost:8080/api/admin/settings/:BlockedApiKey
curl -X PUT -d unblock-key http://localhost:8080/api/admin/settings/:BlockedApiPolicy
```

### dd-verify-migration

Open a separate terminal and cd to the `dd-verify-migration`:

```commandline
start-env.sh
```

Configure the correct API key in the created `etc/config.yml`.
```commandline
start.sh load-from-dataverse
```


[dans-dev-tools]: https://github.com/DANS-KNAW/dans-dev-tools#dans-dev-tools

[dans-datastation-tools]: https://github.com/DANS-KNAW/dans-datastation-tools#dans-datastation-tools

[dd-dtap]: https://github.com/DANS-KNAW/dd-dtap