-- Read-only views. Availability is indicative until transactional stock hold is implemented.
BEGIN;
CREATE OR REPLACE VIEW v_ton_kha_dung AS
WITH dang_giu AS (
 SELECT c.lo_thuoc_id, SUM(c.so_luong_giu) AS luong_dang_giu
 FROM chi_tiet_don_hang c JOIN don_hang d ON d.don_hang_id=c.don_hang_id
 WHERE c.lo_thuoc_id IS NOT NULL
   AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
   AND d.het_han_giu_luc > CURRENT_TIMESTAMP
 GROUP BY c.lo_thuoc_id
)
SELECT p.san_pham_id,p.ma_san_pham,p.ten_san_pham,p.loai_thuoc,
       l.lo_thuoc_id,l.so_lo_nha_sx,l.han_su_dung,l.tinh_trang,
       l.so_luong_ton AS ton_vat_ly,
       COALESCE(g.luong_dang_giu,0) AS luong_dang_giu,
       CASE WHEN l.tinh_trang='DAT' AND l.han_su_dung>CURRENT_DATE
           THEN GREATEST(0,l.so_luong_ton-COALESCE(g.luong_dang_giu,0))
           ELSE 0 END AS kha_dung_du_kien
FROM lo_thuoc l
JOIN san_pham p ON p.san_pham_id=l.san_pham_id
LEFT JOIN dang_giu g ON g.lo_thuoc_id=l.lo_thuoc_id;

CREATE OR REPLACE VIEW v_tong_gia_tri_don AS
SELECT d.don_hang_id,d.ma_don,d.kenh_ban,d.trang_thai,d.trang_thai_tt,
       COUNT(c.chi_tiet_id) AS so_dong,
       COALESCE(SUM(c.so_luong*c.don_gia_chot_vnd),0) AS tong_gia_tri_vnd,
       COALESCE(SUM(c.so_luong_giu),0) AS tong_dang_giu,
       COALESCE(SUM(c.so_luong_xuat),0) AS tong_da_xuat
FROM don_hang d LEFT JOIN chi_tiet_don_hang c ON c.don_hang_id=d.don_hang_id
GROUP BY d.don_hang_id,d.ma_don,d.kenh_ban,d.trang_thai,d.trang_thai_tt;

CREATE OR REPLACE VIEW v_canh_bao_lo AS
SELECT l.lo_thuoc_id,p.ma_san_pham,p.ten_san_pham,l.so_lo_nha_sx,l.han_su_dung,
       l.tinh_trang,l.so_luong_ton,
       CASE WHEN l.han_su_dung<=CURRENT_DATE THEN 'HET_HAN'
            WHEN l.tinh_trang!='DAT' THEN l.tinh_trang
            WHEN l.han_su_dung<=CURRENT_DATE+30 THEN 'SAP_HET_HAN'
            ELSE 'BINH_THUONG' END AS canh_bao
FROM lo_thuoc l JOIN san_pham p ON p.san_pham_id=l.san_pham_id
WHERE l.tinh_trang!='DAT' OR l.han_su_dung<=CURRENT_DATE+30;

CREATE OR REPLACE VIEW v_doi_soat_ca_demo AS
SELECT d.ma_ca,COUNT(*) AS so_don_demo,
       SUM(d.so_tien_da_thu_vnd) AS tong_tien_da_ghi_nhan_demo_vnd
FROM don_hang d
WHERE d.kenh_ban='POS' AND d.trang_thai='HOAN_TAT'
  AND d.trang_thai_tt='DEMO_DA_GHI_NHAN' AND d.ma_ca IS NOT NULL
GROUP BY d.ma_ca;
COMMIT;
