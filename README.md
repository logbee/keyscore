# keyscore
## Install Cassandra under Windows 7
Download v3.9.0 MSI Installer from:
https://academy.datastax.com/planet-cassandra/cassandra

After the installations follow these steps:

1. Open file: DataStax-DDC\apache-cassandra\conf\cassandra.yaml

2. Uncomment the `cdc_raw_directory` and set new value to:

    `cdc_raw_directory: "C:/Program Files/DataStax-DDC/data/cdc_raw"`

3. Step 3: Goto Windows Services and Start the DataStax DDC Server 3.9.0 Service