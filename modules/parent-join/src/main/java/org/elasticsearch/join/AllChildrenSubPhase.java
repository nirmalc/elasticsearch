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
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AllChildrenSubPhase implements FetchSubPhase {


    @Override
    public void hitsExecute(SearchContext context, SearchHit[] hits) {
        if (context.getSearchExt(FamilySearchExt.NAME) == null) {
            return;
        }

        HashSet<String> parentIdx = new HashSet<String>(hits.length);
        BitSet blackList = new BitSet(hits.length);
        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            if ( parentIdx.contains( hit.getType())) {
                blackList.set(i);
            }
            if (hit.getType().equalsIgnoreCase("question")) {
                parentIdx.add(hits[i].getId());
            } else {
                if ( hit.getInnerHits() == null) continue;
                SearchHits children = hit.getInnerHits().get("all_children");
                for (int j = 0; j < children.totalHits; j++) {
                    if (children.getAt(j).getType().equalsIgnoreCase("question")) {
                        parentIdx.add(hits[i].getId());
                        break;
                    }
                }
            }
        }
        ArrayList<SearchHit> trimmedHits = new ArrayList<SearchHit>(hits.length - blackList.cardinality());
        for (int i = 0; i < hits.length; i ++){
            if (blackList.get(i)){
                trimmedHits.add(hits[i]);
            }
        }

        for ( int i =0; i < hits.length; i ++){
            if ( i < trimmedHits.size()) {
                hits[i] = trimmedHits.get(i);
            }else{
                hits[i] = null;
            }

        }
    }
}
