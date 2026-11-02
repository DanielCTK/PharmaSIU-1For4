package com.pharmasiu.mobile.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * A single, auditable initial catalog fixture. CSV is the shared source of the
 * Android seed and the separate Navicat example. No accounts/passwords/orders.
 *
 * Seed is append-only on a truly empty catalog. Never changes products already
 * inserted by the user. Uses one SQLite transaction for products and lots.
 */
final class CatalogSeed {
    private static final String FILE = "database/catalog_seed.csv";

    private CatalogSeed() {}

    static void insertWhenCatalogEmpty(Context context, SQLiteDatabase db) {
        db.beginTransaction();
        try {
            try (Cursor count = db.rawQuery("SELECT COUNT(*) FROM san_pham", null)) {
                if (!count.moveToFirst()) throw new IllegalStateException("Catalog table missing");
                if (count.getLong(0) != 0L) {
                    db.setTransactionSuccessful();
                    return;
                }
            }
            int inserted = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    context.getAssets().open(FILE), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.startsWith("code|")) continue;
                    String[] cells = line.split("\\|", -1);
                    if (cells.length != 11)
                        throw new IllegalStateException("Invalid sample catalog CSV row");
                    ContentValues product = new ContentValues();
                    product.put("ma_san_pham", cells[0]);
                    product.put("ten_san_pham", cells[1]);
                    product.put("hoat_chat", cells[2]);
                    product.put("ham_luong", cells[3]);
                    product.put("don_vi_ban", cells[4]);
                    product.put("loai_thuoc", cells[5]);
                    product.put("gia_niem_yet_vnd", Long.parseLong(cells[6]));
                    product.put("dang_ban", 1);
                    long productId = db.insertOrThrow("san_pham", null, product);
                    if (productId < 1) throw new IllegalStateException("Cannot create product");
                    ContentValues lot = new ContentValues();
                    lot.put("san_pham_id", productId);
                    lot.put("so_lo_nha_sx", cells[7]);
                    lot.put("ngay_nhap", Instant.now().toString());
                    lot.put("han_su_dung", cells[8]);
                    lot.put("so_luong_nhap", Integer.parseInt(cells[9]));
                    lot.put("so_luong_ton", Integer.parseInt(cells[9]));
                    lot.put("tinh_trang", cells[10]);
                    lot.put("ghi_chu", "Dữ liệu mẫu phục vụ kiểm thử giao diện và SQLite");
                    db.insertOrThrow("lo_thuoc", null, lot);
                    inserted++;
                }
            } catch (java.io.IOException problem) {
                throw new IllegalStateException("Cannot load initial catalog CSV", problem);
            }
            if (inserted != 15)
                throw new IllegalStateException("Expected exactly 15 example products, got " + inserted);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
