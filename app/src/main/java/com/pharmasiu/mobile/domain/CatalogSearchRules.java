package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.data.model.Medicine;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Accent-insensitive deterministic offline catalog search, faithful to CatalogSearchRules.kt. */
public final class CatalogSearchRules {
    private CatalogSearchRules(){}
    public static String normalized(String text){
        return Normalizer.normalize(text.toLowerCase(new Locale("vi","VN")),Normalizer.Form.NFD)
            .replaceAll("\\p{M}+","").replace('đ','d').trim();
    }
    public static List<Medicine> filter(List<Medicine> products,String query,String category,String sort){
        String[] tokens=normalized(query).split("\\s+");
        List<Medicine> result=new ArrayList<>();
        for(Medicine m:products){
            boolean categoryMatches;
            if("Tất cả".equals(category))categoryMatches=true;
            else if("Kê đơn".equals(category))categoryMatches=m.requiresPrescription;
            else if("Thực phẩm".equals(category))categoryMatches=
                "Thực phẩm".equals(m.category)||"Thực phẩm bổ sung".equals(m.category);
            else categoryMatches=m.category.equals(category);
            if(!categoryMatches)continue;
            String content=normalized(m.name+" "+m.subtitle+" "+m.category);
            boolean matching=true;
            for(String token:tokens)if(!token.isEmpty()&&!content.contains(token)){matching=false;break;}
            if(matching)result.add(m);
        }
        Comparator<Medicine> sorter=null;
        if("Tên A–Z".equals(sort))sorter=Comparator.comparing((Medicine m)->m.name,
            String.CASE_INSENSITIVE_ORDER).thenComparing(m->m.id);
        else if("Giá thấp".equals(sort))sorter=Comparator.comparing((Medicine m)->m.priceVnd<=0)
            .thenComparingLong(m->m.priceVnd).thenComparing(m->m.id);
        else if("Giá cao".equals(sort))sorter=Comparator.comparing((Medicine m)->m.priceVnd<=0)
            .thenComparing(Comparator.comparingLong((Medicine m)->m.priceVnd).reversed())
            .thenComparing(m->m.id);
        if(sorter!=null)result.sort(sorter);
        return result;
    }
}
