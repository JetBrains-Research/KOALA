# Server

The server is built on the Ktor framework and serves as the backend component for the KOALA system. It receives data from the plugin, processes it, and stores it in both a PostgreSQL database and a local file system in a directory structure.

## Data Processing and Storage

The server handles data sent from the plugin in the following ways:

- **Standard data formats** are stored completely in the PostgreSQL database for efficient querying and processing
- **Additional data** (such as logs from third-party utilities or plugins) are stored only in the directory format
- All data, including standard formats, are also saved as directories in the specified path for backup and direct access purposes

The server organizes the received data in a structured directory hierarchy in the file system, making it easy to navigate and access when needed. Customization of the path for saving will be available in future updates.

## Running the Server

There are two convenient ways to run the server:

### Using Pre-built Docker Image

For a quick setup, you can use our pre-built Docker image:

```
registry.jetbrains.team/p/tasktracker-3/sharable/tasktracker-server:latest
```

You can also find a [docker compose](../docker-compose.yml) file that provides a template to start a Docker container with all necessary configurations.

### Building Locally with Dockerfile

Alternatively, you can build the Docker image locally using the provided [Dockerfile](../Dockerfile) and [docker-compose.yml](../docker-compose.yml) files in the repository root.

## Environment Variables

The server requires the following environment variables:

- `DB_URL` - PostgreSQL database URL in format `postgresql://[user[:password]@][netloc][:port][/dbname]`
- `DB_USERNAME` - username for the database
- `DB_PASSWORD` - password for the database
