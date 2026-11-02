package com.pharmasiu.mobile.core.state;

import com.pharmasiu.mobile.core.DemoState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Scoped RAM account operations; enforces ownership and prevents customer data leakage.
 * A DEMO password reset is not production-grade account recovery.
 */
public final class CustomerAccountStore {
    private final Map<String,List<String>> favoritesByPhone = new LinkedHashMap<>();
    private int nextAddressId;

    public void restoreFavorites(String phone,List<String> active) {
        active.clear();
        active.addAll(favoritesByPhone.getOrDefault(phone,Collections.emptyList()));
    }
    public void rememberFavorites(String phone,List<String> active) {
        favoritesByPhone.put(phone,new ArrayList<>(active));
    }
    public boolean register(DemoState state,String name,String phone,String email,String password){
        if(name==null||name.trim().length()<2||phone==null||!phone.matches("0[0-9]{9,10}")
            ||password==null||password.length()<6||state.customerAccounts.containsKey(phone))return false;
        String value=email==null?"":email.trim().toLowerCase(Locale.ROOT);
        if(!value.isEmpty()){
            if(!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))return false;
            for(DemoState.CustomerProfile profile:state.customerAccounts.values())
                if(value.equals(profile.email.toLowerCase(Locale.ROOT)))return false;
        }
        state.customerAccounts.put(phone,new DemoState.CustomerProfile(
            name.trim(),phone,value,"",password));
        return true;
    }
    public boolean update(DemoState state,String name,String email,String birthday){
        DemoState.CustomerProfile profile=state.currentCustomer();
        if(profile==null||name==null||name.trim().length()<2)return false;
        String value=email==null?"":email.trim().toLowerCase(Locale.ROOT);
        if(!value.isEmpty()&&!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))return false;
        for(DemoState.CustomerProfile other:state.customerAccounts.values())
            if(other!=profile&&!value.isEmpty()&&value.equals(other.email.toLowerCase(Locale.ROOT)))
                return false;
        profile.displayName=name.trim();profile.email=value;
        profile.birthday=birthday==null?"":birthday.trim();
        return true;
    }
    public boolean changePassword(DemoState state,String oldPassword,String newPassword){
        DemoState.CustomerProfile profile=state.currentCustomer();
        if(profile==null||oldPassword==null||!oldPassword.equals(profile.password)
            ||newPassword==null||newPassword.length()<6)return false;
        profile.password=newPassword;return true;
    }
    public List<DemoState.Address> addresses(DemoState state){
        if(state.currentCustomer()==null)return Collections.emptyList();
        return Collections.unmodifiableList(
            state.addressBooks.getOrDefault(state.customerPhone,Collections.emptyList()));
    }
    public DemoState.Address addressById(DemoState state,String id){
        for(DemoState.Address address:addresses(state))
            if(address.id.equals(id))return address;
        return null;
    }
    public boolean saveAddress(DemoState state,String id,String label,String receiver,String phone,
                               String detail,boolean makeDefault){
        if(state.currentCustomer()==null||label==null||label.trim().isEmpty()
            ||receiver==null||receiver.trim().length()<2||phone==null||!phone.matches("0[0-9]{9,10}")
            ||detail==null||detail.trim().length()<3)return false;
        List<DemoState.Address> list=state.addressBooks.computeIfAbsent(
            state.customerPhone,key->new ArrayList<>());
        if(makeDefault)for(DemoState.Address a:list)a.isDefault=false;
        if(id!=null&&!id.isEmpty()){
            DemoState.Address existing=addressById(state,id);
            if(existing==null)return false;
            existing.label=label.trim();existing.receiver=receiver.trim();
            existing.phone=phone;existing.detail=detail.trim();existing.isDefault=makeDefault;
        }else{
            DemoState.Address entry=new DemoState.Address(
                String.format(Locale.ROOT,"ADDR-%03d",++nextAddressId),
                label.trim(),receiver.trim(),phone,detail.trim(),makeDefault||list.isEmpty());
            list.add(entry);
        }
        if(!list.isEmpty()&&list.stream().noneMatch(a->a.isDefault))
            list.get(0).isDefault=true;
        return true;
    }
    public boolean deleteAddress(DemoState state,String id){
        if(state.currentCustomer()==null)return false;
        List<DemoState.Address> list=state.addressBooks.get(state.customerPhone);
        if(list==null)return false;
        boolean removed=list.removeIf(a->a.id.equals(id));
        if(removed&&!list.isEmpty()&&list.stream().noneMatch(a->a.isDefault))
            list.get(0).isDefault=true;
        return removed;
    }
    public void toggleFavorite(DemoState state,String id){
        if(state.currentCustomer()==null||state.product(id)==null)return;
        if(state.favorites.contains(id))state.favorites.remove(id);
        else state.favorites.add(id);
        rememberFavorites(state.customerPhone,state.favorites);
    }
    public boolean changePhone(DemoState state,String nextPhone){
        DemoState.CustomerProfile profile=state.currentCustomer();
        if(profile==null||nextPhone==null||!nextPhone.matches("0[0-9]{9,10}"))return false;
        String previous=state.customerPhone;
        if(previous.equals(nextPhone))return true;
        if(state.customerAccounts.containsKey(nextPhone))return false;
        state.customerAccounts.remove(previous);
        profile.phone=nextPhone;
        state.customerAccounts.put(nextPhone,profile);
        List<DemoState.Address> owned=state.addressBooks.remove(previous);
        if(owned!=null)state.addressBooks.put(nextPhone,owned);
        List<String> favorites=favoritesByPhone.remove(previous);
        if(favorites!=null)favoritesByPhone.put(nextPhone,favorites);
        for(DemoState.Order order:state.orders)
            if(previous.equals(order.customerPhone))order.customerPhone=nextPhone;
        state.customerPhone=nextPhone;
        return true;
    }
    public boolean resetPassword(DemoState state,String phone,String nextPassword){
        if(!DemoState.NONE.equals(state.role)||phone==null||nextPassword==null
            ||nextPassword.length()<6)return false;
        DemoState.CustomerProfile profile=state.customerAccounts.get(phone.trim());
        if(profile==null)return false;
        profile.password=nextPassword;return true;
    }
}
