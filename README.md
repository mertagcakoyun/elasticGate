# Dynamic Elasticsearch Exposer

## Overview

Dynamic Elasticsearch Exposer is a project designed to manage and expose Elasticsearch queries with dynamic parameters. This allows users to interact with Elasticsearch in a flexible way, using parameters such as `startDate`, `endDate`, and `status`. The project aims to eliminate the need for frequent code changes when displaying dynamic query results on the UI or Swagger.

## Purpose

The primary goal is to provide a convenient way to view reports on the UI without requiring code modifications. The project enables teams to create custom endpoints with their Elasticsearch queries and connection details, fostering flexibility and ease of management.


## Technologies

- Ktor
- Elasticsearch Asynchronous
- Couchbase

## Example Elasticsearch Query

```json
{
  "track_total_hits": true,
  "size": 10,
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "doc.product.deleted": {
              "value": false
            }
          }
        },
        {
          "range": {
            "doc.product.receivedAt": {
              "gte": "2023-06-21T00:40:02.000Z"
            }
          }
        }
      ]
    }
  },
  "aggs": {
    "data": {
      "date_histogram": {
        "field": "doc.product.receivedAt",
        "calendar_interval": "month"
      }
    }
  }
}

```
Let's first save Elasticsearch connection information:
``` 
POST /connections/add-connection
{
    "name":"stage_connection",
    "hosts":["esHost1", "esHost2"] ,
    "port":9200
}
```

You can check whether system has successfully created your connection using the following endpoint:

```
GET /connections/connection-by-name/stage_connection
```

If the endpoint returns the host information for the connection you want to create, proceed to create your Elasticsearch query with dynamic parameters.

```
{
  "name": "your-query-name",
  "connectionNames": "stage_connection",
  "index": "your-elastic-index-name",
  "query": "{\"track_total_hits\":true,\"size\":$SIZE,\"query\":{\"bool\":{\"must\":[{\"term\":{\"doc.product.deleted\":{\"value\":$DELETED}}},{\"range\":{\"doc.product.receivedAt\":{\"gte\":\"$RECEIVED_AT\"}}}]}},\"aggs\":{\"data\":{\"date_histogram\":{\"field\":\"doc.product.receivedAt\",\"calendar_interval\":\"$CALENDAR_INTERVAL\"}}}}", // You can add your dynamic parameter using $ prefix.
  "dynamicParameters": [
    {
      "name": "SIZE",
      "type": "integer",
      "default": "10"
    },
    {
      "name": "DELETED",
      "type": "boolean",
      "default": "true"
    },
    {
      "name": "RECEIVED_AT",
      "type": "date",
      "default": "now-10d"
    },
    {
      "name": "CALENDAR_INTERVAL",
      "type": "combo",
      "values": ["day", "month", "year"]
    }
  ]
}
```

You can check whether the system has successfully created your query from the endpoint below.

```
GET /queries/query-by-name/your-query-name
```

If the query is created successfully in the system. Now you can integrate the following endpoint into your applications and get the same query result from our endpoint as you get from elasticsearch.
```
GET /queries/connections/stage_connection/queries/your-query-name
{
    "parameters": {
      "SIZE": "20",
      "DELETED": "false",
      "RECEIVED_AT": "now-100d",
      "CALENDAR_INTERVAL": "month"
    }
}
```

# Endoints 

## Connection collection [/connections]

### List all connection [GET] [/connections]

List connections

+ Response 200 (application/json)

### List specified connection [GET] [/connections/connection-by-name/{name}]

Get specified connection information

+ Response 200 (application/json)

### Delete specified connection [DELETE] [/connections/delete-connection/{name}]

Delete specified connection information

+ Response 204 No Content (application/json)

### Add connection [POST] [/connections/add-connection/]

Add connection information

+ Response 200 (application/json)
+ Request:
```
{
    "name":"dummy1",
    "hosts":["esHost1", "esHost2"] ,
    "port":9200
}

```

## Query collection [/queries]

### List all queries [GET] [/queries]

List queries

+ Response 200 (application/json)

### List specified query [GET] [/queries/query-by-name/{name}]

Get specified connection information

+ Response 200 (application/json)

### Add query [POST] [/queries/add-query/]

Add query information

