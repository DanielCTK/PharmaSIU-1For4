-- PharmaSIU: SQLite edition; 6 tables exactly as in the approved CSDL diagram.
-- Execute once in Navicat SQLite / DB Browser for SQLite (or use the supplied .sqlite3 directly).
-- All timestamps are ISO 8601 UTC TEXT because SQLite has no TIMESTAMPTZ storage class.
PRAGMA foreign_keys = ON;
BEGIN IMMEDIATE;

CREATE TABLE IF NOT EXISTS tai_khoan (
    tai_khoan_id INTEGER PRIMARY KEY,
    email VARCHAR(190) UNIQUE,
    mat_khau_hash TEXT,
    ho_ten VARCHAR(160) NOT NULL,
    so_dien_thoai VARCHAR(20) UNIQUE,
    vai_tro VARCHAR(20) NOT NULL CHECK (vai_tro IN ('CUSTOMER','CASHIER','ADMIN')),
    co_quyen_duyet_toa BOOLEAN NOT NULL DEFAULT 0 CHECK (co_quyen_duyet_toa IN (0,1)),
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'DEMO' CHECK (trang_thai IN ('DEMO','HOAT_DONG','TAM_KHOA')),
    tao_luc TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),
    CHECK (co_quyen_duyet_toa = 0 OR vai_tro = 'CASHIER')
);

CREATE TABLE IF NOT EXISTS san_pham (
    san_pham_id INTEGER PRIMARY KEY,
    ma_san_pham VARCHAR(40) NOT NULL UNIQUE,
    ten_san_pham VARCHAR(200) NOT NULL,
    hoat_chat VARCHAR(200),
    ham_luong VARCHAR(100),
    don_vi_ban VARCHAR(40) NOT NULL,
    loai_thuoc VARCHAR(10) NOT NULL CHECK (loai_thuoc IN ('OTC','RX','KHAC')),
    gia_niem_yet_vnd BIGINT NOT NULL DEFAULT 0 CHECK (gia_niem_yet_vnd >= 0),
    dang_ban BOOLEAN NOT NULL DEFAULT 1 CHECK (dang_ban IN (0,1))
);

