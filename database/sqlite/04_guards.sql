-- Additional checks for direct Navicat edits. NOT a substitute for production transaction logic.
-- If an order is cancelled/rejected/completed, clear so_luong_giu BEFORE its status changes,
-- within the SAME transaction. Held quantities do not subtract from lo_thuoc.so_luong_ton.

DROP TRIGGER IF EXISTS trg_hold_insert;
CREATE TRIGGER trg_hold_insert BEFORE INSERT ON chi_tiet_don_hang
WHEN NEW.so_luong_giu>0 BEGIN
 SELECT CASE WHEN NOT EXISTS(
   SELECT 1 FROM don_hang d
   JOIN lo_thuoc l ON l.lo_thuoc_id=NEW.lo_thuoc_id AND l.san_pham_id=NEW.san_pham_id
   JOIN san_pham p ON p.san_pham_id=NEW.san_pham_id
   WHERE d.don_hang_id=NEW.don_hang_id
     AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
     AND datetime(d.het_han_giu_luc)>datetime('now')
     AND l.tinh_trang='DAT' AND date(l.han_su_dung)>date('now')
     AND (p.loai_thuoc!='RX' OR (
           d.trang_thai_toa='DA_DUYET'
           AND EXISTS(SELECT 1 FROM tai_khoan a
                      WHERE a.tai_khoan_id=d.nguoi_duyet_toa_id
                        AND a.co_quyen_duyet_toa=1)))
     AND NEW.so_luong_giu+COALESCE((
       SELECT SUM(other.so_luong_giu) FROM chi_tiet_don_hang other
       JOIN don_hang active_order ON active_order.don_hang_id=other.don_hang_id
       WHERE other.lo_thuoc_id=NEW.lo_thuoc_id
         AND active_order.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
         AND datetime(active_order.het_han_giu_luc)>datetime('now')),0)
         <=l.so_luong_ton
 ) THEN RAISE(ABORT,'INVALID_HOLD: status, Rx approval, lot quality, expiry or remaining stock') END;
END;

DROP TRIGGER IF EXISTS trg_hold_update;
CREATE TRIGGER trg_hold_update BEFORE UPDATE OF so_luong_giu,lo_thuoc_id,san_pham_id,don_hang_id
ON chi_tiet_don_hang WHEN NEW.so_luong_giu>0 BEGIN
 SELECT CASE WHEN NOT EXISTS(
   SELECT 1 FROM don_hang d
   JOIN lo_thuoc l ON l.lo_thuoc_id=NEW.lo_thuoc_id AND l.san_pham_id=NEW.san_pham_id
   JOIN san_pham p ON p.san_pham_id=NEW.san_pham_id
   WHERE d.don_hang_id=NEW.don_hang_id
     AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
     AND datetime(d.het_han_giu_luc)>datetime('now')
     AND l.tinh_trang='DAT' AND date(l.han_su_dung)>date('now')
     AND (p.loai_thuoc!='RX' OR (
           d.trang_thai_toa='DA_DUYET'
           AND EXISTS(SELECT 1 FROM tai_khoan a
                      WHERE a.tai_khoan_id=d.nguoi_duyet_toa_id
                        AND a.co_quyen_duyet_toa=1)))
     AND NEW.so_luong_giu+COALESCE((
       SELECT SUM(other.so_luong_giu) FROM chi_tiet_don_hang other
       JOIN don_hang active_order ON active_order.don_hang_id=other.don_hang_id
       WHERE other.lo_thuoc_id=NEW.lo_thuoc_id
         AND other.chi_tiet_id<>OLD.chi_tiet_id
         AND active_order.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
         AND datetime(active_order.het_han_giu_luc)>datetime('now')),0)
         <=l.so_luong_ton
 ) THEN RAISE(ABORT,'INVALID_HOLD_UPDATE: status, Rx, lot or remaining stock') END;
END;

