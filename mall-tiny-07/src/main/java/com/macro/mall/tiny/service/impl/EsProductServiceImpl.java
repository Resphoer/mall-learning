package com.macro.mall.tiny.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.macro.mall.tiny.dao.EsProductDao;
import com.macro.mall.tiny.nosql.elasticsearch.document.EsProduct;
import com.macro.mall.tiny.service.EsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商品搜索管理Service实现类
 */
@Service
public class EsProductServiceImpl implements EsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);
    @Autowired
    private EsProductDao productDao;
    @Autowired
    private ElasticsearchClient client;
    @Override
    public int importAll() {
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        try {
            for(EsProduct esProduct:esProductList){
                client.index(i -> i.index("products")
                        .id(String.valueOf(esProduct.getId()))
                        .document(esProduct));
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return esProductList.size();
    }

    @Override
    public void delete(Long id) {
        try{
            client.delete(i -> i.index("products")
                    .id(String.valueOf(id)));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public EsProduct create(Long id) {
        EsProduct result = null;
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if(esProductList.size()>0){
            EsProduct esProduct = esProductList.get(0);
            try{
                client.index(i -> i.index("products")
                        .id(String.valueOf(esProduct.getId()))
                        .document(esProduct));
            } catch(Exception e){
                e.printStackTrace();
            }
            result = esProduct;
        }
        return result;
    }

    @Override
    public void delete(List<Long> ids) {
        if(!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for(Long id : ids){
                try {
                    client.delete(i -> i.index("products")
                            .id(String.valueOf(id)));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum,pageSize);
        try{
            SearchResponse<EsProduct> esProductSearchResponse = client.search(i -> i
                    .index("products")
                    .query(q -> q
                            .match(t -> t.field("name").field("subtitle").field("keywords").query(keyword)
                                    .operator(Operator.Or))), EsProduct.class
            );
            TotalHits total = esProductSearchResponse.hits().total();
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;
            if(isExactResult) {
                List<EsProduct> products = new ArrayList<>();
                List<Hit<EsProduct>> hits = esProductSearchResponse.hits().hits();
                for(Hit<EsProduct> hit:hits){
                    products.add(hit.source());
                }
                Page<EsProduct> productPage = new PageImpl<>(products, pageable, 500);
                return productPage;
            } else{
                return null;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
