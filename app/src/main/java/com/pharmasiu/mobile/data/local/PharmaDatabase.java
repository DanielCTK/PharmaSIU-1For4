package com.pharmasiu.mobile.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Single-device SQLite data source, initialized from the EMPTY, versioned 6-table
 * schema. A 15-item example catalog is inserted on first run only if empty.
 * Never copies the 93-row Navicat demonstration database into user storage.
 * Each connection enables FK checks. Do not share this database by direct network access.
 */
public final class PharmaDatabase extends SQLiteOpenHelper implements AutoCloseable {
    public static final String NAME = "pharmasiu.db";
    private static final String ASSET = "database/pharmasiu_schema.sqlite3";
    private final SQLiteDatabase db;

    public PharmaDatabase(Context context) {
        super(context.getApplicationContext(), NAME, null, 1);
        Context app = context.getApplicationContext();
        File destination = app.getDatabasePath(NAME);
        File parent = destination.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs()))
            throw new IllegalStateException("Cannot create database directory");
        if (!destination.exists()) {
            File staging = new File(parent, NAME + ".initializing");
            if (staging.exists() && !staging.delete())
                throw new IllegalStateException("Unfinished database bootstrap");
            try (InputStream input = app.getAssets().open(ASSET);
                 FileOutputStream output = new FileOutputStream(staging)) {
                byte[] block = new byte[16384];
                int read;
                while ((read = input.read(block)) != -1) output.write(block, 0, read);
                output.getFD().sync();
            } catch (Exception e) {
                staging.delete();
                throw new IllegalStateException("Cannot install empty database schema", e);
            }
            if (!staging.renameTo(destination)) {
                staging.delete();
                throw new IllegalStateException("Cannot atomically install database");
            }
        }
        // SQLiteOpenHelper owns opening/closing; the verified bundled file is
        // installed first so no partially created six-table database can appear.
        SQLiteDatabase opened = getWritableDatabase();
        try {
            if (opened.getVersion() != 1)
                throw new SQLiteException("Unsupported database schema version: " + opened.getVersion());
            try (android.database.Cursor c = opened.rawQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name IN " +
                    "('tai_khoan','san_pham','lo_thuoc','don_hang','chi_tiet_don_hang','kiem_tra_lo')", null)) {
                if (!c.moveToFirst() || c.getInt(0) != 6)
                    throw new SQLiteException("PharmaSIU schema must have all six required tables");
            }
            // Populate only an EMPTY product catalog with 15 documented fixtures.
            // Existing products, accounts and orders are never replaced.
            CatalogSeed.insertWhenCatalogEmpty(app, opened);
        } catch (RuntimeException e) {
            super.close();
            throw e;
        }
        db = opened;
    }
    @Override public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase database) {
        // The full schema, four views, and all business triggers are bundled
        // atomically as a prebuilt verified asset; never create partial tables.
        throw new SQLiteException("Missing bundled PharmaSIU schema asset");
    }

    @Override public void onUpgrade(SQLiteDatabase database, int from, int to) {
        // Explicit migration required to preserve orders/lots; never DROP/CREATE.
        throw new SQLiteException("No safe migration defined from " + from + " to " + to);
    }

    @Override public void onDowngrade(SQLiteDatabase database, int from, int to) {
        throw new SQLiteException("Database downgrade would risk user records");
    }

    public synchronized SQLiteDatabase database() {
        if (!db.isOpen()) throw new IllegalStateException("Database is closed");
        return db;
    }
    @Override public synchronized void close() {
        super.close();
    }
}