DROP TRIGGER IF EXISTS trg_order_release_before_finish;
CREATE TRIGGER trg_order_release_before_finish BEFORE UPDATE OF trang_thai ON don_hang
WHEN NEW.trang_thai IN ('DA_HUY','TU_CHOI','HOAN_TAT') BEGIN
 SELECT CASE WHEN EXISTS(SELECT 1 FROM chi_tiet_don_hang c
          WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_giu>0)
     THEN RAISE(ABORT,'RELEASE_HOLD_FIRST: release all lines in same transaction') END;
END;

DROP TRIGGER IF EXISTS trg_lot_stock_after_hold;
CREATE TRIGGER trg_lot_stock_after_hold BEFORE UPDATE OF so_luong_ton,tinh_trang,han_su_dung
ON lo_thuoc BEGIN
 SELECT CASE WHEN EXISTS(
   SELECT 1 FROM chi_tiet_don_hang c JOIN don_hang d ON d.don_hang_id=c.don_hang_id
   WHERE c.lo_thuoc_id=OLD.lo_thuoc_id AND c.so_luong_giu>0
     AND d.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
     AND datetime(d.het_han_giu_luc)>datetime('now')
     AND (NEW.tinh_trang!='DAT' OR date(NEW.han_su_dung)<=date('now')
          OR NEW.so_luong_ton < (
             SELECT COALESCE(SUM(c2.so_luong_giu),0)
             FROM chi_tiet_don_hang c2 JOIN don_hang d2 ON d2.don_hang_id=c2.don_hang_id
             WHERE c2.lo_thuoc_id=OLD.lo_thuoc_id
               AND d2.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
               AND datetime(d2.het_han_giu_luc)>datetime('now')))
 ) THEN RAISE(ABORT,'ACTIVE_HOLDS: release/adjust held lines before lot changes') END;
END;

 
-- A recorded issue/sale is allowed ONLY after the demo order has an eligible lot
-- and a simulated confirmed payment/delivery. For Rx, professional approval is required.
DROP TRIGGER IF EXISTS trg_issue_insert;
CREATE TRIGGER trg_issue_insert BEFORE INSERT ON chi_tiet_don_hang
WHEN NEW.so_luong_xuat>0 BEGIN
 SELECT CASE WHEN NOT EXISTS(
   SELECT 1 FROM don_hang d
   JOIN lo_thuoc l ON l.lo_thuoc_id=NEW.lo_thuoc_id AND l.san_pham_id=NEW.san_pham_id
   JOIN san_pham p ON p.san_pham_id=NEW.san_pham_id
   WHERE d.don_hang_id=NEW.don_hang_id
     AND (d.trang_thai IN ('SAN_SANG_NHAN','HOAN_TAT')
          OR (d.kenh_ban='POS' AND d.trang_thai='CHO_XU_LY'))
     AND d.trang_thai_tt IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
     AND d.thanh_toan_luc IS NOT NULL
     AND date(l.han_su_dung)>date(COALESCE(d.giao_luc,d.thanh_toan_luc))
     AND date(l.han_su_dung)>date('now')
     AND l.tinh_trang='DAT' AND p.dang_ban=1
     AND (p.loai_thuoc!='RX' OR (d.trang_thai_toa='DA_DUYET'
       AND EXISTS(SELECT 1 FROM tai_khoan a WHERE a.tai_khoan_id=d.nguoi_duyet_toa_id
                  AND a.co_quyen_duyet_toa=1)))
 ) THEN RAISE(ABORT,'INVALID_ISSUE: eligible batch, payment, delivery and Rx approval required') END;
END;

