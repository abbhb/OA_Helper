# es相关内容
mysql需开启binlog
依赖
cannal-server1.1.6
```shell
docker run -p 11111:11111 --name canal \\n-e canal.destinations=example \\n-e canal.instance.master.address=192.168.12.12:13306  \\n-e canal.instance.dbUsername=canal  \\n-e canal.instance.dbPassword=canal  \\n-e canal.instance.connectionCharset=UTF-8 \\n-e canal.instance.tsdb.enable=true \\n-e canal.instance.gtidon=false  \\n-e canal.instance.filter.regex=.\*\\\\..\* \\n--network host \\n--restart=always \\n-d canal/canal-server:v1.1.6
```
canal-adapter1.1.6

同级目录下提供的conf均为对应组件所需conf

## 全量同步接口
```shell
curl http://localhost:18081/etl/es7/user_v1.yml -X POST\n
```


## user表缓存 es索引结构
```shell
PUT /user_v1
{
  "settings": {
    "index": {
      "max_result_window": "100000",
      "refresh_interval": "5s",
      "number_of_shards": "5",
      "translog": {
        "flush_threshold_size": "1024mb",
        "sync_interval": "30s",
        "durability": "async"
      },
      "number_of_replicas": "1"
    },
    "analysis": {
      "analyzer": {
        "ik_t2s_pinyin_analyzer": {
          "type": "custom",
          "tokenizer": "ik_max_word",
          "filter": [
            "pinyin_filter",
            "lowercase"
          ]
        },
        "stand_t2s_pinyin_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "pinyin_filter",
            "lowercase"
          ]
        },
        "ik_t2s_analyzer": {
          "type": "custom",
          "tokenizer": "ik_max_word",
          "filter": [
            "lowercase"
          ]
        },
        "stand_t2s_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase"
          ]
        },
        "ik_pinyin_analyzer": {
          "type": "custom",
          "tokenizer": "ik_max_word",
          "filter": [
            "pinyin_filter",
            "lowercase"
          ]
        },
        "stand_pinyin_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "pinyin_filter",
            "lowercase"
          ]
        },
        "keyword_t2s_pinyin_analyzer": {
          "filter": [
            "pinyin_filter",
            "lowercase"
          ],
          "type": "custom",
          "tokenizer": "keyword"
        },
        "keyword_pinyin_analyzer": {
          "filter": [
            "pinyin_filter",
            "lowercase"
          ],
          "type": "custom",
          "tokenizer": "keyword"
        }
      },
      "filter": {
        "pinyin_filter": {
          "type": "pinyin",
          "keep_first_letter": true,
          "keep_separate_first_letter": false,
          "keep_full_pinyin": false,
          "keep_joined_full_pinyin": true,
          "keep_none_chinese": true,
          "none_chinese_pinyin_tokenize": true,
          "keep_none_chinese_in_joined_full_pinyin": true,
          "keep_original": false,
          "limit_first_letter_length": 1000,
          "lowercase": true,
          "trim_whitespace": true,
          "remove_duplicated_term": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "name": {
        "type": "text",
        "index_phrases": true,
        "analyzer": "ik_max_word",
        "index": true,
        "fields": {
          "keyword": {
            "ignore_above": 256,
            "type": "keyword"
          },
          "stand": {
            "analyzer": "standard",
            "type": "text"
          },
          "STPA": {
            "type": "text",
            "analyzer": "stand_t2s_pinyin_analyzer"
          },
          "ITPA": {
            "type": "text",
            "analyzer": "ik_t2s_pinyin_analyzer"
          }
        }
      },
      "username": {
        "type": "text"
      },
      "email": {
        "type": "keyword"
      },
      "sex": {
        "type": "keyword"
      },
      "avatar": {
        "type": "text"
      },
      "student_id": {
        "type": "keyword"
      },
      "dept_id": {
        "type": "keyword"
      },
      "status": {
        "type": "keyword"
      },
      "phone": {
        "type": "keyword"
      },
      "create_user": {
        "type": "keyword"
      },
      "dept_name": {
        "type": "text",
        "index_phrases": true,
        "analyzer": "ik_max_word",
        "index": true,
        "fields": {
          "keyword": {
            "ignore_above": 256,
            "type": "keyword"
          },
          "stand": {
            "analyzer": "standard",
            "type": "text"
          },
          "STPA": {
            "type": "text",
            "analyzer": "stand_t2s_pinyin_analyzer"
          },
          "ITPA": {
            "type": "text",
            "analyzer": "ik_t2s_pinyin_analyzer"
          }
        }
      },
      "create_time": {
        "type": "date",
        "format": "epoch_millis||strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSSZ"
      },
      "update_time": {
        "type": "date",
        "format": "epoch_millis||strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSSZ"
      }
    }
  }
}

```

## es测试搜索接口
```shell
GET /user_v1/_search
{
  "from": 0,
  "size": 10,
  "terminate_after": 100000,
  "query": {
    "bool": {
      "must": [
        {
          "query_string": {
            "query": "e嘿",
            "fields": [
              "name.ITPA",
              "name.STPA",
              "name.stand",
              "name.keyword"
            ],
            "type": "phrase",
            "default_operator": "and"
          }
        }
      ],
      "adjust_pure_negative": true,
      "boost": 1
    }
  },
  "highlight": {
    "fragment_size": 1000,
    "pre_tags": [
      "<span style=\"color:red;background:yellow;\">"
    ],
    "post_tags": [
      "</span>"
    ],
    "fields": {
      "name.stand": {},
      "desc.stand": {},
      "abstr.stand": {},
      "name.IPA": {},
      "desc.IPA": {},
      "abstr.IPA": {},
      "name.ITPA": {},
      "desc.ITPA": {},
      "abstr.ITPA": {}
    }
  }
}

```