CREATE TABLE IF NOT EXISTS lo_thuoc (
    lo_thuoc_id INTEGER PRIMARY KEY,
    san_pham_id BIGINT NOT NULL REFERENCES san_pham(san_pham_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    so_lo_nha_sx VARCHAR(80) NOT NULL,
    ma_chung_tu_nhap VARCHAR(80),
    ten_nha_cung_cap VARCHAR(200),
    ngay_nhap TEXT NOT NULL,
    han_su_dung DATE NOT NULL,
    vi_tri_ke VARCHAR(80),
    so_luong_nhap BIGINT NOT NULL CHECK (so_luong_nhap >= 0),
    so_luong_ton BIGINT NOT NULL CHECK (so_luong_ton >= 0 AND so_luong_ton <= so_luong_nhap),
    tinh_trang VARCHAR(15) NOT NULL CHECK
       (tinh_trang IN ('CHO_KIEM_TRA','DAT','CACH_LY','KHONG_DAT','QUA_HAN')),
    nguoi_nhap_id BIGINT REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    ghi_chu TEXT,
    UNIQUE(san_pham_id, so_lo_nha_sx),
    UNIQUE(lo_thuoc_id, san_pham_id)
);

CREATE TABLE IF NOT EXISTS don_hang (
    don_hang_id INTEGER PRIMARY KEY,
    ma_don VARCHAR(45) NOT NULL UNIQUE,
    kenh_ban VARCHAR(15) NOT NULL CHECK (kenh_ban IN ('WEB','APP','POS')),
    nguoi_tao_id BIGINT NOT NULL REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    khach_hang_id BIGINT REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    nhan_vien_xu_ly_id BIGINT REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    trang_thai VARCHAR(20) NOT NULL CHECK
      (trang_thai IN ('CHO_XU_LY','CHO_DUYET_TOA','TU_CHOI','DA_GIU_HANG','SAN_SANG_NHAN','HOAN_TAT','DA_HUY')),
    tao_luc TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now')),
    du_kien_nhan_luc TEXT,
    het_han_giu_luc TEXT,
    tep_toa_private TEXT,
    trang_thai_toa VARCHAR(20) NOT NULL DEFAULT 'KHONG_CAN' CHECK
      (trang_thai_toa IN ('KHONG_CAN','CHUA_GUI','CHO_DUYET','DA_DUYET','TU_CHOI')),
    nguoi_duyet_toa_id BIGINT REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    duyet_toa_luc TEXT,
    ly_do_tu_choi_toa TEXT,
    phuong_thuc_tt VARCHAR(20) CHECK
      (phuong_thuc_tt IN ('TIEN_MAT','CHUYEN_KHOAN','TIEN_MAT_DEMO','KHAC_DEMO')),
    trang_thai_tt VARCHAR(20) NOT NULL DEFAULT 'CHUA_THANH_TOAN' CHECK
      (trang_thai_tt IN ('CHUA_THANH_TOAN','DEMO_DA_GHI_NHAN','DA_THANH_TOAN','THAT_BAI')),
    so_tien_da_thu_vnd BIGINT NOT NULL DEFAULT 0 CHECK (so_tien_da_thu_vnd >= 0),
    thanh_toan_luc TEXT,
    giao_luc TEXT,
    ma_ca VARCHAR(40),
    nguoi_doi_soat_id BIGINT REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    doi_soat_luc TEXT,
    ghi_chu TEXT,
    CHECK ( (nguoi_duyet_toa_id IS NULL AND duyet_toa_luc IS NULL) OR
            (nguoi_duyet_toa_id IS NOT NULL AND duyet_toa_luc IS NOT NULL) )
);

CREATE TABLE IF NOT EXISTS chi_tiet_don_hang (
    chi_tiet_id INTEGER PRIMARY KEY,
    don_hang_id BIGINT NOT NULL REFERENCES don_hang(don_hang_id) ON DELETE CASCADE,
    san_pham_id BIGINT NOT NULL REFERENCES san_pham(san_pham_id) ON DELETE RESTRICT,
    lo_thuoc_id BIGINT,
    so_luong BIGINT NOT NULL CHECK (so_luong > 0),
    don_gia_chot_vnd BIGINT NOT NULL CHECK (don_gia_chot_vnd >= 0),
    so_luong_giu BIGINT NOT NULL DEFAULT 0 CHECK (so_luong_giu >= 0),
    so_luong_xuat BIGINT NOT NULL DEFAULT 0 CHECK (so_luong_xuat >= 0),
    FOREIGN KEY (lo_thuoc_id, san_pham_id)
       REFERENCES lo_thuoc(lo_thuoc_id, san_pham_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CHECK (so_luong_giu + so_luong_xuat <= so_luong),
    CHECK (so_luong_giu = 0 OR lo_thuoc_id IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS kiem_tra_lo (
    kiem_tra_id INTEGER PRIMARY KEY,
    lo_thuoc_id BIGINT NOT NULL REFERENCES lo_thuoc(lo_thuoc_id) ON DELETE RESTRICT,
    nguoi_kiem_id BIGINT NOT NULL REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    loai_kiem VARCHAR(15) NOT NULL CHECK (loai_kiem IN ('NHAP_LO','HAN_DUNG','CHAT_LUONG','KIEM_KE')),
    kiem_luc TEXT NOT NULL,
    ket_qua VARCHAR(20) NOT NULL CHECK
      (ket_qua IN ('DAT','CHO_XU_LY','CACH_LY','HET_HAN','KHONG_DAT','CHENH_LECH')),
    so_luong_thuc_dem BIGINT CHECK (so_luong_thuc_dem >= 0),
    ton_truoc_dieu_chinh BIGINT CHECK (ton_truoc_dieu_chinh >= 0),
    ton_sau_dieu_chinh BIGINT CHECK (ton_sau_dieu_chinh >= 0),
    nguoi_duyet_dieu_chinh_id BIGINT REFERENCES tai_khoan(tai_khoan_id) ON DELETE RESTRICT,
    duyet_dieu_chinh_luc TEXT,
    ghi_chu TEXT,
    CHECK ( (nguoi_duyet_dieu_chinh_id IS NULL AND duyet_dieu_chinh_luc IS NULL AND ton_sau_dieu_chinh IS NULL) OR
            (nguoi_duyet_dieu_chinh_id IS NOT NULL AND duyet_dieu_chinh_luc IS NOT NULL AND ton_sau_dieu_chinh IS NOT NULL) )
);

CREATE INDEX IF NOT EXISTS idx_lo_sp_hsd ON lo_thuoc(san_pham_id, tinh_trang, han_su_dung);
CREATE INDEX IF NOT EXISTS idx_don_nguoi ON don_hang(khach_hang_id, tao_luc);
CREATE INDEX IF NOT EXISTS idx_don_ca ON don_hang(ma_ca, trang_thai);
CREATE INDEX IF NOT EXISTS idx_ct_don ON chi_tiet_don_hang(don_hang_id);
CREATE INDEX IF NOT EXISTS idx_ct_lo ON chi_tiet_don_hang(lo_thuoc_id, san_pham_id);
CREATE INDEX IF NOT EXISTS idx_kiem_lo ON kiem_tra_lo(lo_thuoc_id, kiem_luc);

COMMIT;