DROP TRIGGER IF EXISTS trg_issue_update;
CREATE TRIGGER trg_issue_update BEFORE UPDATE OF so_luong_xuat,lo_thuoc_id,san_pham_id,don_hang_id
ON chi_tiet_don_hang WHEN NEW.so_luong_xuat>0 BEGIN
 SELECT CASE WHEN NOT EXISTS(
   SELECT 1 FROM don_hang d
   JOIN lo_thuoc l ON l.lo_thuoc_id=NEW.lo_thuoc_id AND l.san_pham_id=NEW.san_pham_id
   JOIN san_pham p ON p.san_pham_id=NEW.san_pham_id
   WHERE d.don_hang_id=NEW.don_hang_id
     AND (d.trang_thai IN ('SAN_SANG_NHAN','HOAN_TAT')
          OR (d.kenh_ban='POS' AND d.trang_thai='CHO_XU_LY'))
     AND d.trang_thai_tt IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
     AND d.thanh_toan_luc IS NOT NULL
     AND date(l.han_su_dung)>date(COALESCE(d.giao_luc,d.thanh_toan_luc))
     AND date(l.han_su_dung)>date('now')
     AND l.tinh_trang='DAT' AND p.dang_ban=1
     AND (p.loai_thuoc!='RX' OR (d.trang_thai_toa='DA_DUYET'
       AND EXISTS(SELECT 1 FROM tai_khoan a WHERE a.tai_khoan_id=d.nguoi_duyet_toa_id
                  AND a.co_quyen_duyet_toa=1)))
 ) THEN RAISE(ABORT,'INVALID_ISSUE_UPDATE: eligible batch, payment, delivery and Rx approval required') END;
END;

DROP TRIGGER IF EXISTS trg_order_preserve_effects;
CREATE TRIGGER trg_order_preserve_effects BEFORE UPDATE OF trang_thai ON don_hang BEGIN
 SELECT CASE WHEN NEW.trang_thai NOT IN ('DA_GIU_HANG','SAN_SANG_NHAN')
   AND EXISTS(SELECT 1 FROM chi_tiet_don_hang c
     WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_giu>0)
   THEN RAISE(ABORT,'RELEASE_HOLD_FIRST: cannot change from held state') END;
 SELECT CASE WHEN NEW.trang_thai<>'HOAN_TAT'
   AND EXISTS(SELECT 1 FROM chi_tiet_don_hang c
     WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_xuat>0)
   THEN RAISE(ABORT,'ISSUED_LINES: cannot revert an issued order') END;
END;

-- State diagram: payment is recorded BEFORE issuing stock, handover then marks completed.
-- This is a transition guard; INSERT of a directly completed order is forbidden
-- in new data. Existing demo seed was imported before guards.
DROP TRIGGER IF EXISTS trg_complete_insert;
CREATE TRIGGER trg_complete_insert BEFORE INSERT ON don_hang
WHEN NEW.trang_thai='HOAN_TAT' BEGIN
 SELECT RAISE(ABORT,'COMPLETE_REQUIRES_EXISTING_PAID_ISSUED_ORDER');
END;

DROP TRIGGER IF EXISTS trg_complete_update;
CREATE TRIGGER trg_complete_update BEFORE UPDATE OF trang_thai ON don_hang
WHEN NEW.trang_thai='HOAN_TAT' AND OLD.trang_thai<>'HOAN_TAT' BEGIN
 SELECT CASE WHEN
   NEW.trang_thai_tt NOT IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
   OR NEW.thanh_toan_luc IS NULL OR NEW.giao_luc IS NULL
   OR NEW.so_tien_da_thu_vnd < COALESCE((
       SELECT SUM(c.so_luong*c.don_gia_chot_vnd)
       FROM chi_tiet_don_hang c WHERE c.don_hang_id=OLD.don_hang_id),0)
   OR EXISTS(SELECT 1 FROM chi_tiet_don_hang c
             JOIN lo_thuoc l ON l.lo_thuoc_id=c.lo_thuoc_id
             WHERE c.don_hang_id=OLD.don_hang_id
               AND date(l.han_su_dung)<=date(NEW.giao_luc))
   OR NOT EXISTS(SELECT 1 FROM chi_tiet_don_hang c WHERE c.don_hang_id=OLD.don_hang_id)
   OR EXISTS(SELECT 1 FROM chi_tiet_don_hang c
     WHERE c.don_hang_id=OLD.don_hang_id AND
       (c.so_luong_giu<>0 OR c.so_luong_xuat<>c.so_luong))
   THEN RAISE(ABORT,'INCOMPLETE_ISSUE_OR_HANDOFF') END;
