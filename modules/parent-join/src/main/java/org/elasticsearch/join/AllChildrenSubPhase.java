package org.elasticsearch.join;

import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.FetchPhase;
import org.elasticsearch.search.fetch.FetchSearchResult;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.fetch.subphase.InnerHitsContext;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllChildrenSubPhase implements FetchSubPhase {

    @Override
    public void hitsExecute(SearchContext context, SearchHit[] hits) {
        if (context.getSearchExt(FamilySearchExt.NAME) == null) {
            return;
        }

        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            SearchHits searchHits = new SearchHits(SearchHits.EMPTY,0,0);
            HashMap<String,SearchHits> hitsHashMap = new HashMap<>();
            hitsHashMap.put("all_children",searchHits);
            hit.setInnerHits(hitsHashMap);
        }

        for (Map.Entry<String, InnerHitsContext.InnerHitSubContext> entry : context.innerHits().getInnerHits().entrySet()) {
            InnerHitsContext.InnerHitSubContext innerHits = entry.getValue();
            TopDocs[] topDocs;
            try {
                topDocs = innerHits .topDocs(hits);
            } catch (IOException e) {
                throw ExceptionsHelper.convertToElastic(e);
            }
            for (int i = 0; i < hits.length; i++) {
                SearchHit hit = hits[i];
                TopDocs topDoc = topDocs[i];

                Map<String, SearchHits> results = hit.getInnerHits();
                if (results == null) {
                    hit.setInnerHits(results = new HashMap<>());
                }
                innerHits.queryResult().topDocs(topDoc, innerHits.sort() == null ? null : innerHits.sort().formats);
                int[] docIdsToLoad = new int[topDoc.scoreDocs.length];
                for (int j = 0; j < topDoc.scoreDocs.length; j++) {
                    docIdsToLoad[j] = topDoc.scoreDocs[j].doc;
                }
                innerHits.docIdsToLoad(docIdsToLoad, 0, docIdsToLoad.length);
                innerHits.setUid(new Uid(hit.getType(), hit.getId()));
                FetchPhase fetchPhase = new FetchPhase(new ArrayList<FetchSubPhase>());
                fetchPhase.execute(innerHits);
                FetchSearchResult fetchResult = innerHits.fetchResult();
                SearchHit[] internalHits = fetchResult.fetchResult().hits().internalHits();
                for (int j = 0; j < internalHits.length; j++) {
                    ScoreDoc scoreDoc = topDoc.scoreDocs[j];
                    SearchHit searchHitFields = internalHits[j];
                    searchHitFields.score(scoreDoc.score);
                    if (scoreDoc instanceof FieldDoc) {
                        FieldDoc fieldDoc = (FieldDoc) scoreDoc;
                        searchHitFields.sortValues(fieldDoc.fields, innerHits.sort().formats);
                    }
                }
                results.put(entry.getKey(), fetchResult.hits());
            }
        }

    }
}
