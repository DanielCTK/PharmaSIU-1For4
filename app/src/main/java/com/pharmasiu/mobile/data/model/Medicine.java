package com.pharmasiu.mobile.data.model;

import java.util.Objects;

/** Offline catalog specimen, never evidence of verified stock, price or Rx approval. */
public final class Medicine {
    public final String id, name, subtitle, category, unit, description, imageUrl;
    public final long priceVnd;
    public final boolean requiresPrescription;

    public Medicine(String id, String name, String subtitle, long priceVnd, String category,
                    boolean requiresPrescription, String unit, String description) {
        this(id, name, subtitle, priceVnd, category, requiresPrescription, unit, description, null);
    }
    public Medicine(String id, String name, String subtitle, long priceVnd, String category,
                    boolean requiresPrescription, String unit, String description, String imageUrl) {
        this.id=id;this.name=name;this.subtitle=subtitle;this.priceVnd=priceVnd;
        this.category=category;this.requiresPrescription=requiresPrescription;this.unit=unit;
        this.description=description;this.imageUrl=imageUrl;
    }
    public Medicine copy(String id, String name, String subtitle, long priceVnd, String category,
                         boolean requiresPrescription, String unit, String description, String imageUrl) {
        return new Medicine(id,name,subtitle,priceVnd,category,requiresPrescription,unit,description,imageUrl);
    }
    @Override public boolean equals(Object o) {
        if(this==o)return true;
        if(!(o instanceof Medicine))return false;
        Medicine m=(Medicine)o;
        return priceVnd==m.priceVnd && requiresPrescription==m.requiresPrescription &&
            Objects.equals(id,m.id)&&Objects.equals(name,m.name)&&Objects.equals(subtitle,m.subtitle)&&
            Objects.equals(category,m.category)&&Objects.equals(unit,m.unit)&&
            Objects.equals(description,m.description)&&Objects.equals(imageUrl,m.imageUrl);
    }
    @Override public int hashCode(){return Objects.hash(id,name,subtitle,priceVnd,category,requiresPrescription,unit,description,imageUrl);}
    @Override public String toString(){return "Medicine(id="+id+", name="+name+", priceVnd="+priceVnd+")";}
}
