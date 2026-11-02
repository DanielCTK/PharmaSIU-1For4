package com.pharmasiu.mobile.domain;

import java.net.URI;

/** HTTPS only; URL screening is not a substitute for a trusted production catalog. */
public final class CatalogMediaRules {
    private CatalogMediaRules(){}
    public static String supportedImageUrl(String value){
        if(value==null)return null;
        String s=value.trim();
        if(s.isEmpty()||s.length()>2048)return null;
        try{
            URI uri=new URI(s);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost()!=null &&
                !uri.getHost().isEmpty() && uri.getUserInfo()==null && uri.getFragment()==null ? s:null;
        }catch(Exception e){return null;}
    }
}
