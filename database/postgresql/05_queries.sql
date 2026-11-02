-- Read-only queries; safe to execute in Navicat.
-- 1. Products and price (DEMO, not a commercial quote)
SELECT ma_san_pham,ten_san_pham,loai_thuoc,gia_niem_yet_vnd,dang_ban
FROM san_pham ORDER BY san_pham_id;

-- 2. Expected availability per batch: physical stock MINUS valid held quantity
SELECT ma_san_pham,so_lo_nha_sx,han_su_dung,ton_vat_ly,
       luong_dang_giu,kha_dung_du_kien
FROM v_ton_kha_dung ORDER BY ma_san_pham,han_su_dung;

-- 3. Batch expiry and quarantine warnings
SELECT * FROM v_canh_bao_lo ORDER BY han_su_dung;

-- 4. Orders with historical line totals
SELECT * FROM v_tong_gia_tri_don ORDER BY don_hang_id;

-- 5. Detailed request/sale lines
SELECT d.ma_don,d.kenh_ban,d.trang_thai,p.ten_san_pham,
       l.so_lo_nha_sx,c.so_luong,c.so_luong_giu,c.so_luong_xuat,
       c.don_gia_chot_vnd
FROM chi_tiet_don_hang c
JOIN don_hang d ON d.don_hang_id=c.don_hang_id
JOIN san_pham p ON p.san_pham_id=c.san_pham_id
LEFT JOIN lo_thuoc l ON l.lo_thuoc_id=c.lo_thuoc_id
ORDER BY d.don_hang_id,c.chi_tiet_id;

-- 6. Stock-inspection checks awaiting review: never changes lot automatically
SELECT k.kiem_tra_id,l.so_lo_nha_sx,k.loai_kiem,k.ket_qua,
       k.so_luong_thuc_dem,k.ton_truoc_dieu_chinh,
       k.nguoi_duyet_dieu_chinh_id
FROM kiem_tra_lo k JOIN lo_thuoc l ON l.lo_thuoc_id=k.lo_thuoc_id
ORDER BY k.kiem_tra_id;

-- 7. Shift demo report; values do not represent real received money
SELECT * FROM v_doi_soat_ca_demo ORDER BY ma_ca;

-- 8. Quick table counts
SELECT 'tai_khoan' bang, COUNT(*) so_ban_ghi FROM tai_khoan
UNION ALL SELECT 'san_pham',COUNT(*) FROM san_pham
UNION ALL SELECT 'lo_thuoc',COUNT(*) FROM lo_thuoc
UNION ALL SELECT 'don_hang',COUNT(*) FROM don_hang
UNION ALL SELECT 'chi_tiet_don_hang',COUNT(*) FROM chi_tiet_don_hang
UNION ALL SELECT 'kiem_tra_lo',COUNT(*) FROM kiem_tra_lo;
