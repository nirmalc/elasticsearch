package org.elasticsearch.join;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.internal.SearchContext;

import java.util.Collections;
import java.util.HashMap;

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
    }
}