+ Response 200 (application/json)
+ Request:
```
{
  "name": "your-query-name",
  "connectionNames": "stage_connection",
  "index": "your-elastic-index-name",
  "query": "{\"track_total_hits\":true,\"size\":$SIZE,\"query\":{\"bool\":{\"must\":[{\"term\":{\"doc.product.deleted\":{\"value\":$DELETED}}},{\"range\":{\"doc.product.receivedAt\":{\"gte\":\"$RECEIVED_AT\"}}}]}},\"aggs\":{\"data\":{\"date_histogram\":{\"field\":\"doc.product.receivedAt\",\"calendar_interval\":\"$CALENDAR_INTERVAL\"}}}}", // You can add your dynamic parameter using $ prefix.
  "dynamicParameters": [
    {
      "name": "SIZE",
      "type": "integer",
      "default": "10"
    },
    {
      "name": "DELETED",
      "type": "boolean",
      "default": "true"
    },
    {
      "name": "RECEIVED_AT",
      "type": "date",
      "default": "now-10d"
    },
    {
      "name": "CALENDAR_INTERVAL",
      "type": "combo",
      "values": ["day", "month", "year"]
    }
  ]
}

```

### Add query with specified host [POST] [/queries/add-elastic-query-with-specific-host]

Add query information

+ Response 200 (application/json)
+ Request:
```
{
  "name": "get_deleted_products-v3",
  "connectionName": "qc-stage-v2",
  "hosts": ["esHost1","esHost2"], // if any host exist, system will map your connectionName
 "port":9200,
  "index": "qc-cb-observer-product-history",
  "query": "{\"track_total_hits\":true,\"size\":$SIZE,\"query\":{\"bool\":{\"must\":[{\"term\":{\"doc.product.deleted\":{\"value\":$DELETED}}},{\"range\":{\"doc.product.receivedAt\":{\"gte\":\"$RECEIVED_AT\"}}}]}},\"aggs\":{\"data\":{\"date_histogram\":{\"field\":\"doc.product.receivedAt\",\"calendar_interval\":\"$CALENDAR_INTERVAL\"}}}}",
  "dynamicParameters": [
    {
      "name": "SIZE",
      "type": "integer",
      "default": "10"
    },
    {
      "name": "DELETED",
      "type": "boolean",
      "default": "true"
    },
    {
      "name": "RECEIVED_AT",
      "type": "date",
      "default": "now-1d/d"
    },
    {
      "name": "CALENDAR_INTERVAL",
      "type": "combo",
      "values": ["day", "month", "year"]
    }
  ]
}

```


### Update query [PUT] [/queries/update-query/{name}]

Update query information

+ Response 200 (application/json)
+ Request:
```
{
  "name": "your-query-name", // should be same with exist name. 
  "connectionNames": "stage_connection",
  "index": "your-elastic-index-name",
  "query": "{\"track_total_hits\":true,\"size\":$SIZE,\"query\":{\"bool\":{\"must\":[{\"term\":{\"doc.product.deleted\":{\"value\":$DELETED}}},{\"range\":{\"doc.product.receivedAt\":{\"gte\":\"$RECEIVED_AT\"}}}]}},\"aggs\":{\"data\":{\"date_histogram\":{\"field\":\"doc.product.receivedAt\",\"calendar_interval\":\"$CALENDAR_INTERVAL\"}}}}", // You can add your dynamic parameter using $ prefix.
  "dynamicParameters": [
    {
      "name": "SIZE",
      "type": "integer",
      "default": "10"
    },
    {
      "name": "DELETED",
      "type": "boolean",
      "default": "true"
    },
    {
      "name": "RECEIVED_AT",
      "type": "date",
      "default": "now-10d"
    },
    {
      "name": "CALENDAR_INTERVAL",
      "type": "combo",
      "values": ["day", "month", "year"]
    }
  ]
}

```


### Delete specified query [DELETE] [/queries/delete-query/{name}]

Delete specified query information

+ Response 204 No Content (application/json)

### Execute query result [GET] [queries/connections/{connectionName}/queries/{name}]

Execute query information

+ Response 200 (application/json)
+ Request:
```
{
    "parameters": {
      "SIZE": "20",
      "DELETED": "false",
      "RECEIVED_AT": "now-1d",
      "CALENDAR_INTERVAL": "month"
    }
}

```
+ Response 200 (application/json)

For empty elastcisearch result.
```
{
    "took": 90,
    "timed_out": false,
    "_shards": {
        "total": 4,
        "successful": 4,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 0,
            "relation": "eq"
        },
        "max_score": null,
        "hits": []
    },
    "aggregations": {
        "data": {
            "buckets": []
        }
    }
}





