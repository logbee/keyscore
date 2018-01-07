#Frontier API Documentation
## Streams
### Create a new Stream

post::<server_address>/stream

payload: JSON coded Stream object:

```json
{
  "id":"exampleID",
  "source":{
    "source_type":"kafka_source",
    "bootstrap_server":"",
    "source_topic":"",
    "group_ID":"first-keyscore-consumer",
    "offset_commit":"<earliest | latest>"
  },
  "sink":{
    "sink_type":"kafka_sink",
    "sink_topic":"",
    "bootstrap_server":""
  },
  "filter":[
    {
      "filter_type":"add_fields",
      "fields_to_add":{
        "test_field":"es klappt"
      }
    }
  ]
}