END;

DROP TRIGGER IF EXISTS trg_payment_after_issue;
CREATE TRIGGER trg_payment_after_issue
BEFORE UPDATE OF trang_thai_tt,thanh_toan_luc,giao_luc ON don_hang
WHEN EXISTS(SELECT 1 FROM chi_tiet_don_hang c
            WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_xuat>0) BEGIN
 SELECT CASE WHEN
   NEW.trang_thai_tt NOT IN ('DEMO_DA_GHI_NHAN','DA_THANH_TOAN')
   OR NEW.thanh_toan_luc IS NULL
   OR (NEW.trang_thai='HOAN_TAT' AND NEW.giao_luc IS NULL)
   THEN RAISE(ABORT,'ISSUED_ORDER_CANNOT_LOSE_PAYMENT_OR_HANDOFF') END;
END;

-- A completed order cannot gain unchecked new lines or revert fulfillment
-- without a separate return/adjustment workflow (not present in six-table model).
DROP TRIGGER IF EXISTS trg_completed_line_insert;
CREATE TRIGGER trg_completed_line_insert BEFORE INSERT ON chi_tiet_don_hang
WHEN (SELECT d.trang_thai FROM don_hang d WHERE d.don_hang_id=NEW.don_hang_id)='HOAN_TAT'
BEGIN
 SELECT RAISE(ABORT,'COMPLETED_ORDER_LINES_ARE_IMMUTABLE');
END;

DROP TRIGGER IF EXISTS trg_completed_line_update;
CREATE TRIGGER trg_completed_line_update BEFORE UPDATE ON chi_tiet_don_hang
WHEN (SELECT d.trang_thai FROM don_hang d WHERE d.don_hang_id=OLD.don_hang_id)='HOAN_TAT'
BEGIN
 SELECT RAISE(ABORT,'COMPLETED_ORDER_LINES_ARE_IMMUTABLE');
END;

DROP TRIGGER IF EXISTS trg_completed_line_delete;
CREATE TRIGGER trg_completed_line_delete BEFORE DELETE ON chi_tiet_don_hang
WHEN (SELECT d.trang_thai FROM don_hang d WHERE d.don_hang_id=OLD.don_hang_id)='HOAN_TAT'
BEGIN
 SELECT RAISE(ABORT,'COMPLETED_ORDER_LINES_ARE_IMMUTABLE');
END;


-- Explicit physical stock movement, in the same SQL statement/transaction
-- as so_luong_xuat. This keeps the six-table model internally consistent.
-- The demo seed starts with physical stock already net of its issued lines;
-- guard creation runs AFTER seed import, so it never double-deducts seed.
DROP TRIGGER IF EXISTS trg_issue_stock_insert;
CREATE TRIGGER trg_issue_stock_insert AFTER INSERT ON chi_tiet_don_hang
WHEN NEW.so_luong_xuat>0 BEGIN
 UPDATE lo_thuoc
   SET so_luong_ton=so_luong_ton-NEW.so_luong_xuat
   WHERE lo_thuoc_id=NEW.lo_thuoc_id AND san_pham_id=NEW.san_pham_id
     AND so_luong_ton>=NEW.so_luong_xuat;
 SELECT CASE WHEN changes()<>1 THEN RAISE(ABORT,'INSUFFICIENT_PHYSICAL_STOCK_ON_ISSUE') END;
END;

