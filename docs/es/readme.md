# es相关内容


## user表缓存
```shell

PUT /user_v1
{
   "settings": {
    "index": {
      "max_ngram_diff": 10
    },
    "analysis": {
      "tokenizer": {
        "pinyin_tokenizer": {
          "type": "pinyin",
          "first_letter": "prefix",
          "padding_char": " "
        }
      },
      "filter": {
        "pinyin_ngram_filter": {
          "type": "ngram",
          "min_gram": 2,
          "max_gram": 10
        },
        "pinyin_edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 1
        },
        "lowercase": {
          "type": "lowercase"
        }
      },
      "analyzer": {
        "pinyin_full_analyzer": {
          "tokenizer": "pinyin_tokenizer",
          "filter": ["lowercase", "pinyin_ngram_filter"]
        },
        "pinyin_short_analyzer": {
          "tokenizer": "pinyin_tokenizer",
          "filter": ["lowercase", "pinyin_edge_ngram_filter"]
        },
        "ik_smart_analyzer": {
          "tokenizer": "ik_smart",
          "filter": ["lowercase"]
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
        "fields": {
         "full_pinyin": {
            "type": "text",
            "analyzer": "pinyin_full_analyzer"
          },
          "short_pinyin": {
            "type": "text",
            "analyzer": "pinyin_short_analyzer"
          },
          "ik_smart": {
            "type": "text",
            "analyzer": "ik_smart_analyzer"
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
        "fields": {
          "full_pinyin": {
            "type": "text",
            "analyzer": "pinyin_full_analyzer"
          },
          "short_pinyin": {
            "type": "text",
            "analyzer": "pinyin_short_analyzer"
          },
          "ik_smart": {
            "type": "text",
            "analyzer": "ik_smart_analyzer"
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

DELETE /user_v1

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

# 查询
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