package com.macro.mall.tiny.service.impl;

import com.macro.mall.tiny.nosql.mongodb.document.MemberReadHistory;
import com.macro.mall.tiny.service.MemberReadHistoryService;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 会员浏览历史记录管理Service实现类
 */
@Service
public class MemberReadHistoryServiceImpl implements MemberReadHistoryService {
    @Autowired
    private MongoClient client;

    @Autowired
    private CodecRegistry registry;

    @Override
    public int create(MemberReadHistory memberReadHistory) {
        memberReadHistory.setId(null);
        memberReadHistory.setCreateTime(new Date());
        MongoDatabase database = client.getDatabase("mall").withCodecRegistry(registry);
        MongoCollection<MemberReadHistory> collection = database.getCollection("mall-tiny", MemberReadHistory.class);
        collection.insertOne(memberReadHistory);
        return 1;
    }

    @Override
    public int delete(List<String> ids) {
        MongoDatabase database = client.getDatabase("mall").withCodecRegistry(registry);
        MongoCollection<MemberReadHistory> collection = database.getCollection("mall-tiny", MemberReadHistory.class);
        for (String id : ids) {
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        }
        return ids.size();
    }

    @Override
    public List<MemberReadHistory> list(Long memberId) {
        MongoDatabase database = client.getDatabase("mall").withCodecRegistry(registry);
        MongoCollection<MemberReadHistory> collection = database.getCollection("mall-tiny", MemberReadHistory.class);
        List<MemberReadHistory> memberReadHistories = new ArrayList<>();
        collection.find(Filters.eq("memberId", memberId)).sort(Sorts.descending("createTime")).into(memberReadHistories);
        return memberReadHistories;
    }
}