DROP TRIGGER IF EXISTS trg_issue_stock_update;
CREATE TRIGGER trg_issue_stock_update AFTER UPDATE OF so_luong_xuat ON chi_tiet_don_hang
WHEN NEW.so_luong_xuat<>OLD.so_luong_xuat BEGIN
 UPDATE lo_thuoc SET so_luong_ton=so_luong_ton-(NEW.so_luong_xuat-OLD.so_luong_xuat)
 WHERE lo_thuoc_id=NEW.lo_thuoc_id AND san_pham_id=NEW.san_pham_id
   AND so_luong_ton>=NEW.so_luong_xuat-OLD.so_luong_xuat;
 SELECT CASE WHEN changes()<>1 THEN RAISE(ABORT,'INSUFFICIENT_PHYSICAL_STOCK_ON_ISSUE_UPDATE') END;
END;

DROP TRIGGER IF EXISTS trg_issued_line_identity;
CREATE TRIGGER trg_issued_line_identity BEFORE UPDATE OF san_pham_id,lo_thuoc_id,don_hang_id
ON chi_tiet_don_hang WHEN OLD.so_luong_xuat>0
 AND (NEW.san_pham_id<>OLD.san_pham_id
      OR NEW.don_hang_id<>OLD.don_hang_id
      OR NEW.lo_thuoc_id IS NOT OLD.lo_thuoc_id)
BEGIN SELECT RAISE(ABORT,'ISSUED_LINE_CANNOT_CHANGE_PRODUCT_LOT_OR_ORDER'); END;

DROP TRIGGER IF EXISTS trg_issued_line_delete;
CREATE TRIGGER trg_issued_line_delete BEFORE DELETE ON chi_tiet_don_hang
WHEN OLD.so_luong_xuat>0
BEGIN SELECT RAISE(ABORT,'ISSUED_LINE_CANNOT_BE_DELETED'); END;

 
-- Cross-row RX approval truth: a DA_DUYET flag alone is not approval.
DROP TRIGGER IF EXISTS trg_rx_approval_insert;
CREATE TRIGGER trg_rx_approval_insert BEFORE INSERT ON don_hang
WHEN NEW.trang_thai_toa='DA_DUYET' BEGIN
 SELECT CASE WHEN NEW.nguoi_duyet_toa_id IS NULL OR NEW.duyet_toa_luc IS NULL
    OR NOT EXISTS(SELECT 1 FROM tai_khoan a WHERE a.tai_khoan_id=NEW.nguoi_duyet_toa_id
      AND a.vai_tro='CASHIER' AND a.co_quyen_duyet_toa=1 AND a.trang_thai='HOAT_DONG')
  THEN RAISE(ABORT,'RX_APPROVER_REQUIRED') END;
END;
DROP TRIGGER IF EXISTS trg_rx_approval_update;
CREATE TRIGGER trg_rx_approval_update BEFORE UPDATE OF trang_thai_toa,nguoi_duyet_toa_id,duyet_toa_luc ON don_hang
BEGIN
 SELECT CASE WHEN NEW.trang_thai_toa='DA_DUYET'
   AND (NEW.nguoi_duyet_toa_id IS NULL OR NEW.duyet_toa_luc IS NULL
      OR NOT EXISTS(SELECT 1 FROM tai_khoan a WHERE a.tai_khoan_id=NEW.nguoi_duyet_toa_id
        AND a.vai_tro='CASHIER' AND a.co_quyen_duyet_toa=1 AND a.trang_thai='HOAT_DONG'))
   THEN RAISE(ABORT,'RX_APPROVER_REQUIRED') END;
 SELECT CASE WHEN OLD.trang_thai_toa='DA_DUYET' AND
   (NEW.trang_thai_toa<>'DA_DUYET'
    OR NEW.nguoi_duyet_toa_id IS NOT OLD.nguoi_duyet_toa_id)
   AND EXISTS(SELECT 1 FROM chi_tiet_don_hang c JOIN san_pham p ON p.san_pham_id=c.san_pham_id
              WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_xuat>0 AND p.loai_thuoc='RX')
   THEN RAISE(ABORT,'ISSUED_RX_APPROVAL_IMMUTABLE') END;
END;

-- Prevent a line from being moved into an already completed order.
DROP TRIGGER IF EXISTS trg_no_line_move_into_completed;
CREATE TRIGGER trg_no_line_move_into_completed
BEFORE UPDATE OF don_hang_id ON chi_tiet_don_hang
WHEN (SELECT trang_thai FROM don_hang WHERE don_hang_id=NEW.don_hang_id)='HOAN_TAT'
BEGIN SELECT RAISE(ABORT,'CANNOT_MOVE_LINES_INTO_COMPLETED_ORDER'); END;

-- A previously expired reservation cannot be silently reactivated to oversubscribe a lot.
DROP TRIGGER IF EXISTS trg_hold_order_reactivation;
CREATE TRIGGER trg_hold_order_reactivation
BEFORE UPDATE OF trang_thai,het_han_giu_luc ON don_hang
WHEN NEW.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
  AND datetime(NEW.het_han_giu_luc)>datetime('now')
BEGIN
 SELECT CASE WHEN EXISTS(
   SELECT 1 FROM chi_tiet_don_hang c JOIN lo_thuoc l ON l.lo_thuoc_id=c.lo_thuoc_id
   WHERE c.don_hang_id=OLD.don_hang_id AND c.so_luong_giu>0
   GROUP BY c.lo_thuoc_id
   HAVING SUM(c.so_luong_giu) + COALESCE((
      SELECT SUM(other.so_luong_giu) FROM chi_tiet_don_hang other
      JOIN don_hang od ON od.don_hang_id=other.don_hang_id
      WHERE other.lo_thuoc_id=c.lo_thuoc_id AND other.don_hang_id<>OLD.don_hang_id
        AND od.trang_thai IN ('DA_GIU_HANG','SAN_SANG_NHAN')
        AND datetime(od.het_han_giu_luc)>datetime('now')),0)>l.so_luong_ton
 ) THEN RAISE(ABORT,'REACTIVATED_HOLD_EXCEEDS_PHYSICAL_STOCK') END;
END;

-- Immutable manufacturer receipt quantity; corrections require an inventory-count approval.
DROP TRIGGER IF EXISTS trg_received_quantity_immutable;
CREATE TRIGGER trg_received_quantity_immutable BEFORE UPDATE OF so_luong_nhap ON lo_thuoc
WHEN NEW.so_luong_nhap<>OLD.so_luong_nhap BEGIN
 SELECT RAISE(ABORT,'RECEIPT_QUANTITY_IMMUTABLE_USE_APPROVED_COUNT');
END;

-- The only lawful reasons for stock delta within this 6-table DEMO model
-- are recorded line issuances or an approved stock-count correction.
DROP TRIGGER IF EXISTS trg_stock_must_match_ledger;
CREATE TRIGGER trg_stock_must_match_ledger BEFORE UPDATE OF so_luong_ton ON lo_thuoc
WHEN NEW.so_luong_ton<>OLD.so_luong_ton BEGIN
 SELECT CASE WHEN NEW.so_luong_ton <> (
       NEW.so_luong_nhap
       - COALESCE((SELECT SUM(c.so_luong_xuat) FROM chi_tiet_don_hang c
                   WHERE c.lo_thuoc_id=OLD.lo_thuoc_id),0)
       + COALESCE((SELECT SUM(k.ton_sau_dieu_chinh-k.ton_truoc_dieu_chinh)
                   FROM kiem_tra_lo k WHERE k.lo_thuoc_id=OLD.lo_thuoc_id
                   AND k.nguoi_duyet_dieu_chinh_id IS NOT NULL),0)
    ) THEN RAISE(ABORT,'UNAPPROVED_STOCK_ADJUSTMENT') END;
END;

-- Inventory adjustments are recorded/approved then applied to the same lot.
-- Role check is DEMO/admin authorization only, not a production RBAC substitute.
DROP TRIGGER IF EXISTS trg_count_approval_insert;
CREATE TRIGGER trg_count_approval_insert BEFORE INSERT ON kiem_tra_lo
WHEN NEW.nguoi_duyet_dieu_chinh_id IS NOT NULL BEGIN
 SELECT CASE WHEN NEW.loai_kiem<>'KIEM_KE' OR NEW.ket_qua<>'CHENH_LECH'
   OR NEW.ton_truoc_dieu_chinh IS NULL
   OR NEW.ton_truoc_dieu_chinh<>(SELECT so_luong_ton FROM lo_thuoc WHERE lo_thuoc_id=NEW.lo_thuoc_id)
   OR NOT EXISTS(SELECT 1 FROM tai_khoan a WHERE a.tai_khoan_id=NEW.nguoi_duyet_dieu_chinh_id
     AND a.vai_tro='ADMIN' AND a.trang_thai IN ('DEMO','HOAT_DONG'))
 THEN RAISE(ABORT,'INVALID_INVENTORY_APPROVAL') END;
END;
DROP TRIGGER IF EXISTS trg_count_approval_update;
CREATE TRIGGER trg_count_approval_update BEFORE UPDATE ON kiem_tra_lo
BEGIN
 SELECT CASE WHEN OLD.nguoi_duyet_dieu_chinh_id IS NOT NULL
   THEN RAISE(ABORT,'APPROVED_INVENTORY_RECORD_IMMUTABLE') END;
 SELECT CASE WHEN NEW.nguoi_duyet_dieu_chinh_id IS NOT NULL
   AND (NEW.loai_kiem<>'KIEM_KE' OR NEW.ket_qua<>'CHENH_LECH'
     OR NEW.ton_truoc_dieu_chinh IS NULL
     OR NEW.ton_truoc_dieu_chinh<>(SELECT so_luong_ton FROM lo_thuoc WHERE lo_thuoc_id=NEW.lo_thuoc_id)
     OR NOT EXISTS(SELECT 1 FROM tai_khoan a WHERE a.tai_khoan_id=NEW.nguoi_duyet_dieu_chinh_id
       AND a.vai_tro='ADMIN' AND a.trang_thai IN ('DEMO','HOAT_DONG')))
   THEN RAISE(ABORT,'INVALID_INVENTORY_APPROVAL') END;
END;
DROP TRIGGER IF EXISTS trg_count_approval_applied_insert;
CREATE TRIGGER trg_count_approval_applied_insert AFTER INSERT ON kiem_tra_lo
WHEN NEW.nguoi_duyet_dieu_chinh_id IS NOT NULL BEGIN
 UPDATE lo_thuoc SET so_luong_ton=NEW.ton_sau_dieu_chinh WHERE lo_thuoc_id=NEW.lo_thuoc_id;
END;
DROP TRIGGER IF EXISTS trg_count_approval_applied_update;
CREATE TRIGGER trg_count_approval_applied_update AFTER UPDATE ON kiem_tra_lo
WHEN OLD.nguoi_duyet_dieu_chinh_id IS NULL AND NEW.nguoi_duyet_dieu_chinh_id IS NOT NULL BEGIN
 UPDATE lo_thuoc SET so_luong_ton=NEW.ton_sau_dieu_chinh WHERE lo_thuoc_id=NEW.lo_thuoc_id;
END;
DROP TRIGGER IF EXISTS trg_count_approved_delete;
CREATE TRIGGER trg_count_approved_delete BEFORE DELETE ON kiem_tra_lo
WHEN OLD.nguoi_duyet_dieu_chinh_id IS NOT NULL BEGIN
 SELECT RAISE(ABORT,'APPROVED_INVENTORY_RECORD_CANNOT_BE_DELETED');
END;


DROP TRIGGER IF EXISTS trg_issued_quantity_no_rollback;
CREATE TRIGGER trg_issued_quantity_no_rollback BEFORE UPDATE OF so_luong_xuat
ON chi_tiet_don_hang WHEN NEW.so_luong_xuat<OLD.so_luong_xuat BEGIN
 SELECT RAISE(ABORT,'ISSUED_QUANTITY_IMMUTABLE_NEEDS_RETURN_WORKFLOW');
END;
